# SPDX-License-Identifier: GPL-2.0-or-later

SUMMARY = "CramFS tools with XIP filesystem support"
HOMEPAGE = "https://github.com/npitre/cramfs-tools"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=393a5ca445f6965873eca0259a17f833"

SRC_URI = "git://github.com/npitre/cramfs-tools.git;protocol=https;branch=master"
SRCREV = "bf888f2dd0a96cc804ec7d656ecbbe7354ac0497"

DEPENDS = "zlib"

EXTRA_OEMAKE = "CC='${CC}' CFLAGS='${CFLAGS}' LDFLAGS='${LDFLAGS}'"

do_install() {
    install -Dm 0755 ${S}/mkcramfs ${D}${bindir}/mkcramfs
    install -Dm 0755 ${S}/cramfsck ${D}${bindir}/cramfsck
}

BBCLASSEXTEND = "native"
