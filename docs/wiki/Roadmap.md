# Roadmap

Status as of Hatchery `1.1.2`.

## Working today

- Bootstrap, config loading, **SQLite + MySQL** storage (HikariCP).
- Daycare **register/unregister** via block place/break — world-blacklist &
  LuckPerms-meta cap aware.
- **BreedingEngine** tick loop; **pauses on chunk unload**.
- **EnvironmentScanner** with env-point cache invalidation on nearby block
  changes.
- Real **Pixelmon hook**: party access, reflective Pokémon NBT codec, strict
  compatibility (`EggGroup.canBreedWith` + gender + Ditto + undiscovered
  rejection), egg via `pokemon.makeEgg()`.
- **GUIs**: DaycareMenu + PartyPickerMenu with real sprite items.
- Commands `/daycares` and `/hatchery reload|list|give-hourglass|give-upgrade|force-egg|remove`.
- Hourglass right-click consumption + tick advancement.
- Upgrade item right-click apply + drop-on-break payout.
- Particle interval throttling.
- 1.21.1 duplicate protections: parent placement verifies Pixelmon party
  removal, stale daycare copies are cleared on retrieval, and generated eggs
  have held items cleared.
- All player text via `messages.yml`.

## Planned / possible future

- [ ] **PlaceholderAPI** integration for messages.
- PC-storage pairs (currently party-only).
- Point caching / async scan for very large networks.

## Known risks

- **Pixelmon API drift** — a major bump may shift storage, party, or breeding
  APIs; most version-sensitive code should remain in `PixelmonHook`.
- **NMS mappings** — Pokémon NBT codec is reflective vs Mojang-mapped names
  (Arclight provides these at runtime).

Contributions welcome — see
[CONTRIBUTING](https://github.com/NinJackson/Hatchery/blob/main/CONTRIBUTING.md).
The remaining items make good first issues.
