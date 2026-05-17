# libs/ — local build dependencies (NOT committed)

These jars are **proprietary / non-redistributable** and are intentionally
excluded from the repository (`.gitignore`). To build Hatchery you must place
them here yourself. They are referenced `compileOnly` in `build.gradle`, so
they are only needed to compile — they are never shaded into the output jar.

| File | What it is | Where to get it |
|------|------------|-----------------|
| `Pixelmon-1.20.2-9.2.10.jar` | The Pixelmon mod (compile-time API only) | Official Pixelmon download (https://pixelmonmod.com/) — use the **1.20.2 / 9.2.10** build, or adjust the filename in `build.gradle`. |
| `minecraft-server-1.20.2-srg.jar` | SRG-mapped MC 1.20.2 server, for transitive NMS type resolution only (no direct NMS calls are made) | Produced by the Forge/MCP toolchain (e.g. from your Arclight/Forge build's `libraries/net/minecraft/server/1.20.2-*/...-srg.jar`). |

If your Pixelmon build differs, update the filenames in the `dependencies {}`
block of `build.gradle` accordingly.

> Hatchery talks to Pixelmon and NMS **reflectively** wherever possible, so it
> is resilient to mapping changes; these jars are for compile-time type
> resolution, not runtime.
