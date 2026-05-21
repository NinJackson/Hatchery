# Hatchery — Admin Guide

Server-operator content for Hatchery `1.1.2`, currently maintained for the
Minecraft 1.21.1 / Arclight / Pixelmon 9.3.x server line. For the
player-facing manual see **[PLAYER-GUIDE.md](./PLAYER-GUIDE.md)**. For per-key
YAML config reference see **[CONFIG.md](./CONFIG.md)**.

> **1.21.1 port note.** The live build updates the Pixelmon bridge for the
> `v1_21_R1` CraftBukkit package and Pixelmon 9.3.x storage behavior. Parent
> placement now clears both active and original party storage, refreshes the
> client, and refuses placement if Pixelmon still reports that Pokemon UUID in
> the party. Eggs also have held items cleared before delivery.
>
> 📌 **1.1.1 block change.** Hatchery binds to Pixelmon 9.x's
> `pixelmon:<colour>_day_care` blocks (16 dyed variants) instead of the
> defunct `pixelmon:ranch_block`. Configs from earlier versions are
> auto-migrated — the legacy `daycare.block` key is still read and merged
> into the new `daycare.blocks` list. See [`CONFIG.md`](./CONFIG.md).
>
> 📌 **1.1.2 GUI polish.** Non-interactive menu slots are now filled with
> a configurable block (see `gui.filler-item` in `config.yml`) so live
> slots stand out. The legacy zero-width-space inventory title marker has
> been dropped — titles now render exactly as configured in `messages.yml`.

---

## Contents

- [Permission reference](#permission-reference)
- [`/hatchery` admin command](#hatchery-admin-command)
  - [`reload`](#reload)
  - [`list`](#list)
  - [`give-hourglass`](#give-hourglass)
  - [`give-upgrade`](#give-upgrade)
  - [`force-egg`](#force-egg)
  - [`remove`](#remove)
- [LuckPerms `hatchery.maxdaycares` meta](#luckperms-hatcherymaxdaycares-meta)
- [World blacklist](#world-blacklist)
- [Storage backends](#storage-backends)
- [Logs & troubleshooting](#logs--troubleshooting)
- [Backups & disaster recovery](#backups--disaster-recovery)
- [Operational notes (caching, dirty-save, particle interval)](#operational-notes)

---

## Permission reference

| Node | Default | What it grants |
|---|---|---|
| `hatchery.admin`            | op   | All `/hatchery` subcommands |
| `hatchery.use`              | true | Place / open / breed daycares (player-side) |
| `hatchery.hourglass.<tier>` | true | Use a configured hourglass tier (one node per tier in `hourglasses.yml`) |

The hourglass nodes are advertised at plugin load and are also checked at
right-click time in `HourglassService.tryApply` — so you can revoke a tier
from a group cleanly and players will get the configured `no-permission`
message on use.

---

## `/hatchery` admin command

Aliases: `/osb`, `/oldschoolbreeding`. Requires `hatchery.admin`. Tab
completion is wired for all sub-commands; players online + tier IDs from
`hourglasses.yml` are offered as appropriate.

### `reload`

```
/hatchery reload
```

Re-reads all four YAML configs in `plugins/Hatchery/`:

- `config.yml`
- `environment-points.yml`
- `hourglasses.yml`
- `messages.yml`

In-memory state (cached env-points, dirty flags, open menus) is preserved.
The next breeding tick picks up the new configs.

> ⚠️ Switching **storage backends** (`sqlite ↔ mysql`) requires a full
> **server restart** — the connection pool is built only at `onEnable`.

### `list`

```
/hatchery list
```

Prints the total registered daycare count. Useful for sanity-checking
post-restart that the storage backend loaded what you expected.

For per-daycare detail, query the storage directly — see [Backups &
disaster recovery](#backups--disaster-recovery).

### `give-hourglass`

```
/hatchery give-hourglass <player> <tier> [amount]
```

| Arg | Meaning |
|---|---|
| `<player>` | Exact (case-sensitive) online player name |
| `<tier>`   | Hourglass tier id from `hourglasses.yml` (e.g. `bronze`, `silver`, `gold`) |
| `[amount]` | Stack size — optional, default `1` |

Builds a tagged hourglass item (display name + lore from `hourglasses.yml`,
plus the persistent tag `hatchery:hourglass_id=<tier>`) and tries to add it to
the player's inventory. Any **overflow** items are dropped at the player's
feet.

Confirmation message:

```
Gave <amount> <tier> hourglass(es) to <player>.
```

Failures:

- `Player is not online: <name>` — exact-match lookup failed
- `Unknown hourglass tier: <tier>` — no such id in `hourglasses.yml`

### `give-upgrade`

```
/hatchery give-upgrade <player> [amount]
```

Hands out the **upgrade item** as configured under `daycare.upgrade` in
`config.yml`. Defaults to a tagged diamond block.

Just like `give-hourglass`, the item is built with display name, lore, and a
persistent tag (`hatchery:upgrade_id=primary`). Overflow drops at the player's
feet.

### `force-egg`

```
/hatchery force-egg <player|daycare-id>
```

Resolves a daycare in one of two ways:

| Argument form | Lookup |
|---|---|
| **UUID** (full daycare id from `list`/storage) | direct ID lookup |
| **Player name** | the first daycare owned by that player |

If a daycare is found, `BreedingEngine.forceEgg(daycare)` bumps progress to
the threshold and materialises one egg. Useful for testing/replaying breeding
flows or compensating a player whose egg generation got stuck.

Output:

- `Forced egg for daycare <uuid>.`
- `No matching daycare found.` — neither a valid UUID nor a known player with a daycare
- `Could not force an egg for that daycare.` — daycare exists but pair is missing/incompatible, or the egg cap is already hit

### `remove`

```
/hatchery remove <daycare-id>
```

Unregisters a daycare by exact UUID (get IDs from the storage table —
`list` doesn't print them). The world block stays in place — only the plugin's
registration is removed. The env-points cache for that daycare is invalidated.

Use this to clean up "orphan" registrations after manual world edits or to
forcibly release a player's slot without breaking their ranch block.

Output:

- `Removed daycare <uuid>.`
- `No daycare with id: <input>` — UUID didn't parse or no match in storage

---

## LuckPerms `hatchery.maxdaycares` meta

When LuckPerms is installed, Hatchery reads the `hatchery.maxdaycares` **meta**
key for the player at placement time. Semantics in 1.1.0:

| Meta value | Resulting cap |
|---|---|
| absent / not set | `daycare.max-per-player-default` from `config.yml` |
| valid integer ≥ 0 | that exact value (authoritative — can raise *or* lower) |
| `0`              | placement is blocked — player gets `daycare.max-reached` |
| non-numeric      | falls back to the default |

Examples:

```sh
# raise a donor cap to 5
lp user Alex meta set hatchery.maxdaycares 5

# revoke placement entirely without removing hatchery.use
lp user Greg meta set hatchery.maxdaycares 0

# remove the override (back to default)
lp user Alex meta unset hatchery.maxdaycares
```

> 📌 **1.0.0 → 1.1.0 change** — in 1.0.0 this meta could only raise the cap
> (`Math.max(default, meta)`). 1.1.0 honors the meta value directly when
> present.

---

## World blacklist

`config.yml: worlds.blacklisted` — array of world names where Day Care block
placement is denied. Default: `[adventure, build]`. Trying to place in a
blacklisted world cancels the placement and sends `daycare.disabled-world`.

`config.yml: worlds.use-vanilla-pixelmon` — worlds where Hatchery explicitly
**does not** intervene; Pixelmon's vanilla Day Care GUI runs normally on
right-click. Default: empty list.

### Pixelmon Day Care GUI suppression

When a player right-clicks one of the blocks listed in `daycare.blocks`,
Hatchery's `DaycareInteractListener` cancels the Bukkit `PlayerInteractEvent`.
On Arclight this short-circuits vanilla's `useItemOn` flow before
`Block#use()` runs, so Pixelmon's own `DayCareBlock#use` never executes and
its GUI never opens.

If you ever want Pixelmon's vanilla Day Care to coexist with Hatchery, either:

- **Per-world**, add that world's name to `worlds.use-vanilla-pixelmon` and
  Hatchery will skip lifecycle registration there entirely; OR
- **Per-colour**, narrow `daycare.blocks` to exclude specific colour variants
  — any colour not in the list keeps its vanilla behaviour. E.g. leave
  `pixelmon:pink_day_care` out of the list to let players use Pixelmon's own
  Day Care system on pink blocks only.

---

## Storage backends

### SQLite (default)

```yaml
storage:
  type: sqlite
  file: data.db
```

Lives at `plugins/Hatchery/data.db`. HikariCP pool (2 connections). Zero
external dependencies. **Recommended for small/single-shard setups.**

### MySQL

```yaml
storage:
  type: mysql
  mysql:
    host: 127.0.0.1
    port: 3306
    database: hatchery
    user: hatchery
    password: changeme
```

JDBC: `com.mysql:mysql-connector-j:8.3.0`. HikariCP pool size 5. Schema is
auto-created on first connect:

```sql
CREATE TABLE IF NOT EXISTS daycares (
  id        VARCHAR(36) PRIMARY KEY,
  owner     VARCHAR(36) NOT NULL,
  world     VARCHAR(64) NOT NULL,
  x         INT NOT NULL,
  y         INT NOT NULL,
  z         INT NOT NULL,
  upgrades  INT NOT NULL DEFAULT 0,
  points    INT NOT NULL DEFAULT 0,
  eggs      INT NOT NULL DEFAULT 0,
  pair_json TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

**Switching backends does not auto-migrate data.** Export from one and
import to the other manually if needed.

---

## Logs & troubleshooting

Hatchery's lines are tagged `[Hatchery]` in the console. Key boot sequence:

```
[Hatchery] Loading Hatchery v1.1.2
[Hatchery] Enabling Hatchery v1.1.2
[Hatchery] Loaded N daycare(s).
[Hatchery] Hatchery enabled (storage=sqlite).
```

### Common errors

| Error | Cause | Fix |
|---|---|---|
| `Pixelmon is not loaded. Hatchery requires Pixelmon to function. Disabling.` | The Pixelmon mod jar is missing or didn't load | Check `mods/` and the early Forge load output |
| `Failed to initialize storage backend (mysql).` | JDBC URL or credentials wrong | Verify connection from the server host with the same creds |
| `Failed to load configuration. Disabling.` | A YAML file has a parser error | Run `yamllint plugins/Hatchery/*.yml` or check for stray tabs / unquoted strings |
| `addToParty failed: ...` in console when collecting an egg | Pixelmon storage subsystem returned an error | Player should try again; if persistent, check the Pixelmon log for related entries |
| `Refusing daycare placement: Pixelmon party slot ... still contains ... after removal.` | Pixelmon still reported the parent in active/original party storage after Hatchery tried to remove it | Placement is intentionally blocked to prevent a Pokemon dupe; gather the log line, player UUID, and exact Pixelmon build before retrying |

### Players reporting "breeding stuck"

Run through this checklist:

1. `/hatchery list` — does the daycare even exist?
2. The chunk containing the daycare must be **loaded**. Walk over there and
   open the menu — does the Status panel update?
3. Status panel — is `Eggs ready` at the cap? Player must collect first.
4. Status panel — does it show `These Pokemon are not compatible.`?
5. Open `latest.log` and `grep "Hatchery"` for warnings around the time the
   player saw the problem.

If you can reproduce it, `/hatchery force-egg <player>` is a clean way to
unblock them without giving away material rewards (it just emits one egg from
their existing pair).

### Players reporting duplicate parents

The 1.21.1 build contains two layers of protection:

- Parent placement is considered successful only after Pixelmon no longer
  reports that Pokemon UUID in either the active party or original party array.
- If a stale daycare record from an older build already contains a Pokemon that
  is still in the owner's party, shift-clicking the parent slot clears the
  daycare-side copy instead of adding another copy to the party.

If a player can still duplicate a parent, capture the exact reproduction steps
and check the log for `Refusing daycare placement`. The most useful evidence is
the parent slot, Pokemon UUID, and whether the player was in any Pixelmon
temporary-party mode at the time.

---

## Backups & disaster recovery

### SQLite

```sh
# Cold backup (stop server first for atomicity)
cp plugins/Hatchery/data.db plugins/Hatchery/data.db.bak-$(date +%Y%m%d)

# Inspect live data
sqlite3 plugins/Hatchery/data.db
sqlite> .schema daycares
sqlite> SELECT id, owner, world, x, y, z, upgrades, eggs FROM daycares;
```

Drop a single rogue daycare (server stopped):

```sql
DELETE FROM daycares WHERE id = '<uuid>';
```

### MySQL

Standard `mysqldump`:

```sh
mysqldump -u hatchery -p hatchery daycares > hatchery-daycares-$(date +%Y%m%d).sql
```

### Rolling back a bad config

`config.yml`, `environment-points.yml`, `hourglasses.yml`, and `messages.yml`
are read fresh on `/hatchery reload`. To roll back, replace any of them with
a backup copy and reload — no restart needed unless you flipped storage type.

---

## Operational notes

### Env-points cache

Each daycare's environment-points scan result is cached in
`BreedingEngine.envPointsCache` (keyed by `Daycare#getId()`). The
`BlockChangeListener` invalidates the cache for any daycare whose centre is
within `scanRadius+1` blocks of a changed block. Events watched:

- `BlockPlaceEvent`, `BlockBreakEvent`
- `BlockFromToEvent` (fluids flowing)
- `LeavesDecayEvent`, `BlockFadeEvent`, `BlockBurnEvent`
- `StructureGrowEvent` (per-block in the structure)
- `EntityChangeBlockEvent` (falling sand etc.)

Apply an upgrade or change the parent pair also invalidates the cache. If you
want to force a re-scan for everyone (e.g. after a world-edit operation that
touched many blocks but didn't fire Bukkit events), `/hatchery reload`
doesn't touch the cache, but `/hatchery remove <id>` followed by re-placing
the ranch will. For mass cases, a server restart is simpler.

### Dirty-save throttling

`Daycare` now tracks a transient `dirty` flag (flipped by all setters).
`BreedingEngine.tick()` only calls `Storage.saveDaycare(d)` when `d.isDirty()`
is true, then immediately calls `d.markClean()`. Idle ticks (no pair active,
no progress) are now zero DB writes — useful when running many daycares with
MySQL.

If you need to force-persist all in-memory state (before a manual backup,
say), `/hatchery reload` is **not** sufficient — it doesn't flush. The
plugin auto-saves on shutdown via `onDisable`, so a clean server stop is the
canonical way to be sure everything is on disk.

### Particle interval

`particles.during-breeding.interval-seconds` is now honored (it was ignored
in 1.0.0). The plugin records `Daycare#lastBreedingParticleTick` and skips
the particle spawn if `now - last < intervalSeconds * 1000`. The default of
`5` seconds smooths out the previous "particle every tick" spam.

`particles.egg-ready.interval-seconds` similarly throttles the "egg ready"
particle burst.
