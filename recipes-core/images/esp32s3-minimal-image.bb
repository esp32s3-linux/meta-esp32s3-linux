# SPDX-License-Identifier: MIT

SUMMARY = "Minimal package-built CramFS image for ESP32-S3 Linux"
LICENSE = "MIT"

inherit image

IMAGE_INSTALL = "\
    base-files \
    base-passwd \
    busybox \
    busybox-udhcpc \
    esp32s3-etc-defaults \
    iw \
    wireless-regdb-static \
    wpa-supplicant \
"

IMAGE_LINGUAS = " "
IMAGE_FSTYPES = "cramfs"
IMAGE_FEATURES = "read-only-rootfs empty-root-password"
IMAGE_NAME_SUFFIX = ""
IMAGE_LINK_NAME = "rootfs"
IMAGE_ROOTFS_SIZE = "4096"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
NO_RECOMMENDATIONS = "1"
PACKAGE_EXCLUDE = "bash bash-completion ptest-runner"
BAD_RECOMMENDATIONS = "bash bash-completion ptest-runner"
USE_DEPMOD = "0"
KERNELDEPMODDEPEND = ""
KERNEL_DEPLOY_DEPEND = ""
TOOLCHAIN_HOST_TASK = ""
TOOLCHAIN_TARGET_TASK = ""
ROOTFS_BOOTSTRAP_INSTALL = ""
PACKAGE_INSTALL = "${IMAGE_INSTALL}"

ROOTFS_POSTPROCESS_COMMAND += "esp32s3_prepare_bootstrap_etc; "
IMAGE_PREPROCESS_COMMAND += "esp32s3_drop_ldcache; "

esp32s3_prepare_bootstrap_etc() {
    install -d ${IMAGE_ROOTFS}/data
    cp -a ${IMAGE_ROOTFS}${datadir}/esp32s3-etc-defaults/. ${IMAGE_ROOTFS}${sysconfdir}/
    rm -rf ${IMAGE_ROOTFS}${datadir}/esp32s3-etc-defaults
}

esp32s3_drop_ldcache() {
    rm -f ${IMAGE_ROOTFS}${sysconfdir}/ld.so.cache
}

# The ESP32-S3 no-MMU kernel executes aligned binaries directly from flash.
IMAGE_CMD:cramfs:forcevariable = "mkcramfs -L -X ${IMAGE_ROOTFS} ${IMGDEPLOYDIR}/${IMAGE_NAME}.cramfs"
do_image_cramfs[depends] = "cramfs-tools-native:do_populate_sysroot"

do_image_etc_jffs2() {
    rm -rf ${WORKDIR}/etc-rootfs
    install -d ${WORKDIR}/etc-rootfs
    cp -a --no-preserve=ownership ${IMAGE_ROOTFS}${sysconfdir}/. ${WORKDIR}/etc-rootfs/

    mkfs.jffs2 --little-endian --eraseblock=0x10000 --pad=0x70000 \
        --root=${WORKDIR}/etc-rootfs --output=${IMGDEPLOYDIR}/etc.jffs2
}
do_image_etc_jffs2[depends] = "mtd-utils-native:do_populate_sysroot"
addtask image_etc_jffs2 after do_image before do_image_complete
