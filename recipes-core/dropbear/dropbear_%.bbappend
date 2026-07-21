# SPDX-License-Identifier: MIT

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:xtensa = " file://0001-svr-support-no-MMU-builds-without-fork.patch"
