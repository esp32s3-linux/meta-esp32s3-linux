# SPDX-License-Identifier: MIT

SUMMARY = "Initial writable /etc JFFS2 image for ESP32-S3"
LICENSE = "MIT"

inherit image

IMAGE_FSTYPES = "jffs2"
IMAGE_NAME = "etc"
IMAGE_LINK_NAME = "etc"
EXTRA_IMAGECMD:jffs2 = "--little-endian --eraseblock=0x10000"

IMAGE_INSTALL = "base-files"
IMAGE_PREPROCESS_COMMAND += "esp32s3_prepare_etc;"

esp32s3_prepare_etc() {
    rm -rf ${IMAGE_ROOTFS}/*
    install -d ${IMAGE_ROOTFS}/etc
    install -m0644 ${WORKDIR}/fstab ${IMAGE_ROOTFS}/etc/fstab
}

SRC_URI = "file://fstab"
