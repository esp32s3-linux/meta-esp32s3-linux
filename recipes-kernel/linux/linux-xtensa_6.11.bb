# SPDX-License-Identifier: MIT

SUMMARY = "ESP32-S3 Xtensa Linux kernel"
HOMEPAGE = "https://github.com/jcmvbkbc/linux-xtensa"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

# Resolve this branch to an immutable upstream commit before the first build.
SRC_URI = "git://github.com/jcmvbkbc/linux-xtensa.git;protocol=https;branch=xtensa-6.11-esp32-tag \
           file://devkit_c1_8m_linux.config"
SRCREV = "92e07a0857dbd5334906e23256512c310230c522"
S = "${WORKDIR}/git"

LINUX_VERSION = "6.11"
PV = "${LINUX_VERSION}+git${SRCPV}"

inherit kernel

COMPATIBLE_HOST = ".*"
KERNEL_IMAGETYPE = "xipImage"
KERNEL_CONFIG_COMMAND = "oe_runmake -C ${S} O=${B} ${KERNEL_DEFCONFIG}"
KERNEL_DEFCONFIG = "devkit_c1_8m_linux.config"
KERNEL_IMAGE_NAME = "${MACHINE}"

do_configure:prepend() {
    install -Dm0644 ${WORKDIR}/devkit_c1_8m_linux.config ${B}/.config
}

do_deploy:append() {
    install -Dm0644 ${KERNEL_OUTPUT_DIR}/xipImage ${DEPLOYDIR}/xipImage
}
