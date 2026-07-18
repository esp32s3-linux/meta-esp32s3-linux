# SPDX-License-Identifier: MIT

SUMMARY = "Minimal CramFS image for ESP32-S3 Linux"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

ESP32S3_ROOTFS_IMAGE ?= ""

INHIBIT_DEFAULT_DEPS = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_deploy() {
    if [ ! -f "${ESP32S3_ROOTFS_IMAGE}" ]; then
        bbfatal "ESP32S3_ROOTFS_IMAGE must name a completed CramFS image"
    fi
    install -Dm 0644 "${ESP32S3_ROOTFS_IMAGE}" "${DEPLOYDIR}/rootfs.cramfs"
}

addtask deploy after do_compile before do_build
