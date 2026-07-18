# SPDX-License-Identifier: MIT

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:xtensa = " file://busybox.config"

do_prepare_config:prepend:xtensa() {
    install -m 0644 ${UNPACKDIR}/busybox.config ${UNPACKDIR}/defconfig
}
