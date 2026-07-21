# SPDX-License-Identifier: MIT

SUMMARY = "Deploy the pinned ESP-Hosted firmware set for ESP32-S3"
LICENSE = "CLOSED"

ESP_HOSTED_FIRMWARE_DIR = "esp-hosted-20260721"
ESP32S3_PARTITION_TABLE_FILE ?= ""

SRC_URI = "file://esp-hosted-esp32s3-16mb-20260721.tar.xz;name=firmware \
           ${@'file://' + d.getVar('ESP32S3_PARTITION_TABLE_FILE') if d.getVar('ESP32S3_PARTITION_TABLE_FILE') else ''}"
SRC_URI[firmware.sha256sum] = "d19060d6d72141c7e5def7e8b5b0a4fbdb24a648feb99cb60b15ff68dec4d931"
S = "${UNPACKDIR}/${ESP_HOSTED_FIRMWARE_DIR}"

inherit deploy nopackages

INHIBIT_DEFAULT_DEPS = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

python do_deploy() {
    import hashlib
    import json
    import os
    import shutil
    import struct
    import bb

    source = os.path.join(d.getVar("UNPACKDIR"), d.getVar("ESP_HOSTED_FIRMWARE_DIR"))
    deploy = d.getVar("DEPLOYDIR")
    config = os.path.join(source, "flasher_args.json")
    if not os.path.isfile(config):
        bb.fatal("firmware archive does not contain flasher_args.json: %s" % source)

    with open(config, encoding="utf-8") as stream:
        flash_config = json.load(stream)
    flash_files = flash_config.get("flash_files", {})
    configured_size = flash_config.get("flash_settings", {}).get("flash_size")
    expected_size = d.getVar("ESP32S3_FLASH_SIZE")
    if configured_size != expected_size:
        bb.fatal("ESP-Hosted flash size %s does not match machine flash size %s" %
                 (configured_size, expected_size))

    files = {"flasher_args.json"}
    files.update(os.path.basename(filename) for filename in flash_files.values())
    expected_hashes = {
        "flasher_args.json": "23b9497aefa629a35edcef67f8319df079b0e2c07ac8f7af78522cb7b522f2fd",
        "bootloader.bin": "35f47b8de37f62e586909550ae65cc3341c59a29c918e1a567b3e57fadb0681a",
        "network_adapter.bin": "8e70c1afbe7d1c56a84882a27bb8ee87408d5489cdedfd4ec8ce6c963ddf868c",
        "partition-table.bin": "af14e87cc97a09b34722f2aa5744566aee64e502ae965324d329d66e4e252456",
    }
    bb.utils.mkdirhier(deploy)
    for filename in sorted(files):
        matches = []
        for root, _, names in os.walk(source):
            if filename in names:
                matches.append(os.path.join(root, filename))
        if len(matches) != 1:
            bb.fatal("cannot uniquely locate firmware artifact %s below %s" % (filename, source))
        checksum = hashlib.sha256()
        with open(matches[0], "rb") as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                checksum.update(block)
        digest = checksum.hexdigest()
        if digest != expected_hashes.get(filename):
            bb.fatal("unexpected checksum for firmware artifact %s" % filename)
        shutil.copy2(matches[0], os.path.join(deploy, filename))

    partition_file = d.getVar("ESP32S3_PARTITION_TABLE_FILE")
    if not partition_file:
        bb.fatal("ESP32S3_PARTITION_TABLE_FILE must name a partition table CSV")
    partition_source = os.path.join(d.getVar("UNPACKDIR"), partition_file)
    types = {"app": 0x00, "data": 0x01}
    subtypes = {(0x00, "factory"): 0x00, (0x01, "phy"): 0x01, (0x01, "nvs"): 0x02}
    entries = bytearray()
    with open(partition_source, encoding="utf-8") as stream:
        for number, line in enumerate(stream, 1):
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            fields = [field.strip() for field in line.split(",")]
            if len(fields) != 5:
                bb.fatal("invalid partition table line %d: %s" % (number, line))
            label, type_value, subtype_value, offset, size = fields
            part_type = types.get(type_value, int(type_value, 0) if type_value.startswith("0x") else None)
            if part_type is None:
                bb.fatal("invalid partition type on line %d: %s" % (number, type_value))
            part_subtype = subtypes.get((part_type, subtype_value),
                                        int(subtype_value, 0) if subtype_value.startswith("0x") else None)
            if part_subtype is None:
                bb.fatal("invalid partition subtype on line %d: %s" % (number, subtype_value))
            encoded_label = label.encode("ascii")
            if len(encoded_label) > 15:
                bb.fatal("partition label is too long on line %d: %s" % (number, label))
            entries.extend(struct.pack("<HBBII16sI", 0x50AA, part_type, part_subtype,
                                       int(offset, 0), int(size, 0), encoded_label, 0))
    entries.extend(b"\xEB\xEB" + b"\xFF" * 14 + hashlib.md5(entries).digest())
    if len(entries) > 0xC00:
        bb.fatal("partition table exceeds 0xC00 bytes")
    entries.extend(b"\xFF" * (0x1000 - len(entries)))
    with open(os.path.join(deploy, "partition-table.bin"), "wb") as stream:
        stream.write(entries)
}

addtask deploy after do_compile before do_build
