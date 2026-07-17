#!/usr/bin/env python3
"""Build the server datapack that replaces Emojiful's runtime URL downloads."""

import argparse
import json
import re
import shutil
import tempfile
import zipfile
from pathlib import Path

import yaml


PACK_FORMAT = 15  # Minecraft 1.20-1.20.1
NAMESPACE = "tfot_emojis"


def safe_path(value: str) -> str:
    value = value.lower().replace(" ", "_")
    value = re.sub(r"[^a-z0-9/._-]", "_", value)
    return re.sub(r"_+", "_", value).strip("_.") or "emoji"


def write_recipe(root: Path, relative_name: str, recipe: dict) -> None:
    path = root / "data" / NAMESPACE / "recipes" / f"{relative_name}.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(recipe, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")


def copy_texture(root: Path, source: Path, relative_name: str) -> str:
    target = root / "data" / NAMESPACE / "emoji" / relative_name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    return f"{NAMESPACE}:emoji/{relative_name}"


def add_twemoji(root: Path, emoji_data: Path) -> int:
    entries = json.loads((emoji_data / "emoji.json").read_text(encoding="utf-8"))
    used_names: set[str] = set()
    count = 0
    for entry in entries:
        if not entry.get("has_img_twitter"):
            continue
        source = emoji_data / "img-twitter-64" / entry["image"]
        if not source.is_file():
            raise FileNotFoundError(f"Missing Twemoji image: {source}")

        recipe_name = safe_path(entry["short_name"])
        base_name = recipe_name
        suffix = 2
        while recipe_name in used_names:
            recipe_name = f"{base_name}_{suffix}"
            suffix += 1
        used_names.add(recipe_name)

        strings = [f":{name}:" for name in entry.get("short_names", [])]
        if ":face_with_symbols_on_mouth:" in strings:
            strings.append(":swear:")
        texts = entry.get("texts") or []
        texture_name = f"twemoji/{safe_path(entry['image'])}"
        texture = copy_texture(root, source, texture_name)
        write_recipe(root, f"twemoji/{recipe_name}", {
            "type": "emojiful:emoji_recipe",
            "category": entry["category"],
            "name": entry["short_name"],
            "texture": texture,
            "strings": strings,
            "texts": texts,
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
            extension = source.suffix.lower()
            recipe_name = safe_path(entry["name"])
            texture_name = f"custom/{safe_path(category)}/{recipe_name}{extension}"
            texture = copy_texture(root, source, texture_name)
            write_recipe(root, f"custom/{safe_path(category)}/{recipe_name}_{index}", {
                "type": "emojiful:emoji_recipe",
                "category": category,
                "name": entry["name"],
                "texture": texture,
                "strings": entry.get("strings") or [f":{entry['name']}:"],
                "texts": entry.get("texts") or [],
                "sort": int(entry.get("sort", index)),
            })
            count += 1
    return count


def build(output: Path, emoji_data: Path, custom_assets: Path) -> None:
    with tempfile.TemporaryDirectory(prefix="emojiful-datapack-") as temporary:
        root = Path(temporary)
        twemoji_count = add_twemoji(root, emoji_data)
        custom_count = add_custom_emojis(root, custom_assets)

        (root / "pack.mcmeta").write_text(json.dumps({
            "pack": {
                "pack_format": PACK_FORMAT,
                "description": "TFOT Emojiful - emojis locaux synchronisés par le serveur",
            }
        }, ensure_ascii=False, indent=2), encoding="utf-8")
        (root / "README.txt").write_text(
            "TFOT Emojiful datapack for Minecraft Forge 1.20.1\n"
            f"Contains {twemoji_count} Twemoji and {custom_count} custom emojis.\n"
            "Place this ZIP in <world>/datapacks, then run /reload.\n"
            "No emoji image is downloaded from an URL while the game is running.\n",
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
