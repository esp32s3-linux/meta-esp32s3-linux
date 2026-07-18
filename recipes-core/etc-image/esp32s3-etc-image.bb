# SPDX-License-Identifier: MIT

SUMMARY = "Initial writable /etc JFFS2 image for ESP32-S3"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

DEPENDS = "mtd-utils-native"

INHIBIT_DEFAULT_DEPS = "1"
SRC_URI = "file://fstab file://passwd file://shadow file://group file://shells \
           file://hostname file://hosts file://profile file://inittab file://rcS file://rcK"

S = "${UNPACKDIR}"

do_configure[noexec] = "1"

do_compile() {
    install -d ${B}/rootfs/init.d
    install -m 0644 ${S}/fstab ${B}/rootfs/fstab
    install -m 0644 ${S}/passwd ${B}/rootfs/passwd
    install -m 0600 ${S}/shadow ${B}/rootfs/shadow
    install -m 0644 ${S}/group ${B}/rootfs/group
    install -m 0644 ${S}/shells ${B}/rootfs/shells
    install -m 0644 ${S}/hostname ${B}/rootfs/hostname
    install -m 0644 ${S}/hosts ${B}/rootfs/hosts
    install -m 0644 ${S}/profile ${B}/rootfs/profile
    install -m 0644 ${S}/inittab ${B}/rootfs/inittab
    install -m 0755 ${S}/rcS ${B}/rootfs/init.d/rcS
    install -m 0755 ${S}/rcK ${B}/rootfs/init.d/rcK
    mkfs.jffs2 --little-endian --eraseblock=0x10000 \
        --pad=0x70000 --root=${B}/rootfs --output=${B}/etc.jffs2
}

do_deploy() {
    install -Dm 0644 ${B}/etc.jffs2 ${DEPLOYDIR}/etc.jffs2
}

addtask deploy after do_compile before do_build
