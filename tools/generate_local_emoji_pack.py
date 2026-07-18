#!/usr/bin/env python3
"""Build the client-side local emoji folder from Emojiful's former URL sources."""

import argparse
import json
import re
import shutil
import tempfile
import zipfile
from pathlib import Path

import yaml


def safe_filename(value: str) -> str:
    value = value.lower().replace(" ", "_")
    value = re.sub(r"[^a-z0-9._+\-]", "_", value)
    return re.sub(r"_+", "_", value).strip("_.") or "emoji"


def safe_category(value: str) -> str:
    return re.sub(r'[<>:"/\\|?*]', "_", value).strip() or "Autres"


def write_emoji(root: Path, category: str, filename: str, source: Path, metadata: dict) -> None:
    folder = root / "emojiful" / "emojis" / safe_category(category)
    folder.mkdir(parents=True, exist_ok=True)
    image = folder / filename
    shutil.copyfile(source, image)
    image.with_suffix(".json").write_text(
        json.dumps(metadata, ensure_ascii=False, separators=(",", ":")), encoding="utf-8"
    )


def add_twemoji(root: Path, emoji_data: Path) -> int:
    entries = json.loads((emoji_data / "emoji.json").read_text(encoding="utf-8"))
    used_names: dict[str, set[str]] = {}
    count = 0
    for entry in entries:
        if not entry.get("has_img_twitter"):
            continue
        source = emoji_data / "img-twitter-64" / entry["image"]
        if not source.is_file():
            raise FileNotFoundError(f"Missing Twemoji image: {source}")

        category = entry["category"]
        names = used_names.setdefault(category, set())
        stem = safe_filename(entry["short_name"])
        candidate = stem
        suffix = 2
        while candidate in names:
            candidate = f"{stem}_{suffix}"
            suffix += 1
        names.add(candidate)

        aliases = [f":{name}:" for name in entry.get("short_names", [])]
        if ":face_with_symbols_on_mouth:" in aliases:
            aliases.append(":swear:")
        write_emoji(root, category, candidate + source.suffix.lower(), source, {
            "name": entry["short_name"],
            "aliases": aliases,
            "texts": entry.get("texts") or [],
            "sort": int(entry.get("sort_order", 0)),
        })
        count += 1
    return count


def add_custom_emojis(root: Path, assets: Path) -> int:
    categories = yaml.safe_load((assets / "Categories.yml").read_text(encoding="utf-8"))
    count = 0
    for category_file in categories:
        category = Path(category_file).stem
        entries = yaml.safe_load((assets / category_file).read_text(encoding="utf-8")) or []
        for index, entry in enumerate(entries):
            source = assets / entry["location"]
            if not source.is_file():
                raise FileNotFoundError(f"Missing custom emoji image: {source}")
            stem = safe_filename(entry["name"])
            write_emoji(root, category, stem + source.suffix.lower(), source, {
                "name": entry["name"],
                "aliases": entry.get("strings") or [f":{entry['name']}:"],
                "texts": entry.get("texts") or [],
                "sort": int(entry.get("sort", index)),
            })
            count += 1
    return count


def build(output: Path, emoji_data: Path, custom_assets: Path) -> None:
    with tempfile.TemporaryDirectory(prefix="emojiful-local-") as temporary:
        root = Path(temporary)
        twemoji_count = add_twemoji(root, emoji_data)
        custom_count = add_custom_emojis(root, custom_assets)
        readme = root / "emojiful" / "README.txt"
        readme.parent.mkdir(parents=True, exist_ok=True)
        readme.write_text(
            "TFOT Emojiful local emoji pack for Minecraft Forge 1.20.1\n"
            f"Contains {twemoji_count} Twemoji and {custom_count} custom emojis.\n"
            "Extract the emojiful folder into the Minecraft game directory.\n"
            "The mod and this folder are only required on the client.\n",
            encoding="utf-8",
        )

        output.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
            for path in sorted(root.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(root).as_posix())
    print(f"Created {output} ({twemoji_count + custom_count} emojis)")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--emoji-data", required=True, type=Path)
    parser.add_argument("--custom-assets", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    build(args.output, args.emoji_data, args.custom_assets)


if __name__ == "__main__":
    main()
