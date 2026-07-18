#!/usr/bin/env python3
"""Flash an ESP32-S3 bundle using its generated partition table."""

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
        if magic != 0x50AA:
            raise RuntimeError(f"invalid partition entry at offset {offset:#x}")
        result[label.split(b"\0", 1)[0].decode("ascii")] = (address, size)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", required=True)
    parser.add_argument("--bundle", required=True, type=Path)
    parser.add_argument("--baud", default="2000000")
    parser.add_argument("--preserve-etc", action="store_true")
    args = parser.parse_args()

    esptool = shutil.which("esptool")
    if not esptool:
        raise RuntimeError("install esptool first: python3 -m pip install --user esptool")

    config_path = args.bundle / "flasher_args.json"
    config = json.loads(config_path.read_text())
    extra = config.get("extra_esptool_args", {})
    settings = config.get("flash_settings", {})
    command = [esptool, "--chip", extra.get("chip", "esp32s3"), "--port", args.port, "--baud", args.baud]
    for option in ("before", "after"):
        if option in extra:
            command.extend([f"--{option}", str(extra[option])])
    if extra.get("stub") is False:
        command.append("--no-stub")
    command.append("write-flash")
    for key, option in (("flash_mode", "--flash-mode"), ("flash_freq", "--flash-freq"), ("flash_size", "--flash-size")):
        if key in settings:
            command.extend([option, str(settings[key])])
    for address, filename in config["flash_files"].items():
        artifact = args.bundle / Path(filename).name
        if not artifact.is_file():
            raise RuntimeError(f"missing firmware artifact: {artifact}")
        command.extend([address, str(artifact)])
    subprocess.run(command, check=True)

    table = partitions(args.bundle / "partition-table.bin")
    images = {"linux": "xipImage", "rootfs": "rootfs.cramfs"}
    if not args.preserve_etc:
        images["etc"] = "etc.jffs2"
    command = [esptool, "--chip", extra.get("chip", "esp32s3"), "--port", args.port, "--baud", args.baud, "write-flash"]
    for label, filename in images.items():
        if label not in table:
            raise RuntimeError(f"partition table does not contain {label!r}")
        image = args.bundle / filename
        address, size = table[label]
        if not image.is_file() or image.stat().st_size > size:
            raise RuntimeError(f"invalid or oversized {label} image: {image}")
        command.extend([f"{address:#x}", str(image)])
    subprocess.run(command, check=True)


if __name__ == "__main__":
    main()
