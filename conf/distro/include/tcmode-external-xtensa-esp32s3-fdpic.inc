# SPDX-License-Identifier: MIT
#
# The ESP32-S3 compiler is a Crosstool-NG Xtensa call0 FDPIC/uClibc-ng
# toolchain. Its recipe stages the compiler and dynamic configuration in every
# target recipe's native sysroot.

XTENSA_EXTERNAL_TOOLCHAIN = "${RECIPE_SYSROOT_NATIVE}/sysroot-only/xtensa-esp32s3-toolchain"
XTENSA_GNU_CONFIG = "${XTENSA_EXTERNAL_TOOLCHAIN}/xtensa-dynconfig/esp32s3.so"

require conf/distro/include/tcmode-default.inc

TCMODE = "external-xtensa-esp32s3-fdpic"
TCLIBC = "uclibc"
TARGET_PREFIX = "xtensa-esp32s3-linux-uclibcfdpic-"
TARGET_VENDOR = "-esp32s3"
TARGET_OS = "linux-uclibcfdpic"

PREFERRED_TOOLCHAIN_TARGET = "external-xtensa-esp32s3"
PREFERRED_PROVIDER_virtual/cross-binutils:forcevariable = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/${MLPREFIX}cross-cc:class-target = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/${MLPREFIX}cross-c++:class-target = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/${MLPREFIX}compilerlibs:class-target = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/update-alternatives = "update-alternatives-none"

DISTRO_FEATURES:remove = "systemd ptest usrmerge"
INHERIT += "esp32s3-packageqa"
VIRTUAL-RUNTIME_update-alternatives = "update-alternatives-none"
DEPENDS:append:class-target = " xtensa-esp32s3-toolchain-native"
SECURITY_CFLAGS = ""
SECURITY_LDFLAGS = ""
FULL_OPTIMIZATION = "-Os -g0"
TARGET_LINK_HASH_STYLE = "-Wl,--hash-style=both"
VIRTUAL-RUNTIME_init_manager = "busybox"
VIRTUAL-RUNTIME_initscripts = "busybox"
VIRTUAL-RUNTIME_dev_manager = "busybox-mdev"

# Linked libgcc code retains DWARF paths from the Crosstool-NG build.
INSANE_SKIP:${PN}-dbg:append:class-target = " buildpaths"

PATH:prepend:class-target = "${XTENSA_EXTERNAL_TOOLCHAIN}/bin:"

export XTENSA_GNU_CONFIG

# The provider recipe stages the matching uClibc-ng target sysroot.
