# SPDX-License-Identifier: MIT

SUMMARY = "Gather ESP32-S3 firmware and Linux flash artifacts"
LICENSE = "MIT"

inherit deploy nopackages

DEPENDS = "esp-hosted-firmware virtual/kernel esp32s3-minimal-image esp32s3-etc-image"
INHIBIT_DEFAULT_DEPS = "1"

do_compile[noexec] = "1"
do_deploy[depends] += "esp-hosted-firmware:do_deploy virtual/kernel:do_deploy esp32s3-minimal-image:do_deploy esp32s3-etc-image:do_deploy"

do_deploy() {
    bundle="${DEPLOYDIR}/${MACHINE}-flash-bundle"
    rm -rf "$bundle"
    install -d "$bundle"

    for artifact in flasher_args.json partition-table.bin xipImage rootfs.cramfs etc.jffs2; do
        install -m0644 "${DEPLOY_DIR_IMAGE}/${artifact}" "$bundle/${artifact}"
    done

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
