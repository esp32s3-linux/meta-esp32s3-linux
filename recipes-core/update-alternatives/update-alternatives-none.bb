# SPDX-License-Identifier: MIT

SUMMARY = "Empty update-alternatives provider for immutable ESP32-S3 images"
LICENSE = "MIT"

PROVIDES = "virtual/update-alternatives"
INHIBIT_DEFAULT_DEPS = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install() {
    install -d ${D}${sbindir}
    printf '#!/bin/sh\nexit 0\n' > ${D}${sbindir}/update-alternatives
    chmod 0755 ${D}${sbindir}/update-alternatives
}

RPROVIDES:${PN} = "update-alternatives"
