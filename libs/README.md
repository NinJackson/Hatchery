# libs/ — local build dependencies (NOT committed)

These jars are **proprietary / non-redistributable** and are intentionally
excluded from the repository (`.gitignore`). To build Hatchery you must place
them here yourself. They are referenced `compileOnly` in `build.gradle`, so
they are only needed to compile — they are never shaded into the output jar.

| File | What it is | Where to get it |
|------|------------|-----------------|
| `Pixelmon-1.20.2-9.2.10.jar` | Pixelmon mod, compile-time API only. This is the filename currently referenced by `build.gradle` in this repo clone. | Official Pixelmon download. For the current 1.21.1 server line, use the matching Pixelmon 9.3.x jar and update the filename in `build.gradle`. |
| `minecraft-server-1.20.2-srg.jar` | Minecraft server jar for transitive NMS type resolution only. | Produced by the Forge/MCP toolchain. For the current 1.21.1 server line, use the matching 1.21.1 server jar and update the filename in `build.gradle`. |

If your Pixelmon build differs, update the filenames in the `dependencies {}`
block of `build.gradle` accordingly.

> The current 1.21.1 runtime-sensitive code lives in
> `gg.hatchery.pixelmon.PixelmonHook` (including the `v1_21_R1` CraftBukkit
> sprite path and the party-storage duplicate guard). These jars are for
> compile-time type resolution, not runtime.
