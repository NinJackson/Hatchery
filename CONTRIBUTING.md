# Contributing to Hatchery

Thanks for your interest! Hatchery is a community-friendly, MIT-licensed plugin.

## Dev setup

- **Gradle 8.7**, Shadow plugin (see `build.gradle`), and a local JDK that can
  run the build. The project currently emits Java 17 bytecode; the 1.21.1
  Minecraft server runtime itself should run on Java 21.
- Supply the compile-only jars in `libs/` (see [`libs/README.md`](libs/README.md)).
  They are `.gitignore`d and **must never be committed** (non-redistributable).
- Build: `gradle shadowJar` → `build/libs/Hatchery-<version>.jar`.
- Test against **Arclight 1.21.1 + Pixelmon 9.3.x** for the current server line
  unless you are intentionally maintaining an older branch.

## Architecture (where things live)

```
gg.hatchery
├── Hatchery.java            entrypoint / manager owner
├── config/                  config.yml, environment-points.yml, messages.yml, hourglasses.yml
├── daycare/                 Daycare model + DaycareManager (registry, caps, LP meta)
├── breeding/                BreedingEngine (tick loop) + EnvironmentScanner
├── pixelmon/                PixelmonHook (ALL Pixelmon API access) + PokemonNbtCodec
├── ui/                      Daycare / PartyPicker menus + MenuManager
├── listeners/               place/break, interact, inventory, block-change
├── commands/                /daycares, /hatchery
├── storage/                 Storage iface + SQLite + MySQL + SchemaSql
└── util/                    ItemBuilder
```

**Key principle:** keep Pixelmon/NMS coupling isolated in
`pixelmon/PixelmonHook.java` where possible. That file owns the runtime-specific
CraftBukkit sprite path, Pixelmon party storage behavior, and egg cleanup.

## Good first issues

- PlaceholderAPI integration for `messages.yml`.
- PC-storage parent selection.
- Better automated tests around pair compatibility and party-storage edge cases.
- Async or batched environment scanning for very large networks.

## Guidelines

- Match existing code style; keep changes focused.
- Avoid spreading NMS/runtime-specific calls outside `PixelmonHook`.
- New behaviour should be **config-driven** (add keys with sane defaults; never
  hard-code numbers a server owner would want to tune).
- Update `messages.yml` for any new player-facing string.
- No proprietary jars, world saves, or server data in commits.

## PRs

Open an issue first for anything non-trivial. Describe the Pixelmon/Arclight
versions you tested against. By contributing you agree your work is licensed
under the project's [MIT License](LICENSE).
