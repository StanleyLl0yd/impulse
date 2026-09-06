#!/usr/bin/env python3
import argparse
import binascii
import hashlib
import struct
import zlib
from pathlib import Path

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
EXPECTED_SIZE = (432, 432)
CANONICAL = Path("assets/app-icon/impulse-launcher-canonical.png")
RUNTIME = Path("app/src/main/res/drawable-nodpi/ic_launcher_foreground.png")


def fail(path: Path, message: str) -> None:
    raise SystemExit(f"{path}: {message}")


def validate_png(path: Path) -> str:
    data = path.read_bytes()
    if not data.startswith(PNG_SIGNATURE):
        fail(path, "invalid PNG signature")

    offset = len(PNG_SIGNATURE)
    width = height = None
    idat = []
    saw_ihdr = False
    saw_iend = False

    while offset < len(data):
        if offset + 12 > len(data):
            fail(path, "truncated PNG chunk header")

        length = struct.unpack(">I", data[offset:offset + 4])[0]
        chunk_type = data[offset + 4:offset + 8]
        chunk_end = offset + 12 + length
        if chunk_end > len(data):
            fail(path, f"truncated {chunk_type.decode('ascii', 'replace')} chunk")

        chunk_data = data[offset + 8:offset + 8 + length]
        expected_crc = struct.unpack(">I", data[offset + 8 + length:chunk_end])[0]
        actual_crc = binascii.crc32(chunk_type)
        actual_crc = binascii.crc32(chunk_data, actual_crc) & 0xFFFFFFFF
        if actual_crc != expected_crc:
            fail(path, f"CRC mismatch in {chunk_type.decode('ascii', 'replace')} chunk")

        if chunk_type == b"IHDR":
            if saw_ihdr or length != 13:
                fail(path, "invalid IHDR")
            saw_ihdr = True
            width, height, bit_depth, color_type, compression, filtering, interlace = struct.unpack(
                ">IIBBBBB", chunk_data
            )
            if (width, height) != EXPECTED_SIZE:
                fail(path, f"expected {EXPECTED_SIZE[0]}x{EXPECTED_SIZE[1]}, got {width}x{height}")
            if (bit_depth, color_type, compression, filtering, interlace) != (8, 2, 0, 0, 0):
                fail(path, "unexpected PNG encoding; expected 8-bit non-interlaced RGB")
        elif chunk_type == b"IDAT":
            idat.append(chunk_data)
        elif chunk_type == b"IEND":
            if length != 0:
                fail(path, "invalid IEND")
            saw_iend = True
            offset = chunk_end
            if offset != len(data):
                fail(path, "trailing bytes after IEND")
            break

        offset = chunk_end

    if not saw_ihdr or not saw_iend or width is None or height is None or not idat:
        fail(path, "missing required PNG chunks")

    decoder = zlib.decompressobj()
    pixels = decoder.decompress(b"".join(idat)) + decoder.flush()
    if not decoder.eof or decoder.unused_data:
        fail(path, "invalid or trailing compressed pixel stream")

    row_bytes = 1 + width * 3
    expected_bytes = height * row_bytes
    if len(pixels) != expected_bytes:
        fail(path, f"decoded scanline size mismatch: expected {expected_bytes}, got {len(pixels)}")

    for row in range(height):
        filter_type = pixels[row * row_bytes]
        if filter_type > 4:
            fail(path, f"invalid PNG filter type {filter_type} on row {row}")

    return hashlib.sha256(data).hexdigest()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", nargs="*", type=Path)
    args = parser.parse_args()

    if args.paths:
        for path in args.paths:
            digest = validate_png(path)
            print(f"{path}: OK sha256={digest}")
        return

    canonical_digest = validate_png(CANONICAL)
    runtime_digest = validate_png(RUNTIME)
    if CANONICAL.read_bytes() != RUNTIME.read_bytes():
        fail(RUNTIME, "launcher resource must be byte-identical to the canonical PNG")

    print(f"launcher artwork: OK sha256={canonical_digest}")
    if canonical_digest != runtime_digest:
        raise SystemExit("launcher digest mismatch")


if __name__ == "__main__":
    main()
