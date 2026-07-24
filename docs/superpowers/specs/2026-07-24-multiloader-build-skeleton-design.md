# Sub-project A — Multiloader / Multiversion Build Skeleton

**Date:** 2026-07-24
**Status:** Approved (design), pending implementation plan
**Package root:** `dev.shedux.worlddownloader`
**Location:** new subfolder `worlddownloader-mc/` inside the existing repo

## Context

The existing repo (`dev.desco.worlddownloader`, MC 1.8.9 Forge only, Kotlin + Mixin + Essential
loader) is a working world/schematic downloader. The goal is a from-scratch, fully-Kotlin,
multi-loader, multi-version rewrite that downloads server areas as **world / schematic /
litematic**, with a modern custom UI and server-side invisibility.

The full effort is decomposed into five sub-projects, each with its own spec → plan → build cycle:

| # | Sub-project | Scope |
|---|-------------|-------|
| **A** | **Build skeleton (this spec)** | Multiloader + multiversion monorepo. Toolchain, repo layout, version/loader matrix. |
| B | Common core | Download logic (world/schematic/litematic) behind version-agnostic Kotlin interfaces. |
| C | Litematic exporter | New export format. |
| D | UI | shadcn-zinc, `#000000` panels, custom Kotlin render layer (no Elementa). `/wdl` + hotkey. |
| E | Anti-fingerprint | Client-only, no plugin-channel registration, FML mod-list spoof, vanilla brand string. |

**This document specifies A only.** B–E are context, not deliverables here.

## Locked decisions

- **Package root:** `dev.shedux.worlddownloader`
- **Language:** 100% Kotlin. Mixin allowed for MC hooks (Java-annotated mixins permitted).
- **No Essential / Elementa.** Zero dependency on Essential loader or its UI stack.
- **Two version buckets:**
  - **Legacy:** 1.7.10, 1.8.9, 1.12.2 — loaders: Forge + Legacy Fabric.
  - **Modern:** 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, **26.2 (newest)** — loaders: Forge, Fabric,
    NeoForge, Quilt (each where it exists: NeoForge only 1.20.2+; Quilt modern only).
- Matrix is **extensible** — new MC releases are added as rows, not rewrites. Minecraft moved to
  **year-based versioning in 2026**: 26.1 (Mar), 26.2 "Chaos Cubed" (Jun, newest), 26.3 in dev.
  The user's target is "up to 26.2"; the newest modern anchor tracks the latest release. Confirmed
  26.2 tooling: Gradle 9.5.1, Loom 1.17, Fabric Loader 0.19.3.

## Toolchain — the critical choice

Architectury Loom does **not** support 1.7.10 / 1.8.9 (modern-only). The only mature tool that
spans 1.7.10 → latest across multiple loaders in one codebase is the **ReplayMod `preprocessor`**
(Johni0702), which ReplayMod itself ships across 1.7.10–1.21 on Forge + Fabric.

**Decision:** ReplayMod `preprocessor` + Fabric Loom (modern) / legacy Loom (legacy) hybrid.
Version bridging at source level via preprocessor directives (`//#if MC>=11600` … `//#endif`).

**Known gaps (accepted, addressed incrementally in A):**
- NeoForge / Quilt are not preprocessor-native → integrated by hand; A proves NeoForge with one
  anchor, Quilt deferred within A.
- Legacy Fabric 1.8.9 uses a separate maven / loom setup → extra config.

## Repo layout

```
worlddownloader-mc/
├─ settings.gradle.kts        # each anchor version = a subproject/build target
├─ build.gradle.kts           # root config
├─ versions.gradle            # version/loader matrix (source of truth)
├─ core/                      # loader/version-AGNOSTIC Kotlin (download logic) → sub-project B
├─ src/main/kotlin/           # SHARED source; preprocessor emits per-anchor variants
│  └─ dev/shedux/worlddownloader/...
├─ platform/                  # thin loader shims (forge/fabric/neoforge/quilt)
└─ versions/
   ├─ 1.7.10/  1.8.9/  1.12.2/
   ├─ 1.16.5/ 1.20.1/ 1.21.1/ 26.1/ 26.2/   # modern; 26.2 = newest
   └─ mapping/                # preprocessor mapping tables
```

Shared source lives in one place; the preprocessor compiles it per anchor. Loader differences are
isolated to thin `platform/` shims.

## Definition of Done for A

A is **skeleton only** — no download logic ports here (that is B). A is done when:

1. Preprocessor + Loom hybrid is configured and `./gradlew build` **compiles three anchors**,
   proving the hybrid toolchain end-to-end:
   - `1.8.9-forge` (port of the existing mod's build wiring, no logic)
   - `1.20.1-fabric`
   - `26.2-neoforge` (newest release)
2. Each of the three launches in-game and a **`/wdl` command + a rebindable hotkey** both open an
   **empty panel** (UI shell only; shadcn-zinc styling is D).
3. **Zero** Essential / Elementa dependency; build is 100% Kotlin.
4. Remaining anchors (1.7.10, 1.12.2, 1.16.5, 1.18.2, plus Quilt) are **defined as matrix rows**
   in `versions.gradle` but not required to build in A.

## Out of scope for A

- Any world/schematic/litematic download logic (B, C).
- UI styling / real panels (D).
- Anti-fingerprint behavior (E) — the skeleton is client-only by construction, but active FML
  spoofing is E.
- Porting the old `dev.desco.*` code — this is a from-scratch `dev.shedux.*` tree.

## Risks

- Frontier work; ReplayMod preprocessor has a steep learning curve.
- NeoForge / Quilt preprocessor integration is not off-the-shelf.
- Legacy Fabric 1.8.9 toolchain is fiddly (separate maven/loom).
- 1.7.10 legacy toolchain is the heaviest; deferred out of A's 3-anchor proof.

## Ethics / ToS note

World-downloader mods are a long-standing open-source category, but most servers ban them via ToS,
and E hides the mod from server detection. That circumvents those rules; risk is the user's. Keep
to personal / authorized use.
