# Breeding Mechanics

## 1. Register a daycare

Placing any block in `daycare.blocks` registers a daycare owned by the placer,
subject to:

- **World rules** — blocked in `worlds.blacklisted`; in `worlds.use-vanilla-pixelmon`
  Hatchery defers to Pixelmon's own breeding.
- **Cap** — the player's resolved cap (`hatchery.maxdaycares` LuckPerms meta, or
  `daycare.max-per-player-default`).

Breaking the block unregisters it (and refunds upgrades if
`upgrade.drop-on-break`).

## 2. Pair compatibility (strict)

Add two Pokémon from your **party** (PC not supported). They must pass, via
Pixelmon's own API:

- **Egg group** — `EggGroup.canBreedWith` (overlapping groups).
- **Gender** — opposite genders, **or** a valid **Ditto** pairing.
- **Discovered** — undiscovered/mystery Pokémon are rejected.

Incompatible pairs are refused with `breeding.pair-incompatible`.

## 3. Ticks, environment & satisfaction

Every `breeding.tick-interval-seconds` (while the chunk is loaded — breeding
**pauses** on chunk unload):

1. `EnvironmentScanner` scans blocks within the daycare's radius
   (`base-scan-radius` + upgrades × `radius-per-level`).
2. Each block contributes points based on the **pair's types** using
   `environment-points.yml` (+ universal bonuses). See
   [Environment Points](Environment-Points).
3. Total points → a **satisfaction tier** (`satisfaction-levels`) → a
   **speed multiplier** (0.5×–3.0× by default).
4. Breeding progress advances by the tier's multiplier. Tier changes notify the
   owner (`breeding.satisfaction-changed`).

## 4. Egg ready & collection

When progress reaches `breeding.base-points-needed`, the egg is marked ready
(`breeding.egg-ready` with coordinates). The owner right-clicks the daycare and
clicks **Collect Egg** in the GUI (`breeding.egg-collected`). The egg is created
through Pixelmon's `pokemon.makeEgg()` and hatches via normal Pixelmon rules.
`max-eggs-per-daycare` caps uncollected eggs.

## 5. Speed-ups

- **Hourglasses** — right-click with one to add ticks instantly.
- **Environment Upgrade** — right-click to widen the scan radius (more blocks
  count → higher tiers).

See [Hourglasses & Upgrades](Hourglasses-and-Upgrades).

## Performance note

The scanner currently does a **full rescan each tick** — fine for ≲50 active
daycares. Incremental invalidation (`BlockChangeListener`) is on the
[Roadmap](Roadmap) for larger networks.
