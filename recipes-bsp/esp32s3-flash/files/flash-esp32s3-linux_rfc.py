#!/usr/bin/env python3
"""Flash an ESP32-S3 bundle through this workspace's RFC2217 endpoint."""

import argparse
import json
import shutil
import struct
import subprocess
from pathlib import Path


def partitions(path):
    result = {}
    data = path.read_bytes()
    for offset in range(0, len(data), 32):
        entry = data[offset : offset + 32]
        if len(entry) < 32 or entry == b"\xff" * 32:
            break
        magic, _type, _subtype, address, size, label, _flags = struct.unpack("<HBBII16sI", entry)
        if magic == 0xEBEB:
            break
        if magic != 0x50AA:
            raise RuntimeError(f"invalid partition entry at offset {offset:#x}")
        result[label.split(b"\0", 1)[0].decode("ascii")] = (address, size)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="rfc2217://127.0.0.1:14001?ign_set_control")
    parser.add_argument("--bundle", default=Path(__file__).resolve().parent, type=Path)
    parser.add_argument("--baud", default="2000000")
    parser.add_argument("--preserve-etc", action="store_true")
    parser.add_argument("--preserve-data", action="store_true")
    args = parser.parse_args()

    esptool = shutil.which("esptool.py")
    if not esptool:
        raise RuntimeError("source ~/esp/v5.5/esp-idf/export.sh first")

    config = json.loads((args.bundle / "flasher_args.json").read_text())
    extra = config.get("extra_esptool_args", {})
    settings = config.get("flash_settings", {})
    table = partitions(args.bundle / "partition-table.bin")
    images = {"linux": "xipImage", "rootfs": "rootfs.cramfs"}
    if not args.preserve_etc:
        images["etc"] = "etc.jffs2"
    if not args.preserve_data:
        images["data"] = "data.jffs2"
    for label, filename in images.items():
        if label not in table:
            raise RuntimeError(f"partition table does not contain {label!r}")
        image = args.bundle / filename
        _, size = table[label]
        if not image.is_file() or image.stat().st_size > size:
            raise RuntimeError(f"invalid or oversized {label} image: {image}")

    firmware = []
    for address, filename in config["flash_files"].items():
        artifact = args.bundle / Path(filename).name
        if not artifact.is_file():
            raise RuntimeError(f"missing firmware artifact: {artifact}")
        firmware.extend([address, str(artifact)])

    command = [esptool, "--chip", extra.get("chip", "esp32s3"), "--port", args.port, "--baud", args.baud]
    for option in ("before", "after"):
        if option in extra:
            command.extend([f"--{option}", str(extra[option])])
    if extra.get("stub") is False:
        command.append("--no-stub")
    command.append("write_flash")
    for key in ("flash_mode", "flash_freq", "flash_size"):
        if key in settings:
            command.extend([f"--{key}", str(settings[key])])
    command.extend(firmware)
    subprocess.run(command, check=True)

    command = [esptool, "--chip", extra.get("chip", "esp32s3"), "--port", args.port, "--baud", args.baud, "write_flash"]
    for label, filename in images.items():
        image = args.bundle / filename
        address, _ = table[label]
        command.extend([f"{address:#x}", str(image)])
    subprocess.run(command, check=True)


if __name__ == "__main__":
    main()
