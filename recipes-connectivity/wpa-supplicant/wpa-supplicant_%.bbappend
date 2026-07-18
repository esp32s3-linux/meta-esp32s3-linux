# SPDX-License-Identifier: MIT

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:xtensa = " file://0001-os_unix-use-vfork-on-no-MMU-targets.patch"

DEPENDS:remove:xtensa = "dbus"
PACKAGECONFIG:xtensa = ""
DISABLE_STATIC:xtensa = "1"

do_configure:append:xtensa() {
    sed -i \
        -e '/^CONFIG_CTRL_IFACE=/d' \
        -e '/^CONFIG_CTRL_IFACE_DBUS_NEW=/d' \
        -e '/^CONFIG_DRIVER_WEXT=/d' \
        -e '/^CONFIG_EAP/d' \
        -e '/^CONFIG_IEEE8021X_EAPOL=/d' \
        -e '/^CONFIG_FILS=/d' \
        -e '/^CONFIG_WPS=/d' \
        -e '/^CONFIG_DPP=/d' \
        -e '/^CONFIG_SAE=/d' \
        -e '/^CONFIG_SAE_PK=/d' \
        -e '/^CONFIG_OWE=/d' \
        wpa_supplicant/.config
    echo 'CONFIG_TLS=internal' >> wpa_supplicant/.config
    echo 'CONFIG_INTERNAL_LIBTOMMATH=y' >> wpa_supplicant/.config
    echo 'CONFIG_INTERNAL_LIBTOMMATH_FAST=y' >> wpa_supplicant/.config
    echo 'CONFIG_LIBNL32=y' >> wpa_supplicant/.config
}

RRECOMMENDS:${PN}:xtensa = ""
