#!/usr/bin/env python3
import re
from pathlib import Path

ROOTS = (Path(".github/workflows"), Path(".github/actions"))
ACTION_REF = re.compile(r"^\s*uses:\s*([^\s#]+)")
IMAGE = re.compile(r"^\s*image:\s*([^\s#]+)")
FULL_SHA = re.compile(r"^[0-9a-f]{40}$")
DIGEST = re.compile(r"@sha256:[0-9a-f]{64}$")

errors = []

for root in ROOTS:
    for path in sorted(root.rglob("*")):
        if path.suffix not in {".yml", ".yaml"}:
            continue
        text = path.read_text(encoding="utf-8")
        if "pull_request_target:" in text:
            errors.append(f"{path}: pull_request_target is forbidden")
        if "persist-credentials: true" in text:
            errors.append(f"{path}: checkout credentials must not persist")
        if "secrets: inherit" in text:
            errors.append(f"{path}: secrets inheritance is forbidden")
        if path.parent == Path(".github/workflows") and not re.search(
            r"(?m)^permissions:\s*(?:\{\}|$)", text
        ):
            errors.append(f"{path}: explicit workflow permissions are required")

        lines = text.splitlines()
        for number, line in enumerate(lines, start=1):
            action = ACTION_REF.match(line)
            if action:
                target = action.group(1)
                if target.startswith("./") or target.startswith("docker://"):
                    continue
                if "@" not in target:
                    errors.append(f"{path}:{number}: action is not pinned")
                    continue
                _, ref = target.rsplit("@", 1)
                if not FULL_SHA.fullmatch(ref):
                    errors.append(f"{path}:{number}: action ref must be a full 40-character SHA")

            image = IMAGE.match(line)
            if image:
                target = image.group(1)
                if target.startswith("${{"):
                    continue
                if not DIGEST.search(target):
                    errors.append(f"{path}:{number}: container image must be pinned by sha256 digest")

if errors:
    raise SystemExit("\n".join(errors))

print("GitHub Actions supply-chain policy: OK")
