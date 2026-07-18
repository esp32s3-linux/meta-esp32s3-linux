# SPDX-License-Identifier: MIT

SUMMARY = "Minimal package-built CramFS image for ESP32-S3 Linux"
LICENSE = "MIT"

inherit image

IMAGE_INSTALL = "\
    base-files \
    busybox \
    iw \
    wireless-regdb-static \
    wpa-supplicant \
"

IMAGE_LINGUAS = " "
IMAGE_FSTYPES = "cramfs"
IMAGE_FEATURES = "read-only-rootfs"
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
    cat > ${IMAGE_ROOTFS}${sysconfdir}/fstab <<'EOF'
proc /proc proc defaults 0 0
devpts /dev/pts devpts defaults,gid=5,mode=620,ptmxmode=0666 0 0
tmpfs /run tmpfs mode=0755,nosuid,nodev 0 0
mtd:etc /etc jffs2 nofail 0 0
EOF
    cat > ${IMAGE_ROOTFS}${sysconfdir}/inittab <<'EOF'
::sysinit:/bin/mount -t proc proc /proc
::sysinit:/bin/mkdir -p /dev/pts
::sysinit:/bin/mount -a
::sysinit:/sbin/swapon -a
null::sysinit:/bin/ln -sf /proc/self/fd /dev/fd
null::sysinit:/bin/ln -sf /proc/self/fd/0 /dev/stdin
null::sysinit:/bin/ln -sf /proc/self/fd/1 /dev/stdout
null::sysinit:/bin/ln -sf /proc/self/fd/2 /dev/stderr
::sysinit:/bin/hostname -F /etc/hostname
::sysinit:/etc/init.d/rcS
ttyS0::respawn:/sbin/getty -L 115200 ttyS0 vt100
::shutdown:/etc/init.d/rcK
::shutdown:/sbin/swapoff -a
::shutdown:/bin/umount -a -r
EOF
}

esp32s3_drop_ldcache() {
    rm -f ${IMAGE_ROOTFS}${sysconfdir}/ld.so.cache
}

# The ESP32-S3 no-MMU kernel executes aligned binaries directly from flash.
IMAGE_CMD:cramfs:forcevariable = "mkcramfs -L -X ${IMAGE_ROOTFS} ${IMGDEPLOYDIR}/${IMAGE_NAME}.cramfs"
do_image_cramfs[depends] = "cramfs-tools-native:do_populate_sysroot"
