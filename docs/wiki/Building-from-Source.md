# Building from Source

## Toolchain

- **Java 17**
- **Gradle 8.7** + the Shadow plugin (configured in `build.gradle`)
- A Gradle wrapper is **not** committed; use a local Gradle 8.7 (or add a
  wrapper with `gradle wrapper`).

## Supply the compile-only jars

These are **proprietary / non-redistributable** and excluded from the repo
(`.gitignore`). Put them in `libs/` before building:

| File | Notes |
|------|-------|
| `libs/Pixelmon-1.20.2-9.2.10.jar` | Pixelmon mod, compile-time API only. Use the 1.20.2 / 9.2.10 build from the official Pixelmon site, or change the filename in `build.gradle`. |
| `libs/minecraft-server-1.20.2-srg.jar` | SRG-mapped MC 1.20.2 server for transitive NMS *type resolution only* (no direct NMS calls). Produced by the Forge/MCP toolchain. |

Both are referenced `compileOnly`, so they are **never shaded** into the output.

## Build

```bash
gradle shadowJar
# output: build/libs/Hatchery-<version>.jar
```

`build {}` depends on `shadowJar`, so `gradle build` also produces the shaded
jar. SQLite, MySQL connector, HikariCP and SLF4J are bundled; Hikari/SLF4J are
relocated under `gg.hatchery.libs.*` to avoid conflicts.

## Versioning

`version` is set in `build.gradle` (`group = 'gg.hatchery'`). `processResources`
expands `${version}` into `plugin.yml` at build time.

## Project layout

See [the repo README](https://github.com/NinJackson/Hatchery#building-from-source)
and `PLAN.md` for the full package map. The golden rule: **all Pixelmon/NMS
coupling is reflective and isolated in `gg.hatchery.pixelmon.PixelmonHook`** —
that's the only file a Pixelmon version bump should touch.
