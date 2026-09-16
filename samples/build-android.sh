#!/usr/bin/env bash
# Build a C source file into an Android executable for Gypsum import.
# Usage: ./samples/build-android.sh path/to/app.c [arm64-v8a|x86_64]

set -euo pipefail

SOURCE="${1:?Usage: build-android.sh source.c [abi]}"
ABI="${2:-arm64-v8a}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="$ROOT/local.properties"

SDK="$(grep '^sdk.dir=' "$PROPS" | cut -d= -f2)"
NDK="$(ls -d "$SDK/ndk/"* 2>/dev/null | sort -V | tail -1)"
CLANG="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/clang"

case "$ABI" in
  arm64-v8a) TRIPLE="aarch64-linux-android30" ;;
  x86_64) TRIPLE="x86_64-linux-android30" ;;
  *) echo "Unsupported ABI: $ABI" >&2; exit 1 ;;
esac

OUT="${SOURCE%.c}-$ABI"
"$CLANG" --target="$TRIPLE" -fPIE -pie -O2 -o "$OUT" "$SOURCE"
echo "Built $OUT"
