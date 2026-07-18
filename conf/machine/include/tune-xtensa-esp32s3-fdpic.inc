# SPDX-License-Identifier: MIT

DEFAULTTUNE ?= "xtensa-esp32s3-fdpic"

TUNEVALID[xtensa] = "Xtensa architecture"
TUNEVALID[call0] = "Xtensa call0 ABI"
TUNEVALID[fdpic] = "FDPIC executable ABI"
TUNEVALID[nommu] = "No MMU"

AVAILTUNES += "xtensa-esp32s3-fdpic"
TUNE_FEATURES:tune-xtensa-esp32s3-fdpic = "xtensa call0 fdpic nommu"
TUNE_ARCH = "xtensa"
TUNE_PKGARCH = "xtensa-call0-fdpic"
PACKAGE_EXTRA_ARCHS:tune-xtensa-esp32s3-fdpic = "${TUNE_PKGARCH}"

MACHINEOVERRIDES =. "xtensa:xtensa-call0-fdpic:"
