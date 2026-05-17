# Hourglasses & Upgrades

## Hourglasses (fast-forward ticks)

Configured in `hourglasses.yml`. A player right-clicks a daycare while holding
an hourglass to instantly advance breeding by `ticks-added`.

| Tier | Ticks added (default) | Permission |
|------|-----------------------|------------|
| Bronze | 10 | `hatchery.hourglass.bronze` |
| Silver | 25 | `hatchery.hourglass.silver` |
| Gold | 50 | `hatchery.hourglass.gold` |

Each tier:

```yaml
hourglasses:
  bronze:
    base-item: pixelmon:hourglass
    display-name: "&6Bronze Hourglass"
    lore: ["&7Advances breeding by &e10 ticks"]
    ticks-added: 10
    consume: true
    permission: hatchery.hourglass.bronze
```

- The item is matched by `base-item` + `display-name`/`lore` identity.
- `consume: true` removes one on use.
- Add custom tiers by adding more keys with their own `permission`.
- Distribute via crates/shops/`/give` (or, once implemented,
  `/hatchery give-hourglass` — see [Roadmap](Roadmap)).

> Status: hourglass right-click consumption is on the [Roadmap](Roadmap)
> (config is fully defined and read).

## Environment Upgrade item

Configured under `daycare.upgrade` in `config.yml`. Right-click your daycare
holding the upgrade item to **increase its scan radius**, so more surrounding
blocks contribute [environment points](Environment-Points).

| Key | Default | Meaning |
|-----|---------|---------|
| `item` | `minecraft:diamond_block` | The upgrade item |
| `display-name` / `lore` | themed (`{radius-per-level}`, `{max-upgrades}`) | Identity shown to players |
| `radius-per-level` | `2` | Blocks added to the radius per upgrade |
| `max-upgrades` | `5` | Cap per daycare |
| `drop-on-break` | `true` | Refund applied upgrades when the daycare block is broken |

Effective radius = `base-scan-radius` + (upgrades × `radius-per-level`),
clamped at `max-upgrades`.

> Status: upgrade apply + drop-on-break payout are on the [Roadmap](Roadmap)
> (identity/values configured and read today).
