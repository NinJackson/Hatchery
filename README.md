# Hatchery

A daycare/breeding plugin for **Pixelmon on Arclight** — replaces vanilla
Pixelmon's built-in Day Care with a server-managed alternative. Each Pixelmon
**`<colour>_day_care`** block a player places is registered, owned, and ticked
by Hatchery; environment blocks around it contribute type-based "environment
points," progress accrues over time, and an egg is generated when progress
crosses a configurable threshold. The vanilla Pixelmon Day Care GUI is
suppressed for any block Hatchery manages.

**Version:** `1.1.2`
**Tested against:** Minecraft 1.20.2 · Arclight 1.20.2-1.0.3 · Pixelmon 1.20.2-9.2.10

> 📌 **Block change in 1.1.1.** Pixelmon 9.x no longer ships `ranch_block`;
> the modern equivalent is `pixelmon:<colour>_day_care` (16 dyed variants).
> Hatchery 1.1.1 now binds to all 16 colours by default and short-circuits
> Pixelmon's own Day Care interaction. See `CONFIG.md` for the full list
> + how to narrow it.

---

## Docs at a glance

| Doc | Audience | What's in it |
|---|---|---|
| **[docs/PLAYER-GUIDE.md](./docs/PLAYER-GUIDE.md)** | Players | Placing a Day Care block, using the GUI, breeding mechanics, hourglasses, upgrades, eggs, `/daycares` |
| **[docs/ADMIN-GUIDE.md](./docs/ADMIN-GUIDE.md)**   | Admins  | `/hatchery` command reference, permissions, LuckPerms meta, storage, ops procedures |
| **[docs/CONFIG.md](./docs/CONFIG.md)**             | Admins  | Per-key reference for all four YAML config files |
| **[TOUCHUP-PLAN.md](./TOUCHUP-PLAN.md)**           | Devs    | Historical 1.0.0 → 1.1.0 audit + plan (now landed) |

---

## Install

1. Drop `Hatchery-1.1.2.jar` into `plugins/` on a server running:
   - Minecraft **1.20.2**
   - **Arclight** (Forge → Bukkit hybrid)
   - **Pixelmon** 1.20.2-9.2.10
2. Start once to generate the default configs under `plugins/Hatchery/`.
3. Edit configs to taste, then `/hatchery reload`.

**Soft dependencies** (auto-detected if installed): `LuckPerms`, `PlaceholderAPI`.

### Build from source

```sh
cd /root/Hatchery
./gradle-8.7/bin/gradle shadowJar
# → build/libs/Hatchery-1.1.2.jar
```

---

## What's in 1.1.2

- 🟢 **GUI polish** — every non-interactive slot in the Daycare and Party
  Picker menus is now filled with a configurable filler block (default
  `minecraft:black_stained_glass_pane`) so the interactive Parent / Status
  / Egg / Back slots stand out cleanly.
- 🟢 **Title cleanup** — the legacy zero-width-space marker in inventory
  titles is gone. Titles now render as plain "Daycare" / "Choose Pokemon"
  instead of the doubled-with-tofu-glyph string. Menu identification
  continues to go through `MenuManager` (already tracked by player UUID).
- ✏️ **Config: new `gui.filler-item` key** in `config.yml`. Set it to any
  namespaced block ID, e.g. `pixelmon:apricorn_log`.

## What's in 1.1.1

- 🟢 **Day Care block migration** — Hatchery now binds to Pixelmon 9.x's
  `pixelmon:<colour>_day_care` blocks (all 16 dyed variants). The legacy
  `daycare.block: pixelmon:ranch_block` config key kept generating orphan
  daycares because the block doesn't exist in 9.x.
- 🟢 **Pixelmon Day Care GUI suppression** — right-clicking a Hatchery-managed
  block cancels the Bukkit `PlayerInteractEvent` before Pixelmon's
  `DayCareBlock#use()` runs, so the player only sees Hatchery's menu.
- ✏️ **Config: `daycare.block` → `daycare.blocks` (list).** Backward-compatible:
  the legacy single-string key is still accepted and merged into the list.

## What landed in 1.1.0

- Hourglass + upgrade right-click application
- Full admin command suite (`give-hourglass`, `give-upgrade`, `force-egg`, `remove`)
- `daycare.upgrade.drop-on-break` actually drops items
- `makeEgg` mother-selection fix
- `hatchery.maxdaycares` LP meta now authoritative
- Env-points cache + `BlockChangeListener` invalidation
- Dirty-flag save throttle
- Particle interval honoured

Full plan in [`TOUCHUP-PLAN.md`](./TOUCHUP-PLAN.md).

---

## License & credits

In-house plugin maintained by the Oracion team. Pixelmon API surfaces used
under their public mod ABI; Pixelmon itself is © PixelmonMod.
