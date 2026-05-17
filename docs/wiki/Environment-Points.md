# Environment Points

The signature OldSchool mechanic: the **habitat you build around a Day Care**
determines how fast it breeds. Blocks are scored against the **breeding pair's
Pokémon types**.

## How scoring works

1. Each tick, every block within the scan radius is checked.
2. For each of the pair's **types**, the block's point value is looked up in
   `environment-points.yml → type-points.<type>`.
3. Values accumulate into a total → mapped to a **satisfaction tier** →
   **speed multiplier** (see [Configuration](Configuration#satisfaction-levels)).

So a Water/Flying pair benefits from water **and** flying-favored blocks; a
single block can count for multiple types if listed under each.

## File format

```yaml
type-points:
  fire:
    minecraft:netherrack: 2
    minecraft:magma_block: 3
    minecraft:lava: 4
    minecraft:fire: 5
  water:
    minecraft:water: 2
    minecraft:kelp: 1
    minecraft:coral_block: 2
  # ...all 18 types...
```

- Block IDs are **namespaced** and **case-insensitive**
  (`minecraft:grass_block`, `pixelmon:apricorn_log`).
- Add/remove blocks freely; tune values to balance your economy.
- Universal bonuses (applied regardless of type) are defined alongside the
  per-type map.

## Default highlights (shipped)

| Type | Strong blocks (default) |
|------|-------------------------|
| Fire | fire (5), lava (4), magma (3), soul_fire (4) |
| Water | water (2), prismarine (2), coral_block (2) |
| Grass | grass_block (2), apricorn_log (3) |
| Electric | lightning_rod (5), redstone/copper block (2) |
| Ice | blue_ice (4), packed_ice (3) |
| Psychic | amethyst_block (3), end_stone (2) |
| Bug | honey_block (3) |
| Ghost | soul_sand/soul_soil (3) |
| Dragon | dragon_head (5) |
| Dark | blackstone (2) |
| Normal/Ground/Rock/… | common terrain (1–2) |

*(The shipped `environment-points.yml` is the source of truth — this table is a
flavour summary.)*

## Design tips

- A **tightly-themed** build beats a big mixed one — only matched blocks score.
- Combine with the **Upgrade item** to widen the radius once your theme is dense.
- Keep top-tier (`Blissful`, 3×) reachable but effortful — tune
  `satisfaction-levels` thresholds vs. point density.
