# SPDX-License-Identifier: MIT

LIBCEXTENSION = "-uclibc"
LIBCOVERRIDE = ":libc-uclibc"

PREFERRED_PROVIDER_virtual/libc = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/libc-locale = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/libiconv = "external-xtensa-esp32s3-toolchain"
PREFERRED_PROVIDER_virtual/libintl = "gettext"
PREFERRED_PROVIDER_virtual/crypt = "external-xtensa-esp32s3-toolchain"

LIBC_DEPENDENCIES = "external-xtensa-esp32s3-toolchain"
DISTRO_FEATURES_OPTED_OUT += "ldconfig"
