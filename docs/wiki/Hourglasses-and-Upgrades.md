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

- The item is matched by the hidden `hatchery:hourglass_id` persistent tag
  created by `/hatchery give-hourglass`.
- `consume: true` removes one on use.
- Add custom tiers by adding more keys with their own `permission`.
- Distribute with `/hatchery give-hourglass <player> <tier> [amount]`. Crates
  or shops should give the tagged item produced by that command, not a plain
  vanilla item with copied display text.

Plain items created with `/give` will not work as hourglasses unless another
tool preserves the plugin's persistent data tag.

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

Admins can issue upgrade items with `/hatchery give-upgrade <player> [amount]`.
Applied upgrades are refunded on block break when `drop-on-break` is true.
