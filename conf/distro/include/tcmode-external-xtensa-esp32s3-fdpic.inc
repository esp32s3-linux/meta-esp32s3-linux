# SPDX-License-Identifier: MIT
#
# The existing ESP32-S3 compiler is an external Xtensa call0 FDPIC/uClibc-ng
# toolchain.  It needs the dynamic configuration both when it is built and when
# any of its binaries execute.

XTENSA_EXTERNAL_TOOLCHAIN ?= ""
XTENSA_GNU_CONFIG ?= ""

require conf/distro/include/tcmode-default.inc

TCMODE = "external-xtensa-esp32s3-fdpic"
TARGET_PREFIX = "xtensa-esp32s3-linux-uclibcfdpic-"
TARGET_OS = "uclinux-uclibc"

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

# This is deliberately only an environment and prefix declaration.  The native
# cross, libc, and package ABI integration must be implemented before builds.
