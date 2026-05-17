# Hatchery

**Classic Pixelmon Day Care breeding — the "OldSchool" ranch-style breeding
experience, rebuilt from scratch and fully configurable.**

Hatchery is an open-source Bukkit plugin (built for **Arclight 1.20.2**, which
bridges Bukkit ↔ Forge) that recreates the pre-modern, ranch-block-era Pixelmon
breeding loop on top of modern Pixelmon **Day Care** blocks. Players place a Day
Care, add a compatible pair from their party, shape the **environment** around
it to speed things up, and collect the egg from a GUI. Every numeric and
behavioural lever is exposed through YAML.

> Independent fan project. Not affiliated with Mojang, Microsoft, or the
> Pixelmon Modding Group. Pixelmon and the Minecraft server jar are **not**
> bundled — you supply them to build (see [`libs/README.md`](libs/README.md)).

---

## Table of contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [How breeding works](#how-breeding-works)
- [Building from source](#building-from-source)
- [Project status & roadmap](#project-status--roadmap)
- [Compatibility & known limitations](#compatibility--known-limitations)
- [Contributing](#contributing)
- [License](#license)

---

## Documentation

Full docs live in two places (same content):

- 📖 **[Wiki](../../wiki)** — browsable, cross-linked.
- 📁 **[`docs/wiki/`](docs/wiki/)** — in-repo mirror (always available), plus
  the player-facing **[`docs/PLAYER_GUIDE.md`](docs/PLAYER_GUIDE.md)**.

Start at **[Installation](docs/wiki/Installation.md)** ·
**[Configuration](docs/wiki/Configuration.md)** ·
**[Breeding Mechanics](docs/wiki/Breeding-Mechanics.md)** ·
**[Environment Points](docs/wiki/Environment-Points.md)**.

---

## Features

- **Day Care breeding blocks** — register a breeding station by placing any
  configured Pixelmon Day Care block (all 16 colours supported out of the box).
- **Strict pairing** — egg-group + gender + species compatibility, Ditto rules,
  and rejection of undiscovered Pokémon, using Pixelmon's own API.
- **Environment scoring** — blocks around the Day Care grant *points* toward the
  egg, weighted **per Pokémon type** (e.g. water blocks help Water types). Fully
  remappable in `environment-points.yml`.
- **Satisfaction tiers** — accumulated points map to named tiers (Unhappy →
  Blissful) with configurable thresholds and breeding-speed multipliers.
- **GUI-driven** — add Pokémon from a party picker, view environment points, and
  collect the egg from a clean inventory menu (real Pokémon sprite items).
- **Per-player daycare caps** — default cap with **LuckPerms meta** override
  (`hatchery.maxdaycares`) per player or group.
- **World rules** — blacklist worlds, or let specific worlds fall back to
  Pixelmon's own breeding.
- **Hourglasses** — tiered consumable items that fast-forward breeding ticks.
- **Environment Upgrade item** — right-click to expand a daycare's scan radius,
  with configurable max levels and drop-on-break.
- **Pluggable storage** — SQLite or MySQL, toggled in config (HikariCP pooled).
- **Every string configurable** — all player-facing text in `messages.yml`.
- **Mappings-agnostic** — Pixelmon/NMS access is reflective, so it survives
  remapping (Arclight) and most Pixelmon point releases.

## Requirements

| | |
|---|---|
| Server | **Arclight 1.20.2** (Forge + Bukkit hybrid). Paper/Spigot 1.20.2 works for non-Pixelmon parts but Pixelmon itself requires Forge/Arclight. |
| Java | **17** |
| Pixelmon | **9.2.x** (developed against 1.20.2 / 9.2.10) |
| Soft-deps | LuckPerms (per-player caps via meta), PlaceholderAPI |

## Installation

1. Build the jar (see [Building from source](#building-from-source)) or grab a
   release.
2. Drop `Hatchery-<version>.jar` into your server's `plugins/` folder.
3. Start the server once to generate the config files in `plugins/Hatchery/`.
4. Edit the configs to taste and run `/hatchery reload`.

## Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `/daycares` | `/dc` | Open your daycare list / GUI. |
| `/hatchery <…>` | `/osb`, `/oldschoolbreeding` | Admin: `reload`, `list`, `give-hourglass`, `give-upgrade`, `force-egg`, `remove`. |

## Permissions

| Node | Default | Purpose |
|------|---------|---------|
| `hatchery.use` | `true` | Use daycares. |
| `hatchery.admin` | `op` | Full admin access (`/hatchery`). |
| `hatchery.hourglass.bronze` | `true` | Use bronze hourglasses. |
| `hatchery.hourglass.silver` | `true` | Use silver hourglasses. |
| `hatchery.hourglass.gold` | `true` | Use gold hourglasses. |
| `hatchery.maxdaycares` *(LuckPerms meta)* | — | Per-player/group override of the max-daycares cap. |

## Configuration

Generated in `plugins/Hatchery/`:

| File | Controls |
|------|----------|
| `config.yml` | Storage backend, breeding tick interval & points needed, Day Care block IDs, per-player cap + LP meta key, scan radius, the Upgrade item, world rules, satisfaction tiers, particles. |
| `environment-points.yml` | Per-Pokémon-type block→points map (all 18 types) plus universal bonuses. |
| `hourglasses.yml` | Hourglass tiers (base item, name, lore, ticks-added, consume, permission). |
| `messages.yml` | Every player-facing string, `{placeholder}` substitution, prefix. |

See the [**Wiki**](../../wiki) for a full key-by-key reference, and
[`docs/PLAYER_GUIDE.md`](docs/PLAYER_GUIDE.md) for the player-facing explanation.

## How breeding works

1. **Place a Day Care block** (any configured colour) → it registers as *your*
   daycare (subject to your world + cap).
2. **Right-click it** → GUI. Add two party Pokémon; they must pass strict
   compatibility (egg group, gender, species/Ditto rules).
3. Each breeding tick (`tick-interval-seconds`), Hatchery scans blocks within
   the daycare's radius and awards **environment points** based on the pair's
   types (`environment-points.yml`). Points → a **satisfaction tier** with a
   **speed multiplier**.
4. When accumulated progress reaches `base-points-needed`, an **egg is ready** —
   collect it from the GUI.
5. Optionally **right-click an Hourglass** to fast-forward ticks, or apply an
   **Environment Upgrade** to widen the scan radius.

## Building from source

Hatchery uses **Gradle (8.7) + the Shadow plugin** and **Java 17**. A Gradle
wrapper is not committed; use a local Gradle 8.7 (the dev setup vendors one).

```bash
# 1. Supply the proprietary compile-only jars (see libs/README.md)
#    libs/Pixelmon-1.20.2-9.2.10.jar
#    libs/minecraft-server-1.20.2-srg.jar

# 2. Build the shaded plugin jar
gradle shadowJar          # or: ./gradlew shadowJar  if you add a wrapper

# 3. Output
#    build/libs/Hatchery-<version>.jar
```

No NMS is called directly; SQLite/MySQL/Hikari/SLF4J are shaded & relocated
under `gg.hatchery.libs.*`.

## Project status & roadmap

**Working:** config + SQLite/MySQL storage, daycare register/unregister
(world-blacklist + LP-meta cap aware), breeding tick loop (pauses on chunk
unload), environment scanner, real Pixelmon hook (party access, reflective
Pokémon NBT codec, strict compat, `makeEgg`), Daycare/Party GUIs with real
sprites, `/daycares` & `/hatchery`, all messages via `messages.yml`.

**Planned (v0.3+):** hourglass right-click consumption, upgrade-item apply +
drop-on-break payout, incremental environment recalculation (currently full
rescan per tick), particle polish, the remaining `/hatchery` admin subcommands,
PlaceholderAPI message integration. See [`PLAN.md`](PLAN.md) and the
[Roadmap wiki page](../../wiki/Roadmap).

## Compatibility & known limitations

- Designed for **Arclight 1.20.2** (reflective NMS = mappings-agnostic; also fine
  if a future Paper path is used for vanilla bits).
- **Pixelmon API drift:** a major Pixelmon bump (e.g. 9.3+/1.21) may shift
  `EggGroup.canBreedWith` etc.; `PixelmonHook` is the single file to adjust.
- Full environment rescan each tick — comfortable for ≲50 active daycares;
  larger scales want point caching (planned).
- **Party only** — pulls from the player's 6-slot party, not PC storage.

## Contributing

Issues and PRs welcome — see [`CONTRIBUTING.md`](CONTRIBUTING.md). The "Planned"
items above make good first contributions; `PixelmonHook` isolates all
Pixelmon-API coupling.

## License

[MIT](LICENSE) © 2026 NinJackson. Pixelmon/Pokémon/Minecraft are trademarks of
their respective owners; this is an independent fan project and bundles none of
their code.
