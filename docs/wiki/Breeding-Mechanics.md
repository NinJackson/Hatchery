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

On the 1.21.1 server line, Hatchery treats parent placement as successful only
after Pixelmon no longer reports that Pokemon UUID in the player's active or
original party storage. If Pixelmon still reports the UUID, placement is
refused to prevent a duplicate parent.

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
Hatchery clears the generated egg's held item before delivery so parent-held
items cannot be copied onto eggs.
`max-eggs-per-daycare` caps uncollected eggs.

## 5. Speed-ups

- **Hourglasses** — right-click with one to add ticks instantly.
- **Environment Upgrade** — right-click to widen the scan radius (more blocks
  count → higher tiers).

See [Hourglasses & Upgrades](Hourglasses-and-Upgrades).

## Performance note

Environment results are cached per daycare and invalidated when nearby blocks
change, parents change, or an upgrade is applied. Very large networks may
still eventually want async scanning, but ordinary active daycare counts avoid
the old full-rescan-on-every-tick behavior.
