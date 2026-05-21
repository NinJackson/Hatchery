# Installation

## Requirements

| | |
|---|---|
| Server | **Arclight 1.21.1** (Forge + Bukkit hybrid). Pixelmon requires Forge/Arclight. |
| Java | **21** for the 1.21.1 server line |
| Pixelmon | **9.3.x** (tested on 9.3.16) |
| Soft-deps | LuckPerms (per-player daycare caps via meta), PlaceholderAPI |

## Steps

1. Obtain `Hatchery-<version>.jar` — from a [release](https://github.com/NinJackson/Hatchery/releases) or by [building from source](Building-from-Source).
2. Stop the server. Place the jar in `plugins/`.
3. Start the server once — Hatchery generates its config in `plugins/Hatchery/`:
   - `config.yml`, `environment-points.yml`, `hourglasses.yml`, `messages.yml`
4. Edit configs to taste (see [Configuration](Configuration)).
5. Apply changes with `/hatchery reload`. Restart only when changing storage
   backend or replacing the plugin jar.

## Giving players Day Care blocks

Hatchery does **not** create the Day Care item — it reacts to Pixelmon Day Care
blocks being placed. Provide them however you like (crafting recipe datapack,
shop, crate, kit, `/give`). All 16 colours are registered by default
(`daycare.blocks` in `config.yml`).

## LuckPerms (optional but recommended)

Per-player / per-group daycare cap override uses a LuckPerms **meta** key
(default `hatchery.maxdaycares`):

```
/lp group vip meta set hatchery.maxdaycares 3
/lp user Steve meta set hatchery.maxdaycares 5
```

Without LuckPerms, everyone uses `daycare.max-per-player-default`.

## Storage

Default is SQLite (`plugins/Hatchery/data.db`). To use MySQL, set
`storage.type: mysql` and fill the `storage.mysql` block, then reload/restart.
See [Configuration](Configuration).
