# Configuration

All files live in `plugins/Hatchery/`. Apply edits with `/hatchery reload`.
Color codes use `&`. Run `/hatchery reload` after every change.

---

## `config.yml`

### `storage`
| Key | Default | Meaning |
|-----|---------|---------|
| `type` | `sqlite` | `sqlite` or `mysql` |
| `file` | `data.db` | SQLite filename (under plugins/Hatchery) |
| `mysql.host/port/database/user/password` | localhost/3306/hatchery/hatchery/changeme | MySQL connection (HikariCP pooled) |

### `breeding`
| Key | Default | Meaning |
|-----|---------|---------|
| `tick-interval-seconds` | `60` | Seconds between breeding ticks |
| `base-points-needed` | `100` | Progress required for an egg |
| `max-eggs-per-daycare` | `1` | Uncollected eggs a daycare can hold |

### `daycare`
| Key | Default | Meaning |
|-----|---------|---------|
| `block` | `pixelmon:white_day_care` | Back-compat single block |
| `blocks` | all 16 `pixelmon:<color>_day_care` | Blocks that register a daycare |
| `max-per-player-default` | `1` | Default cap per player |
| `permission-meta-key` | `hatchery.maxdaycares` | LuckPerms **meta** key overriding the cap |
| `base-scan-radius` | `5` | Block scan radius before upgrades |
| `upgrade.item` | `minecraft:diamond_block` | Item that, right-clicked on a daycare, adds radius |
| `upgrade.display-name` / `lore` | themed | Upgrade item identity (placeholders: `{radius-per-level}`, `{max-upgrades}`) |
| `upgrade.radius-per-level` | `2` | Radius added per upgrade |
| `upgrade.max-upgrades` | `5` | Max upgrades per daycare |
| `upgrade.drop-on-break` | `true` | Refund upgrades when the daycare is broken |

### `worlds`
| Key | Default | Meaning |
|-----|---------|---------|
| `blacklisted` | `[adventure, build]` | Daycares cannot be created here |
| `use-vanilla-pixelmon` | `[]` | Worlds that fall back to Pixelmon's own breeding |

### `satisfaction-levels`
Ordered list; each: `{ threshold, name, color, speed-mult }`. The highest
threshold a daycare's points meet sets its tier and breeding **speed
multiplier**. Defaults:

| Threshold | Name | Color | Speed |
|-----------|------|-------|-------|
| 0 | Unhappy | `&c` | 0.5× |
| 25 | Content | `&e` | 1.0× |
| 50 | Happy | `&a` | 1.5× |
| 75 | Ecstatic | `&b` | 2.0× |
| 100 | Blissful | `&d` | 3.0× |

### `particles`
`during-breeding` and `egg-ready` blocks: `enabled`, `type` (Bukkit Particle),
`count`, `interval-seconds`, `offset {x,y,z}`. *(Spawn code is wired; visual
polish is on the [Roadmap](Roadmap).)*

---

## `environment-points.yml`

Per-Pokémon-**type** block→points map for all 18 types, plus universal bonuses.
Block IDs are namespaced and case-insensitive. See [Environment Points](Environment-Points)
for how scoring works and the full default table.

---

## `hourglasses.yml`

Tiered consumables that advance breeding ticks when right-clicked on a daycare.
Default `bronze` / `silver` / `gold`:

| Key | Meaning |
|-----|---------|
| `base-item` | Item used as the hourglass (default `pixelmon:hourglass`) |
| `display-name` / `lore` | Identity used to match the held item |
| `ticks-added` | Ticks fast-forwarded (10 / 25 / 50) |
| `consume` | Consume the item on use (`true`) |
| `permission` | Required node (`hatchery.hourglass.<tier>`) |

Add your own tiers by adding more keys under `hourglasses:`.

---

## `messages.yml`

Every player-facing string, with a global `prefix` and `{placeholder}`
substitution, grouped: `daycare.*`, `breeding.*`, `hourglass.*`, `upgrade.*`,
`gui.*`. Admin command output is hard-coded (not localized). Example keys:
`daycare.max-reached` (`{max}`), `breeding.egg-ready` (`{world} {x} {y} {z}`),
`breeding.satisfaction-changed` (`{color}{level}{points}`).
