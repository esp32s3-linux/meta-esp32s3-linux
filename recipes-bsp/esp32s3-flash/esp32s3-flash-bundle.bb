# SPDX-License-Identifier: MIT

SUMMARY = "Gather ESP32-S3 firmware and Linux flash artifacts"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

SRC_URI = "file://flash-esp32s3-linux.py"
S = "${UNPACKDIR}"

DEPENDS = "esp-hosted-firmware virtual/kernel esp32s3-minimal-image esp32s3-data-image"
INHIBIT_DEFAULT_DEPS = "1"

do_compile[noexec] = "1"
do_deploy[depends] += "esp-hosted-firmware:do_deploy virtual/kernel:do_deploy esp32s3-minimal-image:do_image_complete esp32s3-data-image:do_deploy"

do_deploy() {
    bundle="${DEPLOYDIR}/${MACHINE}-flash-bundle"
    rm -rf "$bundle"
    install -d "$bundle"

    for artifact in flasher_args.json partition-table.bin xipImage rootfs.cramfs etc.jffs2 data.jffs2; do
        install -m0644 "${DEPLOY_DIR_IMAGE}/${artifact}" "$bundle/${artifact}"
    done
    install -m0755 "${S}/flash-esp32s3-linux.py" "$bundle/flash-esp32s3-linux.py"

    # Copy every ESP-IDF image named by flasher_args.json, not an assumed list.
    python3 - "${DEPLOY_DIR_IMAGE}/flasher_args.json" "${DEPLOY_DIR_IMAGE}" "$bundle" <<'PY'
import json
import shutil
import sys
from pathlib import Path

config_path, deploy_dir, bundle = map(Path, sys.argv[1:])
for filename in json.loads(config_path.read_text())["flash_files"].values():
    source = deploy_dir / Path(filename).name
    if not source.is_file():
        raise SystemExit(f"missing ESP-IDF deploy artifact: {source}")
    shutil.copy2(source, bundle / source.name)
PY
}

addtask deploy after do_compile before do_build
