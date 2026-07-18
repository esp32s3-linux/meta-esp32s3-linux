# SPDX-License-Identifier: MIT

SUMMARY = "ESP32-S3 writable /etc defaults"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://fstab file://profile file://inittab file://securetty file://rcS file://rcK \
           file://interfaces file://wpa_supplicant.conf file://S40network"

S = "${UNPACKDIR}"

do_install() {
    root=${D}${datadir}/esp32s3-etc-defaults
    install -d $root/init.d $root/network/interfaces.d \
        $root/network/if-pre-up.d $root/network/if-up.d \
        $root/network/if-down.d $root/network/if-post-down.d
    install -m 0644 ${S}/fstab $root/fstab
    install -m 0644 ${S}/profile $root/profile
    install -m 0644 ${S}/inittab $root/inittab
    install -m 0600 ${S}/securetty $root/securetty
    install -m 0755 ${S}/rcS $root/init.d/rcS
    install -m 0755 ${S}/rcK $root/init.d/rcK
    install -m 0644 ${S}/interfaces $root/network/interfaces
    install -m 0600 ${S}/wpa_supplicant.conf $root/wpa_supplicant.conf
    install -m 0755 ${S}/S40network $root/init.d/S40network
}

FILES:${PN} = "${datadir}/esp32s3-etc-defaults"
