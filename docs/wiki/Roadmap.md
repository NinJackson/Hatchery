# Roadmap

Status as of the initial public release (from `PLAN.md`).

## Working today

- Bootstrap, config loading, **SQLite + MySQL** storage (HikariCP).
- Daycare **register/unregister** via block place/break — world-blacklist &
  LuckPerms-meta cap aware.
- **BreedingEngine** tick loop; **pauses on chunk unload**.
- **EnvironmentScanner** (full rescan each tick).
- Real **Pixelmon hook**: party access, reflective Pokémon NBT codec, strict
  compatibility (`EggGroup.canBreedWith` + gender + Ditto + undiscovered
  rejection), egg via `pokemon.makeEgg()`.
- **GUIs**: DaycareMenu + PartyPickerMenu with real sprite items.
- Commands `/daycares` and `/hatchery reload|list`.
- All player text via `messages.yml`.

## Planned (v0.3+)

- [ ] **Hourglass** right-click consumption + tick advancement.
- [ ] **Upgrade item** right-click apply + drop-on-break payout.
- [ ] `BlockChangeListener` incremental env-point cache invalidation (replace
      per-tick full rescan → scales past ~50 daycares).
- [ ] **Particle** effect polish (config wired).
- [ ] Remaining `/hatchery` admin subcommands: `give-hourglass`,
      `give-upgrade`, `force-egg`, `remove`.
- [ ] **PlaceholderAPI** integration for messages.

## Possible future

- PC-storage pairs (currently party-only).
- Point caching / async scan for very large networks.

## Known risks

- **Pixelmon API drift** — a major bump (9.3+/1.21) may shift
  `EggGroup.canBreedWith` etc.; only `PixelmonHook` should need changes.
- **NMS mappings** — Pokémon NBT codec is reflective vs Mojang-mapped names
  (Arclight provides these at runtime).

Contributions welcome — see
[CONTRIBUTING](https://github.com/NinJackson/Hatchery/blob/main/CONTRIBUTING.md).
The planned items make good first issues.
