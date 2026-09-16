#!/usr/bin/env bash
# Build samples/hello.zig into an Android .so for Gypsum import.
# Usage: ./samples/build-zig-android.sh [arm64-v8a|x86_64]

set -euo pipefail

ABI="${1:-arm64-v8a}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

case "$ABI" in
  arm64-v8a) TARGET="aarch64-linux-android31" ;;
  x86_64) TARGET="x86_64-linux-android31" ;;
  *) echo "Unsupported ABI: $ABI" >&2; exit 1 ;;
esac

OUT="$ROOT/samples/hello-zig-$ABI.so"
zig build-lib "$ROOT/samples/hello.zig" \
  -target "$TARGET" \
  -dynamic \
  -O ReleaseFast \
  -femit-bin="$OUT"

echo "Built $OUT"
echo "Import this .so into Gypsum (must export gypsum_main)."
