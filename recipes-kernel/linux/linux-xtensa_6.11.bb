# SPDX-License-Identifier: MIT

SUMMARY = "ESP32-S3 Xtensa Linux kernel"
HOMEPAGE = "https://github.com/jcmvbkbc/linux-xtensa"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

SRC_URI = "git://github.com/jcmvbkbc/linux-xtensa.git;protocol=https;nobranch=1 \
           file://devkit_c1_8m_linux.config"
# Commit referenced by the signed xtensa-6.11-esp32-tag annotated tag.
SRCREV = "3b01ad2a1f71b72b27fefa08e4bf6acbe1de874f"
LINUX_VERSION = "6.11"
PV = "${LINUX_VERSION}+git${SRCPV}"

inherit kernel

S = "${UNPACKDIR}/${BP}"

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
