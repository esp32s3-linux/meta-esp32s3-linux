# SPDX-License-Identifier: MIT

SUMMARY = "Minimal CramFS image for ESP32-S3 Linux"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy nopackages

ESP32S3_ROOTFS_IMAGE ?= ""
ESP32S3_ROOTFS_DIR ?= ""
ESP32S3_MKCRAMFS ?= ""

INHIBIT_DEFAULT_DEPS = "1"
do_configure[noexec] = "1"

do_compile() {
    if [ ! -d "${ESP32S3_ROOTFS_DIR}" ]; then
        bbfatal "ESP32S3_ROOTFS_DIR must name a completed root filesystem directory"
    fi
    if [ ! -x "${ESP32S3_MKCRAMFS}" ]; then
        bbfatal "ESP32S3_MKCRAMFS must name an XIP-capable mkcramfs executable"
    fi
    rm -rf ${B}/rootfs
    cp -a "${ESP32S3_ROOTFS_DIR}" ${B}/rootfs
    install -d -m 0755 ${B}/rootfs/data
    "${ESP32S3_MKCRAMFS}" -L -X ${B}/rootfs ${B}/rootfs.cramfs
}

do_deploy() {
    install -Dm 0644 ${B}/rootfs.cramfs ${DEPLOYDIR}/rootfs.cramfs
}

addtask deploy after do_compile before do_build
