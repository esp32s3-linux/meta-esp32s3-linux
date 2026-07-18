# ESP32-S3 Linux BSP Layer

`meta-esp32s3-linux` provides a Yocto Wrynose BSP for the ESP32-S3 DevKitC-1.

The target runs Linux on ESP32-S3 core 1 using a no-MMU Xtensa `call0` FDPIC
ABI. ESP-Hosted firmware runs on core 0 and provides Wi-Fi to Linux.

## Target

| Property | Value |
|---|---|
| Machine | `esp32s3-devkitc-1-n16r8` |
| Architecture | Xtensa ESP32-S3 |
| Flash | 16 MB |
| PSRAM | 8 MB |
| ABI | `call0` FDPIC, no MMU |
| C library | uClibc-ng |
| Kernel | Linux 6.11 `xipImage` |
| Root filesystem | read-only CramFS |
| Writable configuration | JFFS2 mounted at `/etc` |
| Yocto release | Wrynose |

## Current Artifact Sources

- Yocto builds `xipImage` with the external Crosstool-NG Xtensa toolchain.
- Yocto builds the XIP CramFS root from target packages.
- The root image recipe derives `etc.jffs2` from its finalized package-built
  `/etc` and applies ESP32-S3 policy defaults.
- ESP-Hosted bootloader, application, and partition table are imported from a
  completed ESP-IDF build.

The external target toolchain and prebuilt ESP-Hosted firmware remain required.

## Required Layers

Add these Wrynose-compatible layers to `BBLAYERS`:

```text
poky/meta
meta-openembedded/meta-oe
meta-esp32s3-linux
```

The layer masks unsupported cross-Canadian SDK recipes. It does not currently
produce a conventional Yocto SDK.

## Local Configuration

Set the machine and local artifact paths in `build/conf/local.conf`. Do not add
machine-local absolute paths to this layer.

```bitbake
MACHINE = "esp32s3-devkitc-1-n16r8"
TCMODE = "external-xtensa-esp32s3-fdpic"

XTENSA_EXTERNAL_TOOLCHAIN = "/path/to/xtensa-esp32s3-linux-uclibcfdpic"
XTENSA_GNU_CONFIG = "/path/to/xtensa-dynconfig/esp32s3.so"

ESP_HOSTED_BUILD_DIR = "/path/to/network_adapter/build"
```

The Xtensa compiler and binutils require `XTENSA_GNU_CONFIG` whenever they run.
The layer's external toolchain mode exports it into BitBake tasks.

The upstream kernel branch is no longer reliably fetchable by its historical
symbolic revision. A known-good Buildroot `git4` archive can be selected locally:

```bitbake
SRC_URI:pn-linux-xtensa = "file:///path/to/linux-xtensa-6.11-esp32-tag-git4.tar.gz \
                          file://devkit_c1_8m_linux.config"
S:pn-linux-xtensa = "${UNPACKDIR}/linux-xtensa-6.11-esp32-tag"
```

## Build

From the Yocto workspace, initialize the environment and build the aggregate
flash bundle:

```sh
source .env
bitbake esp32s3-flash-bundle
```

The workspace helper provides the equivalent shorter command:

```sh
./bb esp32s3-flash-bundle
```

Individual targets are also available:

```sh
./bb virtual/kernel
./bb esp32s3-minimal-image
./bb esp-hosted-firmware
```

## Outputs

Artifacts are deployed below:

```text
build/tmp/deploy/images/esp32s3-devkitc-1-n16r8/
```

The complete bundle is:

```text
esp32s3-devkitc-1-n16r8-flash-bundle/
  bootloader.bin
  network_adapter.bin
  partition-table.bin
  flasher_args.json
  xipImage
  rootfs.cramfs
  etc.jffs2
  data.jffs2
  flash-esp32s3-linux.py
```

The flashing script validates image sizes against `partition-table.bin`. For
the N16R8 layout:

| Partition | Offset | Size |
|---|---:|---:|
| `etc` | `0x000b0000` | `0x00070000` |
| `linux` | `0x00120000` | `0x00460000` |
| `rootfs` | `0x00580000` | `0x00540000` |
| `data` | `0x00ac0000` | `0x00400000` |

`etc.jffs2` is padded to the full `etc` partition size. This ensures flashing a
smaller replacement filesystem does not leave stale JFFS2 nodes in later erase
blocks.

`data.jffs2` initializes a 4 MB writable filesystem mounted at `/data`. Add
`--preserve-data` when reflashing a device whose existing `/data` contents must
be retained.

## Flash

Install `esptool`, then run the layer's flash helper with the complete bundle:

```sh
python3 -m pip install --user esptool

python3 /path/to/esp32s3-devkitc-1-n16r8-flash-bundle/flash-esp32s3-linux.py \
  --port /dev/ttyUSB0 \
  --bundle /path/to/esp32s3-devkitc-1-n16r8-flash-bundle
```

On macOS, the port is typically `/dev/tty.usbserial-*` or
`/dev/cu.usbmodem*`.

Add `--preserve-etc` only when the existing `/etc` partition is known to be
compatible. Omit it after changing account files, mount layout, or JFFS2 image
generation.

The script reads firmware offsets from `flasher_args.json` and Linux partition
offsets from `partition-table.bin`. It also recognizes ESP-IDF's `0xEBEB`
partition-table terminator.

## Login

The serial console login is:

```text
login: root
password: press Enter
```

The empty root password matches the current Buildroot reference and is intended
only for bring-up. Configure authentication before exposing network login.

## Wi-Fi

The kernel, package-built root filesystem, and ESP-Hosted firmware provide the
`espsta0` interface, `wpa_supplicant`, `iw`, and DHCP client. Wi-Fi starts from
`/etc/init.d/S40network` after credentials are configured.

Edit `/etc/wpa_supplicant.conf` on the target, replace the example values, and
set `disabled=0`:

```text
network={
    ssid="YOUR_SSID"
    psk="YOUR_PASSPHRASE"
    disabled=0
}
```

Apply the configuration without rebooting:

```sh
/etc/init.d/S40network restart
iw dev espsta0 link
ip addr show espsta0
```

The passphrase is stored in the writable `/etc` JFFS2 partition. Restrict
physical and serial access, or replace the plaintext passphrase with the PSK
generated by `wpa_passphrase` on a development host.

## Persistent `/etc`

The package-built CramFS root provides the bootstrap init environment. Early
userspace reads its `fstab` and mounts the JFFS2 `etc` partition on `/etc`.

`esp32s3-minimal-image` generates `etc.jffs2` from its finalized package-built
`/etc`, after applying the BSP defaults from `esp32s3-etc-defaults`. Package
files such as DHCP hooks therefore enter both the bootstrap CramFS and writable
JFFS2 image automatically. The JFFS2 image root directly becomes `/etc`; it
does not contain an extra `etc/` directory.

## Verification

Before flashing, run:

```sh
./bb -p
./bb esp32s3-flash-bundle
python3 -m py_compile \
  meta-esp32s3-linux/recipes-bsp/esp32s3-flash/files/flash-esp32s3-linux.py
```

Keep `xipImage`, `rootfs.cramfs`, and `etc.jffs2` within the sizes declared by
the generated partition table. The flash helper rejects oversized images.
