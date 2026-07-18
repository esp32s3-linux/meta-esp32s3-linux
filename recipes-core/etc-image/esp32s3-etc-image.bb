# SPDX-License-Identifier: MIT

SUMMARY = "Initial writable /etc JFFS2 image for ESP32-S3"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

DEPENDS = "mtd-utils-native"

INHIBIT_DEFAULT_DEPS = "1"
SRC_URI = "file://fstab file://passwd file://shadow file://group file://shells"

S = "${UNPACKDIR}"

do_configure[noexec] = "1"

do_compile() {
    install -d ${B}/rootfs/etc
    install -m 0644 ${S}/fstab ${B}/rootfs/etc/fstab
    install -m 0644 ${S}/passwd ${B}/rootfs/etc/passwd
    install -m 0600 ${S}/shadow ${B}/rootfs/etc/shadow
    install -m 0644 ${S}/group ${B}/rootfs/etc/group
    install -m 0644 ${S}/shells ${B}/rootfs/etc/shells
    mkfs.jffs2 --little-endian --eraseblock=0x10000 \
        --root=${B}/rootfs --output=${B}/etc.jffs2
}

do_deploy() {
    install -Dm 0644 ${B}/etc.jffs2 ${DEPLOYDIR}/etc.jffs2
}

addtask deploy after do_compile before do_build
