# SPDX-License-Identifier: MIT

def siteinfo_xtensa_esp32s3_fdpic(archinfo, osinfo, targetinfo, d):
    archinfo["xtensa"] = "endian-little bit-32"
    return archinfo, osinfo, targetinfo

SITEINFO_EXTRA_DATAFUNCS:append = " siteinfo_xtensa_esp32s3_fdpic"
