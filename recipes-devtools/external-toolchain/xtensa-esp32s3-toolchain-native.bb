# SPDX-License-Identifier: MIT

SUMMARY = "Crosstool-NG Xtensa ESP32-S3 FDPIC toolchain"
LICENSE = "CLOSED"

SRC_URI = "git://github.com/jcmvbkbc/crosstool-NG.git;protocol=https;branch=xtensa-fdpic;destsuffix=crosstool-NG;name=ctng \
           git://github.com/jcmvbkbc/xtensa-dynconfig.git;protocol=https;branch=original;destsuffix=xtensa-dynconfig;name=dynconfig \
           git://github.com/jcmvbkbc/config-esp32s3.git;protocol=https;branch=master;destsuffix=esp32s3;name=config"
SRCREV_ctng = "bec302a0d3a095521d9c3bccd741b6466becdee0"
SRCREV_dynconfig = "6615c332f41ab2169bd68ff289c351181251a6fc"
SRCREV_config = "f595d8653421eedced6bbe8f7822783d2fe2716d"
SRCREV_FORMAT = "ctng_dynconfig_config"

DEPENDS = "autoconf-native automake-native bison-native coreutils-native flex-native git-native gperf-native help2man-native libtool-native ncurses-native perl-native rsync-native texinfo-native unzip-native wget-native"

inherit native

S = "${UNPACKDIR}/crosstool-NG"
CTNG_TARGET = "xtensa-esp32s3-linux-uclibcfdpic"
TOOLCHAIN_DIR = "${B}/builds/${CTNG_TARGET}"
INHIBIT_SYSROOT_STRIP = "1"
SYSROOT_DIRS:append = " ${RECIPE_SYSROOT_NATIVE}/sysroot-only"

do_configure() {
    cd ${S}
    ./bootstrap
    SED=/usr/bin/sed ./configure --enable-local
}

# Crosstool-NG checks out the component branches selected by its ESP32-S3 sample.
do_compile[network] = "1"
do_compile() {
    cd ${S}
    export SED=/usr/bin/sed
    oe_runmake

    oe_runmake -C ${UNPACKDIR}/xtensa-dynconfig \
        ORIG=1 \
        CONF_DIR=${UNPACKDIR} \
        esp32s3.so
    export XTENSA_GNU_CONFIG=${UNPACKDIR}/xtensa-dynconfig/esp32s3.so

    install -d ${B}/home
    ./ct-ng ${CTNG_TARGET}
    env -u CC -u CXX -u CFLAGS -u CXXFLAGS \
        -u CPP -u CPPFLAGS -u LD -u LDFLAGS -u AS -u AR -u NM \
        -u RANLIB -u STRIP -u OBJCOPY -u OBJDUMP -u READELF \
        HOME=${B}/home \
        PATH=${PATH} \
        SED=/usr/bin/sed \
        XTENSA_GNU_CONFIG=${UNPACKDIR}/xtensa-dynconfig/esp32s3.so \
        CT_PREFIX=${B}/builds \
        ./ct-ng build
}

do_install() {
    install -d ${D}${RECIPE_SYSROOT_NATIVE}/sysroot-only/xtensa-esp32s3-toolchain
    cp -a --no-preserve=ownership ${TOOLCHAIN_DIR}/. \
        ${D}${RECIPE_SYSROOT_NATIVE}/sysroot-only/xtensa-esp32s3-toolchain/
    install -Dm0755 ${UNPACKDIR}/xtensa-dynconfig/esp32s3.so \
        ${D}${RECIPE_SYSROOT_NATIVE}/sysroot-only/xtensa-esp32s3-toolchain/xtensa-dynconfig/esp32s3.so
}

INSANE_SKIP:${PN} += "already-stripped file-rdeps"
