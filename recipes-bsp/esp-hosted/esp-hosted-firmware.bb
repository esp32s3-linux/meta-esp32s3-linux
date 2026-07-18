# SPDX-License-Identifier: MIT

SUMMARY = "Deploy the ESP-Hosted firmware set for ESP32-S3"
LICENSE = "Apache-2.0"

# The first Yocto milestone consumes an ESP-IDF build produced from the pinned
# ESP-Hosted checkout. This avoids silently using an arbitrary host ESP-IDF
# environment. Convert this recipe into a fully native ESP-IDF build after its
# downloads and Python dependencies are mirrored.
ESP_HOSTED_BUILD_DIR ?= ""

inherit deploy

do_compile[noexec] = "1"

python do_deploy() {
    import json
    import os
    import shutil
    import bb

    source = d.getVar("ESP_HOSTED_BUILD_DIR")
    deploy = d.getVar("DEPLOYDIR")
    if not source:
        bb.fatal("ESP_HOSTED_BUILD_DIR must name a completed network_adapter build directory")

    config = os.path.join(source, "flasher_args.json")
    if not os.path.isfile(config):
        bb.fatal("ESP_HOSTED_BUILD_DIR does not contain flasher_args.json: %s" % source)

    with open(config, encoding="utf-8") as stream:
        flash_files = json.load(stream).get("flash_files", {})

    files = {"flasher_args.json", "partition-table.bin"}
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
}

addtask deploy after do_compile before do_build
