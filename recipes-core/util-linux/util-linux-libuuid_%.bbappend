# SPDX-License-Identifier: MIT

# The established ESP32-S3 uClibc-ng ABI uses a signed 32-bit time_t.
EXTRA_OECONF:append:xtensa = " --disable-year2038"
CACHED_CONFIGUREVARS:append:xtensa = " ac_cv_func_reallocarray=yes"
