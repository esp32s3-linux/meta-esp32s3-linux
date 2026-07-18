# SPDX-License-Identifier: MIT

SUMMARY = "Minimal CramFS image for ESP32-S3 Linux"
LICENSE = "MIT"

inherit core-image

IMAGE_FSTYPES = "cramfs"
IMAGE_INSTALL = "busybox base-files base-passwd ${MACHINE_ESSENTIAL_EXTRA_RDEPENDS}"

IMAGE_NAME = "rootfs"
IMAGE_LINK_NAME = "rootfs"

# `/etc` is provided by a separate persistent JFFS2 image.
IMAGE_ROOTFS_EXTRA_SPACE = "0"
