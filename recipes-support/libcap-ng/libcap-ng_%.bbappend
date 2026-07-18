# SPDX-License-Identifier: MIT

do_install:prepend:xtensa() {
    if [ ! -e ${B}/src/.libs/libcap-ng.so.0.0.0 ]; then
        touch ${B}/.libcap-ng-static-only
        install -d ${D}${libdir}
        touch ${D}${libdir}/libcap-ng.so.0.0.0
    fi
}

do_install:append:xtensa() {
    if [ -e ${B}/.libcap-ng-static-only ]; then
        rm -f ${D}${base_libdir}/libcap-ng.so.0.0.0 ${D}${libdir}/libcap-ng.so
    fi
}
