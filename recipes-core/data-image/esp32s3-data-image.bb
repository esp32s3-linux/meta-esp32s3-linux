# SPDX-License-Identifier: MIT

SUMMARY = "Initial writable /data JFFS2 image for ESP32-S3"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

DEPENDS = "mtd-utils-native"
INHIBIT_DEFAULT_DEPS = "1"

do_configure[noexec] = "1"

do_compile() {
    install -d ${B}/rootfs
    mkfs.jffs2 --little-endian --eraseblock=0x10000 \
        --pad=0x400000 --root=${B}/rootfs --output=${B}/data.jffs2
}

do_deploy() {
    install -Dm 0644 ${B}/data.jffs2 ${DEPLOYDIR}/data.jffs2
}

addtask deploy after do_compile before do_build
