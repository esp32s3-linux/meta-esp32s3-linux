# SPDX-License-Identifier: MIT

SUMMARY = "ESP32-S3 Xtensa Linux kernel"
HOMEPAGE = "https://github.com/jcmvbkbc/linux-xtensa"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

SRC_URI = "git://github.com/jcmvbkbc/linux-xtensa.git;protocol=https;branch=xtensa-6.11-esp32-tag \
           file://devkit_c1_8m_linux.config"
# Buildroot pins this symbolic revision. The local build configuration may
# override SRC_URI with its downloaded git4 archive when upstream removes it.
SRCREV = "xtensa-6.11-esp32-tag"
LINUX_VERSION = "6.11"
PV = "${LINUX_VERSION}+git${SRCPV}"

inherit kernel

COMPATIBLE_HOST = ".*"
DEPENDS:remove = "virtual/cross-binutils virtual/cross-cc"
PATH:prepend = "${XTENSA_EXTERNAL_TOOLCHAIN}/bin:"

KERNEL_IMAGETYPE = "xipImage"
KERNEL_CONFIG_COMMAND = "oe_runmake -C ${S} O=${B} olddefconfig"
KERNEL_IMAGE_NAME = "${MACHINE}"

do_configure:prepend() {
    install -Dm0644 ${UNPACKDIR}/devkit_c1_8m_linux.config ${B}/.config
}

do_deploy:append() {
    install -Dm0644 ${KERNEL_OUTPUT_DIR}/xipImage ${DEPLOYDIR}/xipImage
}
