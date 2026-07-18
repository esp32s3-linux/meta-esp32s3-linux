# SPDX-License-Identifier: MIT

def esp32s3_package_qa_machdata(machdata, d):
    machdata["linux-uclibcfdpic"] = {
        "xtensa": (94, 0, 0, True, 32),
    }
    return machdata

PACKAGEQA_EXTRA_MACHDEFFUNCS:append = " esp32s3_package_qa_machdata"
