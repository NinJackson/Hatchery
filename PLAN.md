# Hatchery — Custom Classic-Breeding Plugin

> Historical planning document. This was written for the original 1.20.2 /
> Pixelmon 9.2.x implementation. The current maintained server line is 1.21.1 /
> Pixelmon 9.3.x; see `README.md` and `docs/` for live build and operations
> guidance.

> A Bukkit plugin running on Arclight (1.20.2) that recreates Pixelmon's pre-modern
> ranch-style breeding system on top of modern Pixelmon Day Care blocks, with full
> configurability for every mechanic.

## Goals

- Recreate the classic 1.12.2 / 1.16.5-era breeding experience that players used to
  with OldSchoolBreeding, but rewritten in-house and tailored to OracionMC.
- Every numeric / behavioural lever exposed through YAML configs.
- No dependency on closed-source paid plugins.
- Compatible with the rest of the 1.20.2 plugin stack (CMI, LuckPerms, GriefDefender,
  Multiverse, Pixelmon Reforged 9.2.x).

---

## Plan responses (from approval Q&A)

| Question                          | Decision                                  |
|-----------------------------------|-------------------------------------------|
| Pair compatibility                | **Strict** — egg-group + gender + species |
| Egg delivery                      | **GUI** — click "Collect Egg" button      |
| Pause when chunk unloaded         | **Yes**                                   |
| Legacy migration from OSB         | **Start fresh** (no import)               |
| Storage backend                   | **Both** SQLite & MySQL, config-toggled   |

---

## Architecture

**Language:** Java 17
**Build:** Gradle + shadow plugin (single deployable jar)
**Runtime:** Bukkit plugin (Arclight bridges to Forge for Pixelmon)
**Soft deps:** LuckPerms (for permission meta), PlaceholderAPI
**NMS strategy:** reflection-only for Mojang-mapped NMS so the plugin is mappings-agnostic

```
Hatchery/
├── build.gradle
├── settings.gradle
├── libs/
│   ├── Pixelmon-1.20.2-9.2.10.jar         # compileOnly
│   └── minecraft-server-1.20.2-srg.jar    # transitive type resolution only
├── src/main/java/gg/hatchery/
│   ├── Hatchery.java                       # entrypoint, owns managers
│   ├── config/
│   │   ├── ConfigManager.java
│   │   ├── MainConfig.java                  # config.yml
│   │   ├── EnvironmentConfig.java          # environment-points.yml
│   │   ├── MessagesConfig.java             # messages.yml
│   │   └── HourglassConfig.java            # hourglasses.yml
│   ├── daycare/
│   │   ├── Daycare.java                    # state model
│   │   └── DaycareManager.java             # global registry + caps + LP meta
│   ├── breeding/
│   │   ├── BreedingEngine.java             # tick loop, egg generation
│   │   └── EnvironmentScanner.java         # scan radius → points per type
│   ├── pixelmon/
│   │   ├── PixelmonHook.java               # all Pixelmon API access
│   │   └── PokemonNbtCodec.java            # reflective NBT base64 codec
│   ├── ui/
│   │   ├── HatcheryMenu.java               # menu interface
│   │   ├── MenuManager.java
│   │   ├── DaycareMenu.java                # 27-slot main GUI
│   │   └── PartyPickerMenu.java            # 9-slot party picker
│   ├── listeners/
│   │   ├── DaycareLifecycleListener.java   # place/break Pixelmon Day Care block
│   │   ├── DaycareInteractListener.java    # right-click Day Care → GUI
│   │   ├── BlockChangeListener.java        # env recalc (TODO)
│   │   └── InventoryClickListener.java     # routes clicks to menus
│   ├── commands/
│   │   ├── DaycaresCommand.java            # /daycares (player)
│   │   └── HatcheryAdminCommand.java       # /hatchery (admin)
│   ├── storage/
│   │   ├── Storage.java                    # interface
│   │   ├── SqliteStorage.java
│   │   ├── MysqlStorage.java
│   │   └── SchemaSql.java
│   └── util/
│       └── ItemBuilder.java
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    ├── environment-points.yml
    ├── messages.yml
    └── hourglasses.yml
```

---

## Feature checklist (config-driven)

- [x] **What blocks give how many points to specific types** — `environment-points.yml`
- [x] **Maximum daycares**, with **LuckPerms meta override** per player/group — `hatchery.maxdaycares` meta key
- [x] **What worlds are not OSB breeding** — `worlds.blacklisted` + `worlds.use-vanilla-pixelmon`
- [x] **Satisfaction levels**, configurable thresholds + speed multipliers — `satisfaction-levels` in config
- [ ] **Particles** during breeding & on egg-ready — defined in config; spawn code wired but not finished
- [x] **All messages** through `messages.yml` (admin command output is hard-coded)
- [ ] **Custom hourglasses** that advance breeding ticks — config defined; click handler TODO
- [x] **Upgrade item** identity + display + radius-per-level — config defined; right-click apply TODO
- [x] **Drop upgrades on break** toggle — `daycare.upgrade.drop-on-break`
- [x] **SQLite OR MySQL** storage backend — `storage.type`

---

## Configs (defaults shipped in jar resources)

### `config.yml`
Top-level switches: storage backend, breeding tick interval / points-needed, daycare
block ID, max-per-player default, LP meta key, scan radius, upgrade item identity,
satisfaction tiers, particle settings.

### `environment-points.yml`
Per-Pokemon-type block-point mapping (all 18 types) + universal bonuses applied
regardless of type.

### `messages.yml`
Every player-facing string with `{placeholder}` substitution, organised into
sections (`daycare.*`, `breeding.*`, `hourglass.*`, `upgrade.*`, `gui.*`).

### `hourglasses.yml`
Tiered hourglass items (bronze/silver/gold by default) — base item, display name,
lore, ticks-added, consume-on-use, permission.

---

## Build & deploy pipeline

1. Develop locally.
2. Build with Gradle 8.7 + OpenJDK 17:
   ```bash
   gradle shadowJar
   ```
3. Drop `build/libs/Hatchery-<version>.jar` into the server's `plugins/`
4. Restart the server

---

## Versions (v0.2 — current)

**Done:**
- Bootstrap, config loading, SQLite + MySQL storage
- Daycare registration via block-place / unregister via block-break (with world blacklist & LP-meta-aware max-per-player)
- BreedingEngine tick loop, paused when chunk unloaded
- EnvironmentScanner (full rescan each tick)
- Real Pixelmon hook: party access, Pokemon NBT codec (reflective), strict compat checks (`EggGroup.canBreedWith` + gender + ditto rules + undiscovered rejection), egg generation via `pokemon.makeEgg()`
- DaycareMenu + PartyPickerMenu with real sprite items (via `SpriteItemHelper` + reflective `CraftItemStack`)
- `/daycares` and `/hatchery` commands
- All player messages routed through `messages.yml`

**Open (v0.3+):**
- Hourglass right-click consumption + tick advancement
- Upgrade item right-click apply + drop-on-break payout
- `BlockChangeListener` cache invalidation (currently full rescan each tick)
- Particle effects (spawn code wired; needs more polish)
- Admin commands: `give-hourglass`, `give-upgrade`, `force-egg`, `remove`
- PlaceholderAPI integration for messages

---

## Test environment

- Stack: Arclight 1.20.2-1.0.3 + Forge 1.20.2-48.1.0 + Pixelmon 9.2.10

---

## Risks / known issues

1. **Pixelmon API drift** — if upgrading to 9.3+ (1.21.1), `EggGroup.canBreedWith` etc.
   may shift. The PixelmonHook is the only file that needs touching.
2. **NMS mapping** — Pokemon NBT codec uses reflection against Mojang-mapped names.
   Arclight provides those at runtime. If switching to Paper for vanilla, this still works.
3. **Full rescan each tick** — fine for ≤50 daycares; would need point caching beyond.
4. **No PCStorage support** — only the player's party (6 slots). Pulling from PC would
   need a follow-on feature.
