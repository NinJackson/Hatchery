# Contributing to Hatchery

Thanks for your interest! Hatchery is a community-friendly, MIT-licensed plugin.

## Dev setup

- **Java 17**, **Gradle 8.7**, Shadow plugin (see `build.gradle`).
- Supply the compile-only jars in `libs/` (see [`libs/README.md`](libs/README.md)).
  They are `.gitignore`d and **must never be committed** (non-redistributable).
- Build: `gradle shadowJar` → `build/libs/Hatchery-<version>.jar`.
- Test against **Arclight 1.20.2 + Pixelmon 9.2.x** (Arclight bridges Bukkit↔Forge).

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

**Key principle:** all Pixelmon/NMS coupling is **reflective and isolated**.
`pixelmon/PixelmonHook.java` is the single file that touches the Pixelmon API —
keep it that way so Pixelmon version bumps stay a one-file change.

## Good first issues (planned work)

- Hourglass right-click consumption + tick advancement.
- Environment Upgrade item: right-click apply + drop-on-break payout.
- `BlockChangeListener`: incremental env-point cache invalidation (replace the
  per-tick full rescan).
- Particle effect polish (`particles` config is wired).
- Remaining `/hatchery` admin subcommands: `give-hourglass`, `give-upgrade`,
  `force-egg`, `remove`.
- PlaceholderAPI integration for `messages.yml`.

## Guidelines

- Match existing code style; keep changes focused.
- Don't introduce hard NMS calls — go through reflection helpers.
- New behaviour should be **config-driven** (add keys with sane defaults; never
  hard-code numbers a server owner would want to tune).
- Update `messages.yml` for any new player-facing string.
- No proprietary jars, world saves, or server data in commits.

## PRs

Open an issue first for anything non-trivial. Describe the Pixelmon/Arclight
versions you tested against. By contributing you agree your work is licensed
under the project's [MIT License](LICENSE).
