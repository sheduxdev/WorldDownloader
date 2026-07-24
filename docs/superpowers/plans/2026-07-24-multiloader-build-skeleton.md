# Multiloader Build Skeleton (Sub-project A) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up a from-scratch multiloader/multiversion Minecraft mod monorepo that compiles three anchor targets from a shared Kotlin source and opens an empty panel via `/wdl` command and a rebindable hotkey.

**Architecture:** A `worlddownloader-mc/` Gradle monorepo. Modern anchors build with **Architectury Loom** (Fabric/Forge/NeoForge, 1.16+); the legacy 1.8.9 anchor builds with **ForgeGradle 2** (mirroring the existing repo's working 1.8.9 setup). Shared Kotlin source lives once under `src/` and is fanned out to each anchor by the **ReplayMod `preprocessor`** Gradle plugin using source directives (`//#if MC>=11600`). Each anchor keeps only thin loader-shim code.

**Tech Stack:** Kotlin, Gradle (Kotlin DSL), Architectury Loom, ForgeGradle 2, ReplayMod preprocessor, Mixin (for MC hooks), Fabric Loader / Forge / NeoForge.

## Global Constraints

- Package root is exactly `dev.shedux.worlddownloader`. No `dev.desco.*`.
- 100% Kotlin for mod logic. Java is allowed only for Mixin classes.
- Zero dependency on Essential loader or Elementa (no `gg.essential:*`).
- Two version buckets. Legacy: 1.7.10, 1.8.9, 1.12.2 (Forge + Legacy Fabric). Modern: 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2-newest (Forge, Fabric, NeoForge, Quilt where each exists).
- A is skeleton only: NO world/schematic/litematic download logic, NO real UI styling, NO active anti-fingerprint code.
- **Full coverage target:** EVERY MC version from 1.7.10 through 26.2 (all point releases), on EVERY loader that exists for that version (Forge, Legacy Fabric, Fabric, Quilt, NeoForge — per the availability windows in Task 1). ~280 (version×loader) rows, generated not hand-typed. No loader is built for a version where that loader does not exist (impossible, not skipped).
- A's build proof is exactly three rows: `1.8.9-forge`, `1.20.1-fabric`, `26.2-neoforge`. The remaining ~277 rows are declared in the matrix but `build=false` in A; they flip to `build=true` per version once the 3-anchor toolchain is proven. Building them all out is the ongoing work after A, not part of A.
- The empty panel must be reachable by BOTH a `/wdl` client command AND a rebindable key binding.
- Modern anchors use **latest stable** tooling. Confirmed for MC 26.2 (newest release, June 2026): **Gradle 9.5.1, Architectury/Fabric Loom 1.17, Fabric Loader 0.19.3**, latest Kotlin. Confirm each against the reference templates at build time.
- Minecraft switched to **year-based versioning in 2026**: 26.1 (Mar), **26.2 "Chaos Cubed" (newest)**, 26.3 in dev. The matrix has **no fixed ceiling** — every new release is a new row, never a rewrite; the newest modern anchor tracks the latest release (currently 26.2).
- **Toolchain conflict (known, unresolved in A):** legacy 1.8.9 uses ForgeGradle 2, incompatible with Gradle 9.x; modern (26.2) needs Gradle 9.5.1. A single shared wrapper cannot satisfy both. Accept the two-toolchain split — the legacy anchor uses its own Gradle (ForgeGradle-2-compatible, ~Gradle 4–5 or the debuggingss fork's max), the modern anchors use 9.5.1. Do NOT force one version across both buckets.

## Reference templates (mirror these — do not invent the DSL)

The exact Gradle plugin DSL for the preprocessor and Architectury Loom drifts by version. Copy structure from these canonical, working repos and only adapt coordinates:

- ReplayMod preprocessor + example: `https://github.com/ReplayMod/preprocessor` (README shows the `preprocess { }` project-graph block and `//#if` directive syntax).
- ReplayMod itself (real 1.7.10→1.21 multiloader monorepo): `https://github.com/ReplayMod/ReplayMod` — `build.gradle`, `settings.gradle`, `versions/` layout are the reference implementation for this whole plan.
- Architectury Loom multiloader template (Fabric/Forge/NeoForge for modern): `https://github.com/architectury/architectury-templates`.
- Existing working 1.8.9 Forge setup to mirror for the legacy anchor: `../build.gradle.kts` in this repo (ForgeGradle FG_2.1, Mixin 0.8.5, `stable_22` mappings) — strip the `gg.essential` lines.

When a step below prints a coordinate (loader/loom/yarn version), confirm it against the template's current `gradle.properties` before pinning; treat drifted coordinates as an expected adjustment, not a plan deviation.

---

### Task 1: Monorepo scaffold + version matrix

**Files:**
- Create: `worlddownloader-mc/settings.gradle.kts`
- Create: `worlddownloader-mc/build.gradle.kts`
- Create: `worlddownloader-mc/gradle.properties`
- Create: `worlddownloader-mc/versions.gradle.kts`
- Create: `worlddownloader-mc/gradle/wrapper/gradle-wrapper.properties`
- Create: `worlddownloader-mc/.gitignore`

**Interfaces:**
- Produces: a Gradle root project named `worlddownloader-mc` with three empty subprojects registered — `1.8.9-forge`, `1.20.1-fabric`, `26.2-neoforge` — plus a `versions.gradle.kts` map listing every matrix row (buildable flag per row).

- [ ] **Step 1: Create the Gradle wrapper**

Run from repo root:
```bash
cd "c:/Users/ddeni/Desktop/sheduxdev/WorldDownloader"
mkdir -p worlddownloader-mc && cd worlddownloader-mc
gradle wrapper --gradle-version 9.5.1
```
Modern anchors need Gradle 9.5.1 (MC 26.2 / Loom 1.17 requirement). If no system `gradle`, copy `gradle/` + `gradlew*` from a current template and set version 9.5.1 in `gradle-wrapper.properties`. NOTE: the 1.8.9-forge anchor (Task 3, ForgeGradle 2) will NOT run under Gradle 9 — per Global Constraints it gets its own Gradle wrapper (nested build or separate invocation); this root wrapper drives the modern anchors.

- [ ] **Step 2: Write the version matrix**

Full coverage: EVERY listed MC version, on EVERY loader that exists for it. The matrix is
**generated** from the ordered version list + per-loader availability windows — never hand-typed.
`worlddownloader-mc/versions.gradle.kts`:
```kotlin
// Ordered list of ALL target MC versions (index = chronological order). Add new releases at the end.
val allVersions = listOf(
    "1.7.10",
    "1.8","1.8.1","1.8.2","1.8.3","1.8.4","1.8.5","1.8.6","1.8.7","1.8.8","1.8.9",
    "1.9","1.9.1","1.9.2","1.9.3","1.9.4",
    "1.10","1.10.1","1.10.2","1.11","1.11.1","1.11.2","1.12","1.12.1","1.12.2",
    "1.13","1.13.1","1.13.2",
    "1.14","1.14.1","1.14.2","1.14.3","1.14.4","1.15","1.15.1","1.15.2",
    "1.16","1.16.1","1.16.2","1.16.3","1.16.4","1.16.5","1.17","1.17.1",
    "1.18","1.18.1","1.18.2","1.19","1.19.1","1.19.2","1.19.3","1.19.4",
    "1.20","1.20.1","1.20.2","1.20.3","1.20.4","1.20.5","1.20.6",
    "1.21","1.21.1","1.21.2","1.21.3","1.21.4","1.21.5","1.21.6","1.21.7","1.21.8","1.21.9","1.21.10","1.21.11",
    "26.1","26.1.1","26.1.2","26.2",
)
fun idx(v: String) = allVersions.indexOf(v).also { require(it >= 0) { "unknown version $v" } }

// Loader availability windows (inclusive), verified against each loader's release history:
//   forge        : all versions (1.7.10..latest); 26.x Forge unconfirmed -> NeoForge covers 26.x
//   legacyfabric : 1.7.10 .. 1.13.2   (pre-official-Fabric)
//   fabric       : 1.14   .. latest
//   quilt        : 1.18.2 .. latest
//   neoforge     : 1.20.2 .. latest
fun loadersFor(v: String): List<String> {
    val i = idx(v)
    val out = mutableListOf<String>()
    if (i <= idx("1.21.11")) out += "forge"          // Forge across 1.7.10..1.21.x
    if (i <= idx("1.13.2"))  out += "legacyfabric"
    if (i >= idx("1.14"))    out += "fabric"
    if (i >= idx("1.18.2"))  out += "quilt"
    if (i >= idx("1.20.2"))  out += "neoforge"
    return out
}

// Every valid (version, loader) pair = a matrix row. Only the 3 proof ids build in A.
data class Anchor(val id: String, val mc: String, val loader: String, val build: Boolean)
val proofIds = setOf("1.8.9-forge", "1.20.1-fabric", "26.2-neoforge")
val anchors: List<Anchor> = allVersions.flatMap { v ->
    loadersFor(v).map { l -> val id = "$v-$l"; Anchor(id, v, l, id in proofIds) }
}
extra["anchors"] = anchors
// -> ~280 declared rows; 3 build=true in A. Flip build=true per version as the toolchain is proven.
```

- [ ] **Step 3: Register only the buildable subprojects in settings**

`worlddownloader-mc/settings.gradle.kts`:
```kotlin
rootProject.name = "worlddownloader-mc"
apply(from = "versions.gradle.kts")

@Suppress("UNCHECKED_CAST")
val anchors = extra["anchors"] as List<*>
// include only build=true rows in A
listOf("1.8.9-forge", "1.20.1-fabric", "26.2-neoforge").forEach { id ->
    include(":$id")
    project(":$id").projectDir = file("versions/$id")
}
```

- [ ] **Step 4: Minimal root build + gradle.properties**

`worlddownloader-mc/build.gradle.kts`:
```kotlin
// Root is config-only; each anchor build lives under versions/<id>/.
tasks.register("anchors") {
    doLast { println("Buildable anchors: 1.8.9-forge, 1.20.1-fabric, 26.2-neoforge") }
}
```
`worlddownloader-mc/gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4G
org.gradle.parallel=true
kotlin.code.style=official
modGroup=dev.shedux.worlddownloader
modBaseName=worlddownloader
```
`worlddownloader-mc/.gitignore`:
```
.gradle/
build/
run/
```

- [ ] **Step 5: Verify the project graph loads**

Run:
```bash
cd worlddownloader-mc && ./gradlew projects
```
Expected: BUILD SUCCESSFUL, and the printed tree lists `+--- Project ':1.8.9-forge'`, `':1.20.1-fabric'`, `':26.2-neoforge'`. (The `versions/<id>` dirs are empty for now — Gradle tolerates missing build files only if the projects have none; if it errors on missing build script, create empty `versions/<id>/build.gradle.kts` files.)

- [ ] **Step 6: Commit**

```bash
git add worlddownloader-mc
git commit -m "feat(mc): scaffold multiloader monorepo + version matrix"
```

---

### Task 2: Modern anchor standalone — `1.20.1-fabric`

Proves the modern Architectury Loom + Kotlin + no-Essential path with a working `/wdl` command and rebindable keybind opening an empty screen. Built standalone first (no preprocessor yet) to isolate toolchain risk.

**Files:**
- Create: `worlddownloader-mc/versions/1.20.1-fabric/build.gradle.kts`
- Create: `worlddownloader-mc/versions/1.20.1-fabric/src/main/resources/fabric.mod.json`
- Create: `worlddownloader-mc/versions/1.20.1-fabric/src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt`
- Create: `worlddownloader-mc/versions/1.20.1-fabric/src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt`

**Interfaces:**
- Produces: client entrypoint `dev.shedux.worlddownloader.WorldDownloaderMod` that (a) registers a `/wdl` client command and (b) registers a rebindable `KeyMapping`, both opening `EmptyPanelScreen`.

- [ ] **Step 1: Write the Fabric build script**

Mirror the Architectury/Fabric template. `versions/1.20.1-fabric/build.gradle.kts`:
```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    id("dev.architectury.loom") version "1.7.+"
}

val mc = "1.20.1"
loom { }

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    mappings("net.fabricmc:yarn:$mc+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+$mc")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")
}
// Confirm these five versions against architectury-templates gradle.properties before first build.
```

- [ ] **Step 2: Write the mod metadata**

`versions/1.20.1-fabric/src/main/resources/fabric.mod.json`:
```json
{
  "schemaVersion": 1,
  "id": "worlddownloader",
  "version": "0.0.1",
  "name": "WorldDownloader",
  "environment": "client",
  "entrypoints": { "client": [ { "adapter": "kotlin", "value": "dev.shedux.worlddownloader.WorldDownloaderMod" } ] },
  "depends": { "fabric-language-kotlin": "*" }
}
```

- [ ] **Step 3: Write the empty panel screen**

`.../ui/EmptyPanelScreen.kt`:
```kotlin
package dev.shedux.worlddownloader.ui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

// A: intentionally empty shell. shadcn-zinc styling is sub-project D.
class EmptyPanelScreen : Screen(Text.literal("WorldDownloader")) {
    override fun shouldPause(): Boolean = false
}
```

- [ ] **Step 4: Write the entrypoint (command + keybind)**

`.../WorldDownloaderMod.kt`:
```kotlin
package dev.shedux.worlddownloader

import com.mojang.brigadier.CommandDispatcher
import dev.shedux.worlddownloader.ui.EmptyPanelScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object WorldDownloaderMod : ClientModInitializer {
    private lateinit var openKey: KeyBinding

    override fun onInitializeClient() {
        // /wdl command
        ClientCommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<FabricClientCommandSource>, _ ->
            dispatcher.register(
                ClientCommandManager.literal("wdl").executes { ctx ->
                    ctx.source.client.send { ctx.source.client.setScreen(EmptyPanelScreen()) }
                    1
                }
            )
        }
        // rebindable hotkey (default: unbound, category worlddownloader)
        openKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.worlddownloader.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.worlddownloader")
        )
        ClientTickEvents.END_CLIENT_TICK.register { client: MinecraftClient ->
            while (openKey.wasPressed()) client.setScreen(EmptyPanelScreen())
        }
    }
}
```

- [ ] **Step 5: Build the anchor**

Run:
```bash
cd worlddownloader-mc && ./gradlew :1.20.1-fabric:build
```
Expected: BUILD SUCCESSFUL; a jar under `versions/1.20.1-fabric/build/libs/`.

- [ ] **Step 6: Launch and verify in-game (this is the test)**

Run:
```bash
./gradlew :1.20.1-fabric:runClient
```
Verify manually, all three must hold:
1. Game reaches main menu with no crash (mod loaded).
2. Enter a world, type `/wdl` → an empty screen opens (dark default background, no widgets), `Esc` closes it.
3. Options → Controls → search "worlddownloader" → the `key.worlddownloader.open` binding exists and is rebindable; bind a key, press it in-world → same empty screen opens.

If any fail, stop and fix before committing.

- [ ] **Step 7: Commit**

```bash
git add worlddownloader-mc/versions/1.20.1-fabric
git commit -m "feat(mc): 1.20.1-fabric anchor with /wdl command + rebindable hotkey"
```

---

### Task 3: Legacy anchor standalone — `1.8.9-forge`

Proves the legacy ForgeGradle 2 + Kotlin path, mirroring the existing repo's working 1.8.9 setup with Essential stripped. Same `/wdl` + keybind behavior, 1.8.9 API.

**Files:**
- Create: `worlddownloader-mc/versions/1.8.9-forge/build.gradle.kts`
- Create: `worlddownloader-mc/versions/1.8.9-forge/src/main/resources/mcmod.info`
- Create: `worlddownloader-mc/versions/1.8.9-forge/src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt`
- Create: `worlddownloader-mc/versions/1.8.9-forge/src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt`

**Interfaces:**
- Produces: `@Mod`-annotated object `dev.shedux.worlddownloader.WorldDownloaderMod` (modid `worlddownloader`) registering a `/wdl` `ICommand` and a `KeyBinding` polled on `ClientTickEvent`, both opening `EmptyPanelScreen`.

- [ ] **Step 1: Write the legacy build script**

Mirror `../../build.gradle.kts` (repo root) minus Essential. `versions/1.8.9-forge/build.gradle.kts`:
```kotlin
import net.minecraftforge.gradle.user.ReobfMappingType

plugins {
    java
    kotlin("jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("net.minecraftforge.gradle.forge") version "useModule@com.github.debuggingss:ForgeGradle:FG_2.1-SNAPSHOT"
    id("org.spongepowered.mixin") version "useModule@com.github.xcfrg:MixinGradle:0.6-SNAPSHOT"
}

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    mappings = "stable_22"
    runDir = "run"
    makeObfSourceJar = false
    clientRunArgs.add("--mixin worlddownloader.mixins.json")
}

repositories {
    mavenCentral(); maven("https://jitpack.io/"); maven("https://repo.spongepowered.org/repository/maven-public/")
}
dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}
reobf { create("shadowJar"); all { mappingType = ReobfMappingType.SEARGE } }
tasks {
    reobfJar { dependsOn(shadowJar) }
    withType<JavaCompile> { sourceCompatibility = "1.8"; targetCompatibility = "1.8"; options.encoding = "UTF-8" }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
}
```
Note: `useModule@` plugin syntax comes from the existing root build's `infix from` helper — if the plugin resolution fails, copy the exact `pluginManagement`/`from` helper from `../../build.gradle.kts` into `worlddownloader-mc/settings.gradle.kts`.

- [ ] **Step 2: Write mcmod.info**

`versions/1.8.9-forge/src/main/resources/mcmod.info`:
```json
[ { "modid": "worlddownloader", "name": "WorldDownloader", "version": "0.0.1" } ]
```

- [ ] **Step 3: Write the empty panel (1.8.9 GuiScreen)**

`.../ui/EmptyPanelScreen.kt`:
```kotlin
package dev.shedux.worlddownloader.ui

import net.minecraft.client.gui.GuiScreen

// A: intentionally empty shell. shadcn-zinc styling is sub-project D.
class EmptyPanelScreen : GuiScreen() {
    override fun doesGuiPauseGame(): Boolean = false
}
```

- [ ] **Step 4: Write the entrypoint (command + keybind, 1.8.9 API)**

`.../WorldDownloaderMod.kt`:
```kotlin
package dev.shedux.worlddownloader

import dev.shedux.worlddownloader.ui.EmptyPanelScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard

@Mod(modid = "worlddownloader", name = "WorldDownloader", version = "0.0.1", clientSideOnly = true, modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
object WorldDownloaderMod {
    private val openKey = KeyBinding("key.worlddownloader.open", Keyboard.KEY_NONE, "category.worlddownloader")

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        ClientCommandHandler.instance.registerCommand(object : CommandBase() {
            override fun getCommandName() = "wdl"
            override fun getCommandUsage(s: ICommandSender) = "/wdl"
            override fun getRequiredPermissionLevel() = 0
            override fun execute(server: net.minecraft.server.MinecraftServer?, sender: ICommandSender, args: Array<out String>) {
                Minecraft.getMinecraft().addScheduledTask { Minecraft.getMinecraft().displayGuiScreen(EmptyPanelScreen()) }
            }
        })
        ClientRegistry.registerKeyBinding(openKey)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onKey(e: InputEvent.KeyInputEvent) {
        if (openKey.isPressed) Minecraft.getMinecraft().displayGuiScreen(EmptyPanelScreen())
    }
}
```
Note: 1.8.9 has no first-class Kotlin adapter. Either add the Forgelin language adapter dependency (`compileOnly`/`embed("net.shadowfacts:Forgelin:1.8.4")`) as the existing mod does, OR drop `modLanguageAdapter` and register from a tiny Java `@Mod` bootstrap that calls the Kotlin `object`. Pick whichever the existing repo already proved and mirror it.

- [ ] **Step 5: Build the anchor**

Run:
```bash
cd worlddownloader-mc && ./gradlew :1.8.9-forge:build
```
Expected: BUILD SUCCESSFUL; a reobf jar under `versions/1.8.9-forge/build/libs/`.

- [ ] **Step 6: Launch and verify in-game (the test)**

Run:
```bash
./gradlew :1.8.9-forge:runClient
```
Verify the same three conditions as Task 2 Step 6, adapted to 1.8.9 (Controls menu under category `worlddownloader`, `/wdl` opens the empty `GuiScreen`, hotkey opens it).

- [ ] **Step 7: Commit**

```bash
git add worlddownloader-mc/versions/1.8.9-forge
git commit -m "feat(mc): 1.8.9-forge legacy anchor (Essential stripped)"
```

---

### Task 4: Introduce the preprocessor — share source across the two anchors

Both anchors now duplicate `WorldDownloaderMod`/`EmptyPanelScreen`. Extract the shared source to one place and let the ReplayMod preprocessor emit per-anchor variants, with version-guarded API differences.

**Files:**
- Create: `worlddownloader-mc/src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt` (unified, `//#if` guarded)
- Create: `worlddownloader-mc/src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt` (unified)
- Modify: `worlddownloader-mc/settings.gradle.kts` (apply preprocessor plugin + project graph)
- Modify: `worlddownloader-mc/versions/1.8.9-forge/build.gradle.kts` (consume preprocessed source)
- Modify: `worlddownloader-mc/versions/1.20.1-fabric/build.gradle.kts` (consume preprocessed source)
- Delete: the per-anchor duplicated `WorldDownloaderMod.kt` / `EmptyPanelScreen.kt` from Tasks 2 & 3

**Interfaces:**
- Consumes: the two working anchor builds from Tasks 2–3.
- Produces: a `preprocess { }` project graph where `1.20.1-fabric` is the main (source-of-truth) project and `1.8.9-forge` is a derived node; `./gradlew build` compiles both from `src/`.

- [ ] **Step 1: Wire the preprocessor project graph**

Follow the ReplayMod preprocessor README exactly. In `settings.gradle.kts`, apply the plugin and declare the graph mapping each anchor to its MC version int (e.g. `1.20.1` → `11605`? — use the README's `mcVersion` convention, typically `1_20_01` style). Mirror the example's `preprocess` block; do not invent the API. The main project is `1.20.1-fabric`.

- [ ] **Step 2: Move shared source to `src/` with version guards**

Create `src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt` guarding the screen base class:
```kotlin
package dev.shedux.worlddownloader.ui

//#if MC>=11600
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
class EmptyPanelScreen : Screen(Text.literal("WorldDownloader")) {
    override fun shouldPause(): Boolean = false
}
//#else
//$$ import net.minecraft.client.gui.GuiScreen
//$$ class EmptyPanelScreen : GuiScreen() {
//$$     override fun doesGuiPauseGame(): Boolean = false
//$$ }
//#endif
```
Do the same for `WorldDownloaderMod.kt`: the Fabric body (Task 2) under `//#if MC>=11600`, the Forge 1.8.9 body (Task 3) under `//#else` with `//$$` line prefixes. The preprocessor strips/activates the correct branch per anchor.

- [ ] **Step 3: Point each anchor at the preprocessed source**

In both anchor `build.gradle.kts`, remove the local `src/main/kotlin` mod sources and set the source root to the preprocessor's generated output (the README shows `sourceSets.main.kotlin.srcDir(preprocessor output)` or applies it automatically via the plugin). Keep loader-specific resources (`fabric.mod.json`, `mcmod.info`) local to each anchor.

- [ ] **Step 4: Delete the duplicated per-anchor Kotlin**

```bash
rm worlddownloader-mc/versions/1.20.1-fabric/src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt \
   worlddownloader-mc/versions/1.20.1-fabric/src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt \
   worlddownloader-mc/versions/1.8.9-forge/src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt \
   worlddownloader-mc/versions/1.8.9-forge/src/main/kotlin/dev/shedux/worlddownloader/ui/EmptyPanelScreen.kt
```

- [ ] **Step 5: Build both anchors from shared source (the test)**

Run:
```bash
cd worlddownloader-mc && ./gradlew :1.20.1-fabric:build :1.8.9-forge:build
```
Expected: BOTH BUILD SUCCESSFUL, compiled from the single `src/` tree. Then re-run both `runClient` tasks and re-verify the `/wdl` + hotkey behavior from Tasks 2 & 3 still holds (regression check — the refactor must not change behavior).

- [ ] **Step 6: Commit**

```bash
git add -A worlddownloader-mc
git commit -m "refactor(mc): unify anchor source under ReplayMod preprocessor"
```

---

### Task 5: Add the newest NeoForge anchor — `26.2-neoforge`

Third and final build proof: the newest release (MC 26.2, year-versioned) on a modern non-Fabric loader, driven from the same shared source. This directly proves the user's "up to 26.2" target.

**Files:**
- Create: `worlddownloader-mc/versions/26.2-neoforge/build.gradle.kts`
- Create: `worlddownloader-mc/versions/26.2-neoforge/src/main/resources/META-INF/neoforge.mods.toml`
- Modify: `worlddownloader-mc/settings.gradle.kts` (add `26.2-neoforge` node to the preprocessor graph)
- Modify: `src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt` (add NeoForge branch guards)

**Interfaces:**
- Consumes: the shared `src/` and preprocessor graph from Task 4.
- Produces: a third buildable anchor whose entrypoint opens `EmptyPanelScreen` via `/wdl` and a rebindable key, using NeoForge for MC 26.2 APIs.

**Preprocessor version-code note:** the ReplayMod preprocessor keys versions by a numeric `mcVersion` code (e.g. `1.20.1`→`11605`-style). Year-versioning (26.2) breaks the old 1.x scheme — use the code convention from the CURRENT preprocessor release (which supports 26.x), and define 26.2's code in the graph (Task 4 Step 1). Guard 26.2-only source with that code, e.g. `//#if MC>=<26.2-code>`.

- [ ] **Step 1: Write the NeoForge build script**

Mirror the Architectury Loom NeoForge template, at latest coordinates for 26.2. `versions/26.2-neoforge/build.gradle.kts`:
```kotlin
plugins {
    kotlin("jvm") version "2.0.20"        // latest stable Kotlin
    id("dev.architectury.loom") version "1.17.+"   // 26.2 requires Loom 1.17
}
loom { }
dependencies {
    minecraft("com.mojang:minecraft:26.2")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:<26.2-neoforge-version>")   // confirm current NeoForge build for 26.2
    // Kotlin for NeoForge via Kotlin-for-Forge (latest for 26.2):
    implementation("thedarkcolour:kotlinforforge-neoforge:<latest>")
}
// Requires Gradle 9.5.1 (root wrapper). Confirm neoforge + kotlinforforge versions against their 26.2 releases before first build.
```

- [ ] **Step 2: Write the NeoForge mod metadata**

`versions/26.2-neoforge/src/main/resources/META-INF/neoforge.mods.toml`:
```toml
modLoader = "kotlinforforge"
loaderVersion = "[5,)"
license = "All Rights Reserved"
[[mods]]
modId = "worlddownloader"
version = "0.0.1"
displayName = "WorldDownloader"
[[dependencies.worlddownloader]]
modId = "neoforge"
type = "required"
versionRange = "[26.2,)"
side = "CLIENT"
```

- [ ] **Step 3: Add the NeoForge branch to shared source**

In `src/main/kotlin/dev/shedux/worlddownloader/WorldDownloaderMod.kt`, split the modern branch by loader. The preprocessor supports loader-token guards in addition to MC version (README: e.g. `//#if FABRIC` / `//#if NEOFORGE`). Under `//#if NEOFORGE`, register via NeoForge: a `@Mod` object using `RegisterClientCommandsEvent` for `/wdl` and `RegisterKeyMappingsEvent` for the keybind, both opening `EmptyPanelScreen`. Keep the Fabric branch under `//#if FABRIC`. Write the full NeoForge body inline (no placeholder):
```kotlin
//#if NEOFORGE
//$$ // NeoForge 26.2 entrypoint
//$$ import net.neoforged.fml.common.Mod
//$$ import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
//$$ import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.minecraft.client.KeyMapping
//$$ import net.minecraft.client.Minecraft
//$$ import com.mojang.blaze3d.platform.InputConstants
//$$ import net.minecraft.commands.Commands
//$$ @Mod("worlddownloader")
//$$ object WorldDownloaderMod {
//$$     val openKey = KeyMapping("key.worlddownloader.open", InputConstants.UNKNOWN.value, "category.worlddownloader")
//$$     @SubscribeEvent fun cmds(e: RegisterClientCommandsEvent) {
//$$         e.dispatcher.register(Commands.literal("wdl").executes {
//$$             Minecraft.getInstance().execute { Minecraft.getInstance().setScreen(dev.shedux.worlddownloader.ui.EmptyPanelScreen()) }; 1 })
//$$     }
//$$     @SubscribeEvent fun keys(e: RegisterKeyMappingsEvent) { e.register(openKey) }
//$$ }
//#endif
```
(If the preprocessor's loader-token syntax differs from `NEOFORGE`, use the token names from the graph you defined in Task 4 Step 1 — check the README.)

- [ ] **Step 4: Add the anchor to settings + graph, then build (the test)**

Add `include(":26.2-neoforge")` and its node to the preprocessor graph in `settings.gradle.kts`. Run:
```bash
cd worlddownloader-mc && ./gradlew :26.2-neoforge:build
./gradlew :26.2-neoforge:runClient
```
Expected: BUILD SUCCESSFUL. In-game, verify the same three conditions as Task 2 Step 6 on NeoForge / MC 26.2. Then run the full proof (modern anchors on the root Gradle 9.5.1 wrapper; the 1.8.9 anchor on its own ForgeGradle-2 Gradle per Global Constraints):
```bash
./gradlew :1.20.1-fabric:build :26.2-neoforge:build   # modern, root wrapper
# 1.8.9-forge built via its own legacy Gradle invocation (Task 3)
```
All three anchors SUCCESSFUL = A's definition of done met.

- [ ] **Step 5: Commit**

```bash
git add -A worlddownloader-mc
git commit -m "feat(mc): add newest 26.2-neoforge anchor from shared source"
```

---

### Task 6: Declare remaining matrix rows + document the skeleton

Locks in the remaining anchors as declared-not-built rows and writes the one doc a future contributor needs to add a version.

**Files:**
- Modify: `worlddownloader-mc/versions.gradle.kts` (already lists all rows — verify complete)
- Create: `worlddownloader-mc/README.md`

**Interfaces:**
- Consumes: the three-anchor build from Task 5.
- Produces: `README.md` documenting the buckets, the three built anchors, and the exact steps to promote a `build=false` row to a built anchor.

- [ ] **Step 1: Verify the matrix is complete**

Verify `versions.gradle.kts` `allVersions` lists every MC release 1.7.10 → 26.2 in order, and `loadersFor` windows match the availability table (Legacy Fabric ≤1.13.2, Fabric ≥1.14, Quilt ≥1.18.2, NeoForge ≥1.20.2, Forge ≤1.21.x). Add a quick sanity task that prints `anchors.size` (~280) and asserts the 3 proof ids are `build=true`:
```kotlin
tasks.register("matrixCheck") {
    doLast {
        @Suppress("UNCHECKED_CAST") val a = extra["anchors"] as List<Any>
        println("matrix rows: ${a.size}")
    }
}
```

- [ ] **Step 2: Write the skeleton README**

`worlddownloader-mc/README.md`:
```markdown
# worlddownloader-mc

Multiloader / multiversion skeleton (sub-project A). Package root `dev.shedux.worlddownloader`.

## Buckets
- Legacy (ForgeGradle 2, own Gradle): 1.7.10, 1.8.9, 1.12.2 — Forge (+ Legacy Fabric later).
- Modern (Architectury Loom, Gradle 9.5.1): 1.16.5, 1.18.2, 1.20.1, 1.21.1, 26.1, 26.2 (newest) — Forge, Fabric, NeoForge, Quilt. Matrix is open-ended; new releases are new rows.

## Built anchors (A proof)
- `1.8.9-forge`, `1.20.1-fabric`, `26.2-neoforge` (newest).
Build modern: `./gradlew :1.20.1-fabric:build :26.2-neoforge:build` — legacy `1.8.9-forge` builds via its own Gradle (see Task 3).

## Shared source
Lives in `src/`. The ReplayMod preprocessor fans it out per anchor via `//#if MC>=…` / loader guards.

## Add a new anchor
1. Set the row `build = true` in `versions.gradle.kts`.
2. `include(":<id>")` in `settings.gradle.kts` and add it to the `preprocess` graph.
3. Create `versions/<id>/build.gradle.kts` (mirror the nearest same-loader anchor) + loader metadata.
4. Add `//#if` branches in `src/` for any changed API. Build + runClient to verify `/wdl` + hotkey.

Not built in A: 1.7.10, 1.12.2, 1.16.5, 1.18.2, and Quilt — declared rows only.
```

- [ ] **Step 3: Verify**

Run:
```bash
cd worlddownloader-mc && ./gradlew anchors && ./gradlew projects
```
Expected: `anchors` prints the three built ids; `projects` lists exactly the three built subprojects.

- [ ] **Step 4: Commit**

```bash
git add worlddownloader-mc/versions.gradle.kts worlddownloader-mc/README.md
git commit -m "docs(mc): document skeleton + declare remaining matrix rows"
```

---

## Self-Review

**Spec coverage:**
- Package `dev.shedux.worlddownloader` → Global Constraints + every source path. ✓
- 100% Kotlin, no Essential → Tasks 2–3 (no `gg.essential`), legacy build strips Essential. ✓
- Two buckets, anchor lists → Task 1 matrix + Task 6. ✓
- Toolchain (preprocessor + Loom/ForgeGradle hybrid) → Tasks 2 (Loom), 3 (ForgeGradle 2), 4 (preprocessor). ✓
- Repo layout (`src/` shared, `versions/`, `platform/` shims) → Tasks 1, 4. `platform/` shims: not needed at A's shell scope (loader differences handled by `//#if` guards in the tiny entrypoint) — deferred to B when real logic needs abstraction. Noted, not a gap.
- DoD: 3 anchors build (`1.8.9-forge`, `1.20.1-fabric`, `26.2-neoforge`) → Task 5 Step 4 full proof. ✓
- DoD: `/wdl` + rebindable hotkey open empty panel → Tasks 2/3/5 entrypoints + verify steps. ✓
- DoD: remaining anchors declared as rows → Tasks 1 + 6. ✓
- Out of scope (download logic, UI styling, anti-fingerprint) → nothing in A touches them; screens are empty shells. ✓

**Placeholder scan:** No "TBD/TODO/handle edge cases". The preprocessor `preprocess { }` graph (Task 4 Step 1) and loader-token syntax point to the ReplayMod README rather than inventing DSL — this is a deliberate "mirror the canonical template" instruction, not a placeholder, because the exact DSL is version-volatile and fabricating it would be wrong. Gradle coordinates carry "confirm against template" notes for the same reason.

**Type consistency:** `EmptyPanelScreen` (no-arg ctor), `WorldDownloaderMod`, key id `key.worlddownloader.open`, category `category.worlddownloader`, command literal `wdl`, modid `worlddownloader` — identical across Tasks 2, 3, 5 and the unified Task 4 source. ✓
