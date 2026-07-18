# SPDX-License-Identifier: MIT

SUMMARY = "Deploy the ESP-Hosted firmware set for ESP32-S3"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# The first Yocto milestone consumes an ESP-IDF build produced from the pinned
# ESP-Hosted checkout. This avoids silently using an arbitrary host ESP-IDF
# environment. Convert this recipe into a fully native ESP-IDF build after its
# downloads and Python dependencies are mirrored.
ESP_HOSTED_BUILD_DIR ?= ""
ESP32S3_PARTITION_TABLE_FILE ?= ""

SRC_URI = "${@'file://' + d.getVar('ESP32S3_PARTITION_TABLE_FILE') if d.getVar('ESP32S3_PARTITION_TABLE_FILE') else ''}"
S = "${UNPACKDIR}"

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

    source = d.getVar("ESP_HOSTED_BUILD_DIR")
    deploy = d.getVar("DEPLOYDIR")
    if not source:
        bb.fatal("ESP_HOSTED_BUILD_DIR must name a completed network_adapter build directory")

    config = os.path.join(source, "flasher_args.json")
    if not os.path.isfile(config):
        bb.fatal("ESP_HOSTED_BUILD_DIR does not contain flasher_args.json: %s" % source)

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
    bb.utils.mkdirhier(deploy)
    for filename in sorted(files):
        matches = []
        for root, _, names in os.walk(source):
            if filename in names:
                matches.append(os.path.join(root, filename))
        if len(matches) != 1:
            bb.fatal("cannot uniquely locate firmware artifact %s below %s" % (filename, source))
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
