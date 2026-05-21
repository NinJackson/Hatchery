# Hatchery — Configuration Reference

Per-key reference for the four YAML files Hatchery loads from its data folder
(`plugins/Hatchery/`). This document tracks `Hatchery 1.1.2` on the current
Minecraft 1.21.1 / Arclight / Pixelmon 9.3.x server line.

Related docs:

- **[PLAYER-GUIDE.md](./PLAYER-GUIDE.md)** — what the player sees / does
- **[ADMIN-GUIDE.md](./ADMIN-GUIDE.md)** — `/hatchery` command reference + ops

All four files are merged with packaged defaults on load — new keys added in
future plugin versions will appear automatically without overwriting your
existing values.

---

## `config.yml`

### `storage`

| Key | Type | Default | Notes |
|---|---|---|---|
| `storage.type`             | `sqlite` \| `mysql` | `sqlite` | Backend switch — **requires server restart** to apply |
| `storage.file`             | string | `data.db` | SQLite file name inside `plugins/Hatchery/` |
| `storage.mysql.host`       | string | `127.0.0.1` | MySQL host |
| `storage.mysql.port`       | int    | `3306` | MySQL port |
| `storage.mysql.database`   | string | `hatchery` | Database name; auto-creates `daycares` table on connect |
| `storage.mysql.user`       | string | `hatchery` | |
| `storage.mysql.password`   | string | `changeme` | |

### `breeding`

| Key | Type | Default | Notes |
|---|---|---|---|
| `breeding.tick-interval-seconds` | int | `60` | How often each daycare advances; minimum effective value is 1s |
| `breeding.base-points-needed`    | int | `100` | Progress threshold to emit one egg |
| `breeding.max-eggs-per-daycare`  | int | `1` | Once reached, breeding stalls until eggs are collected |

### `daycare`

| Key | Type | Default | Notes |
|---|---|---|---|
| `daycare.blocks`                 | list&lt;namespaced ID&gt; | all 16 `pixelmon:<colour>_day_care` IDs (see below) | Each placed instance of any listed block registers a Hatchery daycare. Pixelmon's own Day Care GUI is suppressed on right-click. |
| `daycare.block` *(legacy)*       | namespaced ID | — | **Deprecated.** If present, merged into the `blocks` set for backward compatibility with 1.0.0 / 1.1.0 configs. Prefer `daycare.blocks`. |
| `daycare.max-per-player-default` | int | `1` | Per-player cap when no LP meta override is present |
| `daycare.permission-meta-key`    | string | `hatchery.maxdaycares` | LP meta key checked at placement time |
| `daycare.base-scan-radius`       | int | `5` | Blocks scanned around the daycare in X/Z; vertical span is fixed at ±2 |

#### Default `daycare.blocks`

Pixelmon 9.x ships **only dyed Day Care blocks** — no neutral or `ranch_block`
variant. Hatchery's default list covers all 16 colours so any of them works
out of the box:

```yaml
daycare:
  blocks:
    - pixelmon:black_day_care
    - pixelmon:blue_day_care
    - pixelmon:brown_day_care
    - pixelmon:cyan_day_care
    - pixelmon:gray_day_care
    - pixelmon:green_day_care
    - pixelmon:light_blue_day_care
    - pixelmon:light_gray_day_care
    - pixelmon:lime_day_care
    - pixelmon:magenta_day_care
    - pixelmon:orange_day_care
    - pixelmon:pink_day_care
    - pixelmon:purple_day_care
    - pixelmon:red_day_care
    - pixelmon:white_day_care
    - pixelmon:yellow_day_care
```

Narrow the list if you want certain colours reserved for events / VIPs /
themed builds. Block IDs are compared case-insensitively against the
Bukkit `Material.getKey()` of the placed block.

### `daycare.upgrade`

| Key | Type | Default | Notes |
|---|---|---|---|
| `daycare.upgrade.item`              | namespaced ID | `minecraft:diamond_block` | Material the upgrade item is built on |
| `daycare.upgrade.display-name`      | string         | `&b&lDaycare Environment Upgrade` | `&` color codes supported |
| `daycare.upgrade.lore`              | list<string>   | (3-line default) | `{radius-per-level}` and `{max-upgrades}` placeholders honored |
| `daycare.upgrade.radius-per-level`  | int            | `2` | Blocks added to scan radius per applied upgrade |
| `daycare.upgrade.max-upgrades`      | int            | `5` | Cap on applied upgrades |
| `daycare.upgrade.drop-on-break`     | bool           | `true` | When `true`, breaking a daycare drops a stack of upgrade items equal to the applied level |

### `worlds`

| Key | Type | Default | Notes |
|---|---|---|---|
| `worlds.blacklisted`         | list&lt;string&gt; | `[adventure, build]` | Placing a Day Care block in these worlds is rejected |
| `worlds.use-vanilla-pixelmon`| list&lt;string&gt; | `[]` | Hatchery doesn't intervene; Pixelmon's vanilla Day Care GUI runs normally on these worlds |

### `satisfaction-levels`

List of level definitions. Each entry:

| Key | Type | Notes |
|---|---|---|
| `threshold`  | int    | Minimum environment points for this level |
| `name`       | string | Display name in Status panel |
| `color`      | string | `&`-prefixed color code |
| `speed-mult` | float  | Multiplier applied to progress-per-tick |

The highest threshold ≤ current envPoints wins; lower entries are otherwise
ignored. The default config defines `Unhappy / Content / Happy / Ecstatic /
Blissful` at thresholds `0 / 25 / 50 / 75 / 100`.

### `gui`

| Key | Type | Default | Notes |
|---|---|---|---|
| `gui.filler-item` | namespaced ID | `minecraft:black_stained_glass_pane` | Block/item used to "fill" every non-interactive slot in the Daycare and Party Picker menus so the live slots stand out. Falls back to black stained glass pane if the configured ID can't be resolved. |

Note: the inventory **title** is set in `messages.yml` under `gui.title`,
not here. Hatchery 1.1.2 dropped the legacy zero-width-space title marker —
the title is now rendered exactly as configured.

### `particles`

Two sub-sections share the same shape: `particles.during-breeding` and
`particles.egg-ready`.

| Key | Type | Default | Notes |
|---|---|---|---|
| `enabled`           | bool   | `true` | |
| `type`              | string | `HEART` (breeding), `VILLAGER_HAPPY` (egg-ready) | Bukkit `Particle` enum name |
| `count`             | int    | `3` / `10` | Particle count per emit |
| `interval-seconds`  | int    | `5` / `2` | **Honored in 1.1.0** — minimum spacing between emits per daycare. Set `0` for every breeding tick. |
| `offset.x`/`y`/`z`  | float  | `0.5 / 1.0 / 0.5` | Offset from the ranch block's origin corner |

---

## `environment-points.yml`

Environment points are scored per Pokémon type, then summed across **all
distinct types in the active pair** plus a universal-bonus table. Block IDs
are namespaced and case-insensitive.

```yaml
type-points:
  water:
    minecraft:water:        3
    minecraft:kelp:         1
    minecraft:blue_ice:     4
  grass:
    minecraft:grass_block:  2
    minecraft:flowering_azalea: 2
    pixelmon:apricorn_log:  3
  fire:
    minecraft:magma_block:  3
    minecraft:campfire:     2
    minecraft:lava:         4
  # …18 types total

universal-bonuses:
  pixelmon:fossil_machine_top: 2
  minecraft:flower_pot: 1
```

The packaged default covers every Pokémon type — extend it per server taste.
The block IDs you write here are matched against the **Bukkit Material
namespace key** of each block in the scan volume. On Arclight, modded Pixelmon
blocks expose namespaced material keys like `pixelmon:apricorn_log`.

---

## `hourglasses.yml`

Each entry under `hourglasses:` defines a single tier of admin-issued item.
Tier IDs are arbitrary strings — what matters is the `<id>` argument to
`/hatchery give-hourglass`.

Per-tier keys:

| Key | Type | Notes |
|---|---|---|
| `base-item`    | namespaced ID | Material used for the stack; falls back to `SAND` if unknown |
| `display-name` | string        | `&` color codes |
| `lore`         | list<string>  | Each entry is a line |
| `ticks-added`  | int           | Progress added per right-click |
| `consume`      | bool          | If `true` (default), 1 item is removed from the held stack on success |
| `permission`   | string        | Required to apply this tier; default `hatchery.hourglass.<id>` |

A tier is recognised at runtime by the persistent tag `hatchery:hourglass_id`
which `give-hourglass` bakes into the item — display-name matching is not
used. Plain vanilla items, or items issued via `/give`, **will not work** as
hourglasses even if their visible properties match.

---

## `messages.yml`

Every player-facing string. Admin command output is intentionally hard-coded
(not configurable).

- `&` color codes translated to `§`.
- `{placeholder}` substitution at runtime — the placeholder set is fixed per
  message, listed below.
- `prefix` is prepended to all messages.

| Key | Placeholders | Sent when |
|---|---|---|
| `daycare.placed`           | — | Ranch block accepted; daycare registered |
| `daycare.removed`          | — | Ranch broken; daycare unregistered |
| `daycare.max-reached`      | `{max}` | Placement denied because owner is at cap |
| `daycare.disabled-world`   | — | Placement denied in a blacklisted world |
| `breeding.egg-ready`       | `{world}` `{x}` `{y}` `{z}` | One egg has been generated; owner notified |
| `breeding.egg-collected`   | `{species}` | Collect-egg button used successfully |
| `breeding.pair-incompatible` | — | Reserved; not emitted by 1.1.2 code |
| `breeding.satisfaction-changed` | `{color}` `{level}` `{points}` | Reserved; not emitted by 1.1.2 code |
| `hourglass.used`           | `{ticks}` | Hourglass right-click succeeded |
| `hourglass.no-active-breeding` | — | Hourglass used on a daycare with no pair |
| `hourglass.no-permission`  | — | Player lacked the tier permission |
| `upgrade.applied`          | `{radius}` | Upgrade item right-click succeeded |
| `upgrade.max-reached`      | — | Tried to upgrade a daycare at max level |
| `gui.title`                | — | Title bar of the daycare GUI |
| `gui.add-pokemon`          | — | Reserved; not used by core 1.1.2 GUI |
| `gui.view-environment`     | — | Reserved; not used by core 1.1.2 GUI |
| `gui.collect-egg`          | — | Reserved; not used by core 1.1.2 GUI |
