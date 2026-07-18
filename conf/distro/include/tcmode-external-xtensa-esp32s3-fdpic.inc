# SPDX-License-Identifier: MIT
#
# The existing ESP32-S3 compiler is an external Xtensa call0 FDPIC/uClibc-ng
# toolchain.  It needs the dynamic configuration both when it is built and when
# any of its binaries execute.

XTENSA_EXTERNAL_TOOLCHAIN ?= ""
XTENSA_GNU_CONFIG ?= ""

require conf/distro/include/tcmode-default.inc

TCMODE = "external-xtensa-esp32s3-fdpic"
TCLIBC = "uclibc"
TARGET_PREFIX = "xtensa-esp32s3-linux-uclibcfdpic-"
TARGET_VENDOR = "-esp32s3"
TARGET_OS = "linux-uclibcfdpic"

PREFERRED_TOOLCHAIN_TARGET = "external-xtensa-esp32s3"
PREFERRED_PROVIDER_virtual/cross-binutils:forcevariable = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/cross-cc:forcevariable = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/cross-c++:forcevariable = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/compilerlibs:forcevariable = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/update-alternatives = "update-alternatives-none"

DISTRO_FEATURES:remove = "systemd ptest usrmerge"
INHERIT += "esp32s3-packageqa"
VIRTUAL-RUNTIME_update-alternatives = "update-alternatives-none"
SECURITY_CFLAGS = ""
SECURITY_LDFLAGS = ""
FULL_OPTIMIZATION = "-Os -g0"
TARGET_LINK_HASH_STYLE = "-Wl,--hash-style=both"
VIRTUAL-RUNTIME_init_manager = "busybox"
VIRTUAL-RUNTIME_initscripts = "busybox"
VIRTUAL-RUNTIME_dev_manager = "busybox-mdev"

PATH:prepend:class-target = "${XTENSA_EXTERNAL_TOOLCHAIN}/bin:"

export XTENSA_GNU_CONFIG
BB_ENV_PASSTHROUGH_ADDITIONS:append = " XTENSA_GNU_CONFIG"

python () {
    import bb

    toolchain = d.getVar("XTENSA_EXTERNAL_TOOLCHAIN")
    dynconfig = d.getVar("XTENSA_GNU_CONFIG")
    if not toolchain:
        bb.fatal("XTENSA_EXTERNAL_TOOLCHAIN must name the existing Crosstool-NG output")
    if not dynconfig:
        bb.fatal("XTENSA_GNU_CONFIG must name the generated esp32s3.so")
}

# The external toolchain recipe stages the matching uClibc-ng sysroot for target
# recipes. The compiler and sysroot remain external, versioned inputs.
