# Building from Source

## Toolchain

- **Gradle 8.7** + the Shadow plugin (configured in `build.gradle`)
- A local JDK that can run Gradle. The project currently compiles Java 17
  bytecode, while the 1.21.1 server runtime itself should run on Java 21.
- A Gradle wrapper is **not** committed; use a local Gradle 8.7 (or add a
  wrapper with `gradle wrapper`).

## Supply the compile-only jars

These are **proprietary / non-redistributable** and excluded from the repo
(`.gitignore`). Put them in `libs/` before building:

| File | Notes |
|------|-------|
| `libs/Pixelmon-1.20.2-9.2.10.jar` | Pixelmon mod, compile-time API only. This is the filename currently referenced by `build.gradle` in this repo clone; for a pure 1.21.1 build, use the matching Pixelmon 9.3.x jar and update the filename in `build.gradle`. |
| `libs/minecraft-server-1.20.2-srg.jar` | SRG-mapped Minecraft server jar for transitive NMS type resolution only. For a pure 1.21.1 build, use the matching 1.21.1 server jar and update the filename in `build.gradle`. |

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
and `PLAN.md` for the full package map. The golden rule: **Pixelmon/NMS
coupling lives in `gg.hatchery.pixelmon.PixelmonHook`**. The current 1.21.1
line uses the `v1_21_R1` CraftBukkit package for sprite conversion and keeps
the party-storage duplicate guard there.
