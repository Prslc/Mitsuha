<div align="center">

# Mitsuha

</div>

## Overview

Mitsuha is an Xposed module tailored for Xiaomi devices running MIUI / HyperOS (tested on OS1 / Android 14).  
It quietly tweaks system behavior to give you a smoother, cleaner experience without modifying APKs.

## Notes

- **Requires Xposed API 101** — use a compatible framework such as LSPosed.
- Only tested on **OS1 (Android 14)**; compatibility with other MIUI / HyperOS versions may vary.
- Use at your own risk – while it’s designed to be safe, system modifications always carry a small chance of instability.

## Features

- **Block system OTA updates** – intercepts update checks and prevents the annoying “update available” reminder.
- **Fix predictive back gesture progress** – HyperOS's back animation progress curve is broken; replaces it with AOSP's two-phase (linear + lerp) mapping.
- **Safety‑detector bypass** – dynamically patches the task dispatch field in MiSafetyDetectService, preventing unwanted restrictions.

## Credit

- **[LSPosed](https://github.com/LSPosed/LSPosed)** – The modern Xposed framework that makes this module possible.
- **[DexKit](https://github.com/LuckyPray/DexKit)** – High‑performance dex query engine used to locate obfuscated methods and fields at runtime.
- **[Hyper Helper](https://github.com/HowieHChen/XiaomiHelper)** – Inspiration and reference for identifying predictive back gesture hook points.
