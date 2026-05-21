# Hatchery — Player Guide

This guide covers everything a regular player can do with Hatchery. For server
operator content (admin commands, config, perms management) see
**[ADMIN-GUIDE.md](./ADMIN-GUIDE.md)**.

---

## Contents

- [Placing your first daycare](#placing-your-first-daycare)
- [Opening the daycare menu](#opening-the-daycare-menu)
- [Adding parents](#adding-parents)
- [Compatibility rules](#compatibility-rules)
- [Breeding & environment points](#breeding--environment-points)
- [Satisfaction levels](#satisfaction-levels)
- [Hourglasses — speeding things up](#hourglasses--speeding-things-up)
- [Upgrades — expanding the scan radius](#upgrades--expanding-the-scan-radius)
- [Collecting the egg](#collecting-the-egg)
- [Removing or breaking a daycare](#removing-or-breaking-a-daycare)
- [`/daycares` command](#daycares-command)
- [Permissions you need](#permissions-you-need)
- [FAQ & common pitfalls](#faq--common-pitfalls)

---

## Placing your first daycare

Place any of Pixelmon's **dyed Day Care blocks** (`pixelmon:<colour>_day_care`)
in a non-blacklisted world. Pixelmon ships 16 colour variants — Hatchery
treats all of them the same, so pick whichever you like the look of. On
success you'll see:

```
[Hatchery] Your daycare has been registered!
```

> 💡 **Pixelmon's own Day Care system is suppressed** for blocks Hatchery
> manages. Right-clicking opens **Hatchery's** menu, not the vanilla
> Pixelmon one. This is by design.

If the place is rejected, you'll get one of:

- `Daycares are disabled in this world.` — the world is in
  `worlds.blacklisted`.
- `You have reached your daycare limit (X).` — you're at your cap (default
  `1`; staff/donors may have more via permissions meta).

## Opening the daycare menu

Right-click your registered Day Care block. A 27-slot chest GUI opens:

```
┌─────────────────────────────────────────────┐
│  ·  ·  ·  ·  🥚  ·  ·  ·  ·                  │   row 0 (egg button at slot 4)
│  ·  ·  Parent A  ·  Parent B  ·  ·            │   row 1 (parents at slot 11 & 15)
│  ·  ·  ·  ·  STATUS  ·  ·  ·  ·               │   row 2 (status book at slot 22)
└─────────────────────────────────────────────┘
```

- **Slot 11 — Parent A** — Pokémon icon when set, otherwise a light-grey pane
- **Slot 15 — Parent B** — same
- **Slot 22 — Status book** — owner, progress, satisfaction, environment
  points, eggs ready, upgrade level, current scan radius
- **Slot 4 — Collect Egg** (turtle egg) — only shown when at least one egg
  is ready

Only the **owner** of the daycare can add/remove Pokémon or collect eggs. Other
players can open the menu read-only — their clicks are rejected with a message.

## Adding parents

1. Click an empty **Parent A** or **Parent B** slot. A 9-slot
   **Party Picker** menu opens showing your current Pixelmon party.
2. Click any party slot to move that Pokémon into the daycare. The Pokémon is
   removed from your party and stored in the daycare's persistent state.
3. Click **Back** (arrow, slot 8) to return to the daycare menu without
   choosing.

> The picker shows your 6 party slots in order; empty slots appear as a grey
> "Empty Slot" placeholder.

To **retrieve** a parent later: shift-click that slot in the daycare menu. The
Pokemon returns to your party (if there's space; otherwise you get
`Your party is full.`).

If the same Pokemon is already in your party because it was stored by an older
buggy build, Hatchery clears the daycare copy instead of adding a second copy.
You'll see: `That Pokemon was already in your party, so the daycare copy was cleared.`

## Compatibility rules

Both parents must satisfy Pixelmon's normal breeding rules:

| Rule | Detail |
|---|---|
| Egg groups | They must share at least one egg group, OR exactly one is a Ditto |
| No Ditto pair | Two Dittos cannot breed each other |
| Egg group filter | Neither parent can be in the `Undiscovered` egg group |
| Genders | Opposite genders, OR exactly one parent is a Ditto |

If the pair is incompatible the **Status** panel adds a line:

```
These Pokemon are not compatible.
```

Breeding will not progress while the pair is incompatible.

## Breeding & environment points

Every `breeding.tick-interval-seconds` (default **60s**) Hatchery looks at
each active daycare and:

1. Scans a cuboid around the Day Care block (default 5 blocks horizontal, ±2
   blocks vertical).
2. Sums the value of each block from `environment-points.yml`, matching
   against the **types of both parents** plus any universal bonuses.
3. Maps that total onto a **satisfaction level** (see below).
4. Adds `max(1, round(envPoints × multiplier ÷ 10))` to the daycare's
   progress.
5. When progress hits `breeding.base-points-needed` (default **100**), an
   egg is generated and progress resets to 0.

> 💡 **Chunks must be loaded** for a daycare to tick. If you wander far away
> from your base, the chunk unloads and your daycare pauses. Walking back, or
> Chunky's keep-loaded feature, fixes it.

## Satisfaction levels

Default ladder from `config.yml`:

| Threshold | Name | Speed |
|---|---|---|
| 0    | Unhappy  | 0.5× |
| 25   | Content  | 1.0× |
| 50   | Happy    | 1.5× |
| 75   | Ecstatic | 2.0× |
| 100  | Blissful | 3.0× |

The highest threshold ≤ your current environment points wins. The Status
panel always shows the live level + color.

## Hourglasses — speeding things up

Hourglasses are admin-issued items (`/hatchery give-hourglass`) that **advance
a daycare's breeding progress** when right-clicked on the Day Care block.

Default tiers (configurable in `hourglasses.yml`):

| Tier | Ticks added | Permission |
|---|---|---|
| Bronze | +10 | `hatchery.hourglass.bronze` |
| Silver | +25 | `hatchery.hourglass.silver` |
| Gold   | +50 | `hatchery.hourglass.gold` |

**How to use:**

1. Hold the hourglass item in your main hand.
2. Right-click your daycare's Day Care block.

What happens:

- If you don't have the tier's permission → `You don't have permission to use this hourglass.`
- If the daycare has no active pair → `This daycare has no active breeding pair.`
- Otherwise: progress jumps by the tier's `ticks-added`, the message
  `Breeding advanced by N ticks!` appears, and 1 item is consumed (default).
  Any open daycare menu auto-refreshes to show the new progress.

> ℹ️ **How items are recognised** — hourglass items carry a hidden persistent
> tag (`hatchery:hourglass_id`) baked in by the admin `give-hourglass` command.
> A hourglass with the right *display name* but no tag won't work — get a
> fresh one from an admin if your stack stops being accepted.

## Upgrades — expanding the scan radius

Upgrade items (default: tagged Diamond Block) **expand a daycare's environment
scan radius** when right-clicked on the Day Care block.

| Item | Effect |
|---|---|
| Daycare Environment Upgrade (default Diamond Block) | +`upgrade.radius-per-level` blocks to scan radius (default +2) per use |
| Max upgrades per daycare | `upgrade.max-upgrades` (default 5) |

**How to use:**

1. Hold the upgrade item in your main hand.
2. Right-click your daycare's Day Care block.

What happens:

- If the daycare is already at max upgrade level → `This daycare is fully upgraded.`
- Otherwise: upgrade level +1, 1 item consumed, message
  `Daycare upgraded! Radius: N blocks.` appears, and the env-points cache for
  this daycare is invalidated so the next breeding tick re-scans with the new
  radius.

If you break your Day Care block while it has applied upgrades, you get those
items back (default — controlled by `daycare.upgrade.drop-on-break`).

## Collecting the egg

When the Status panel shows `Eggs ready: 1+`:

1. The green **Collect Egg** button appears in slot 4 of the daycare menu.
2. Click it.
3. The egg is materialised as a Pixelmon egg item and placed in your party.
   Hatchery clears held items from generated eggs, so a parent-held item cannot
   be copied onto the egg.

Failure modes:

- `Your party is full — make room first.` — the egg stays in the daycare
  until you free a slot.
- `Failed to generate egg.` — the parent pair is invalid (one was removed,
  or they became incompatible somehow). Re-check both parents.

Egg species follows Pixelmon's normal inheritance:

- **Ditto + non-Ditto** → species of the non-Ditto parent.
- **Male non-Ditto + Female non-Ditto** → species of the **female** parent
  (the mother).

(The 1.0.0 bug where the male's species was chosen as the egg is fixed in
1.1.0.)

## Removing or breaking a daycare

- **Shift + click** a parent slot in the daycare menu to retrieve that
  Pokémon back to your party. The other parent stays.
- **Break the Day Care block** with a normal block-break to unregister the
  daycare entirely.

What happens on break:

- Any **applied upgrades** drop as items at the block's position (1 stack of
  size = your upgrade level, with the tagged display item). This is gated by
  `daycare.upgrade.drop-on-break: true` in config.
- **Parents currently inside are lost** — pull them out via the menu **before**
  breaking the block.
- You'll see: `[Hatchery] Daycare removed.`

## `/daycares` command

Alias: `/dc`.

```
/daycares
```

Shows all daycares you own:

```
Daycare — 1/3
#1  world (152, 71, -310)  eggs: 2
```

- The header reads `<owned>/<max>` — `<max>` is your effective cap
  (`daycare.max-per-player-default` overridden by `hatchery.maxdaycares`
  LuckPerms meta).
- Each line shows world + coordinates + ready-to-collect egg count.

## Permissions you need

| Node | Default | What it allows |
|---|---|---|
| `hatchery.use`              | true | Place a Day Care block, open your daycare menu, breed |
| `hatchery.hourglass.bronze` | true | Use bronze hourglasses |
| `hatchery.hourglass.silver` | true | Use silver hourglasses |
| `hatchery.hourglass.gold`   | true | Use gold hourglasses |

If a hourglass tier rejects you with the "no permission" message, ask staff
to grant or restore the matching node.

---

## FAQ & common pitfalls

**Q. Which Day Care block colour should I use?**
Doesn't matter. All 16 Pixelmon `pixelmon:<colour>_day_care` blocks work
identically in Hatchery — pick whichever fits your build. Server admins can
narrow the list in `config.yml` (`daycare.blocks`) if they want, e.g. only
allow `pink_day_care` for an event.

**Q. I right-clicked a Day Care block and got Pixelmon's vanilla menu, not Hatchery's.**
That block isn't in Hatchery's config (or there's been a config reload race).
Have an admin check `/hatchery list` and the `daycare.blocks` list in
`config.yml`. The fix is usually `/hatchery reload`.

**Q. My breeding hasn't moved in hours.**
Check three things: (1) the chunk holding your Day Care block is loaded — is
someone near it, or do you have a keep-loaded plugin set on it? (2) the
parents are compatible — open the menu and check the status book for the red
"not compatible" line. (3) `Eggs ready: <max>` — once you hit the daycare's
egg cap, progress pauses until you collect.

**Q. My satisfaction is stuck at "Unhappy".**
The blocks around your Day Care don't match your parents' types. Open
`environment-points.yml` or ask an admin which blocks contribute for your
Pokémon's types. The default config covers all 18 types; check those default
mappings.

**Q. I right-click my Day Care with a hourglass but nothing happens.**
Most common: the held hourglass wasn't issued via `/hatchery give-hourglass`
and lacks the persistent tag the plugin checks for. Display name + lore alone
aren't enough — items must carry the `hatchery:hourglass_id` PDC tag. Ask an
admin for a freshly-issued stack.

**Q. I'm only allowed 1 daycare. Can I get more?**
That's a server policy. Ask staff to raise `hatchery.maxdaycares` for you via
LuckPerms (`lp user <you> meta set hatchery.maxdaycares 3`).

**Q. Can I take Pokémon out of my PC to use as parents?**
Not in 1.x — parents are pulled from your in-game party only. Withdraw the
Pokémon from your PC first.

**Q. Did I lose my eggs when I broke my daycare?**
Yes. Eggs are stored in the daycare; once it's unregistered, the eggs are
gone. Collect first, break second.
