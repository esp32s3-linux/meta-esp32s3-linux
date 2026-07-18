# SPDX-License-Identifier: MIT

SUMMARY = "External Xtensa ESP32-S3 FDPIC toolchain sysroot"
LICENSE = "CLOSED"

PROVIDES = "virtual/cross-binutils virtual/cross-cc virtual/cross-c++ virtual/compilerlibs virtual/libc virtual/libc-locale virtual/libiconv virtual/crypt"

INHIBIT_DEFAULT_DEPS = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

EXTERNAL_TARGET_SYSROOT = "${XTENSA_EXTERNAL_TOOLCHAIN}/${@d.getVar('TARGET_PREFIX').rstrip('-')}/sysroot"

python () {
    import os

    root = d.expand("${EXTERNAL_TARGET_SYSROOT}")
    if not os.path.isdir(root):
        bb.fatal("external Xtensa target sysroot not found: %s" % root)
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_stash_locale[noexec] = "1"
addtask stash_locale after do_install before do_populate_sysroot

do_install() {
    cp -a --no-preserve=ownership ${EXTERNAL_TARGET_SYSROOT}/. ${D}/
}

PACKAGES = "${PN} ${PN}-dev ${PN}-staticdev"
FILES:${PN} = "${base_libdir}/*.so.* ${base_libdir}/*-*.so ${base_sbindir} ${bindir} ${libdir}/*.so.*"
FILES:${PN}-dev = "${includedir} ${base_libdir}/*.so ${base_libdir}/*.o ${libdir}/*.so ${libdir}/*.o"
FILES:${PN}-staticdev = "${libdir}/*.a ${base_libdir}/*.a"
RDEPENDS:${PN}-dev = "${PN}"
RPROVIDES:${PN} = "ldconfig glibc-utils"
