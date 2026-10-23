#!/usr/bin/env python3
"""Replace bitchat branding with Yarik while preserving wire-format tokens."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SKIP_DIR_NAMES = {
    ".git",
    ".gradle",
    "build",
    ".idea",
    "node_modules",
}

SKIP_FILE_NAMES = {
    "lint-baseline.xml",
    "rebrand-yarik.py",
}

# Placeholders for protected substrings (longest first).
PROTECTED = [
    "bitchat1:",
    "com.bitchat.",
    "BitchatFilePacket",
    "BitchatMessageType",
    "BitchatMessage",
    "BitchatPacket",
]

PLACEHOLDER_PREFIX = "\0YARIK_PROTECT_"

REPLACEMENTS = [
    # Env / release tooling
    ("BITCHAT_", "YARIK_"),
    ("bitchat-android-arti-builder", "yarik-android-arti-builder"),
    ("bitchat-android", "yarik-android"),
    # Theme / UI branding (order matters: longer tokens first)
    ("BitChatBrandButton", "YarikBrandButton"),
    ("BitChatIcon", "YarikIcon"),
    ("BitchatWatchApplication", "YarikWatchApplication"),
    ("BitchatSheetTopBar", "YarikSheetTopBar"),
    ("BitchatBottomSheet", "YarikBottomSheet"),
    ("DarkBitchatColorScheme", "DarkYarikColorScheme"),
    ("LightBitchatColorScheme", "LightYarikColorScheme"),
    ("DarkBitchatPalette", "DarkYarikPalette"),
    ("LightBitchatPalette", "LightYarikPalette"),
    ("LocalBitchatPalette", "LocalYarikPalette"),
    ("BitchatFontFamily", "YarikFontFamily"),
    ("BitchatMotion", "YarikMotion"),
    ("BitchatPalette", "YarikPalette"),
    ("BitchatTheme", "YarikTheme"),
    ("BitchatSheetCenterTopBar", "YarikSheetCenterTopBar"),
    ("BitchatSheetTitle", "YarikSheetTitle"),
    ("BitchatWearTypography", "YarikWearTypography"),
    ("BitchatWearColorScheme", "YarikWearColorScheme"),
    ("BitchatWearTheme", "YarikWearTheme"),
    ("BitchatBadge", "YarikBadge"),
    ("NostrEmbeddedBitChat", "NostrEmbeddedYarik"),
    ("NotBitchat", "NotYarikApk"),
    # String resource keys
    ("share_bitchat_", "share_yarik_"),
    ("install_bitchat_", "install_yarik_"),
    ("prepare_apk_error_not_bitchat", "prepare_apk_error_not_yarik"),
    ("notification_action_quit_bitchat", "notification_action_quit_yarik"),
    # Channels / prefs
    ("bitchat_watch_messages", "yarik_watch_messages"),
    ("bitchat_mesh_service", "yarik_mesh_service"),
    ("bitchat_permissions", "yarik_permissions"),
    # Product name in prose / comments
    ("BitChat-over-Nostr", "Yarik-over-Nostr"),
    ("BitChat packet", "Yarik packet"),
    ("BitChat Packets", "Yarik packets"),
    ("BitChat protocol", "Yarik protocol"),
    ("BitChat packets", "Yarik packets"),
    ("BitChat Packets", "Yarik packets"),
    ("BitChat", "Yarik"),
    ("bitchat protocol", "Yarik protocol"),
    ("bitchat client", "Yarik client"),
    ("bitchat app", "Yarik app"),
    ("bitchat style", "Yarik style"),
    ("bitchat-android", "yarik-android"),
    ("bitchat wordmark", "Yarik wordmark"),
    ("for bitchat", "for Yarik"),
    ("the bitchat", "the Yarik"),
    ("Download BitChat", "Download Yarik"),
    # APK path alias (keep wire compat: also handled in ApkWebServer manually)
    ('"/bitchat.apk"', '"/yarik.apk"'),
    ("'/bitchat.apk'", "'/yarik.apk'"),
    ("|| uri == \"/bitchat.apk\"", '|| uri == "/yarik.apk"'),
]

TEXT_SUFFIXES = {
    ".kt",
    ".kts",
    ".xml",
    ".md",
    ".properties",
    ".pro",
    ".sh",
    ".py",
    ".rs",
    ".csv",
    ".json",
    ".yml",
    ".yaml",
    ".gradle",
    ".txt",
    ".html",
}


def should_process(path: Path) -> bool:
    if path.name in SKIP_FILE_NAMES:
        return False
    for part in path.parts:
        if part in SKIP_DIR_NAMES:
            return False
    if path.suffix.lower() in TEXT_SUFFIXES or path.name in {
        "gradle.properties",
        "AGENTS.md",
        "PRIVACY_POLICY.md",
    }:
        return True
    return False


def protect(text: str) -> tuple[str, list[str]]:
    saved: list[str] = []
    for i, token in enumerate(PROTECTED):
        placeholder = f"{PLACEHOLDER_PREFIX}{i}\x00"
        if token in text:
            saved.append((placeholder, token))
            text = text.replace(token, placeholder)
    return text, saved


def restore(text: str, saved: list[tuple[str, str]]) -> str:
    for placeholder, token in saved:
        text = text.replace(placeholder, token)
    return text


def transform(content: str) -> str:
    content, saved = protect(content)
    for old, new in REPLACEMENTS:
        content = content.replace(old, new)
    return restore(content, saved)


def main() -> int:
    changed: list[Path] = []
    for path in ROOT.rglob("*"):
        if not path.is_file() or not should_process(path):
            continue
        try:
            original = path.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        updated = transform(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed.append(path)
    print(f"Updated {len(changed)} files")
    for p in sorted(changed):
        print(p.relative_to(ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
