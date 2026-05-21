# FAQ

**Does Hatchery add the Day Care item/recipe?**
No. It reacts to Pixelmon **Day Care blocks** being placed. Provide the item via
a crafting datapack, shop, crate, kit, or `/give`. All 16 colours are
recognised by default (`daycare.blocks`).

**Which servers does it run on?**
The current server-tested line runs on **Arclight 1.21.1** with **Pixelmon
9.3.x**. Pixelmon itself requires Forge/Arclight, so plain Paper/Spigot can't
run the Pixelmon side.

**Can it pull Pokémon from the PC?**
No — the player's 6-slot **party** only. PC support is a possible future
feature.

**Why are my Pokémon "not compatible"?**
Strict rules via Pixelmon's API: overlapping **egg group**, opposite **gender**
(or valid **Ditto**), and both **discovered**. Mystery/undiscovered are
rejected.

**Breeding seems paused.**
Breeding only ticks while the daycare's **chunk is loaded** (by design). Keep
the area loaded (player nearby / chunk loader) for continuous progress.

**How do I give a player more daycares?**
Set the LuckPerms **meta** `hatchery.maxdaycares` on the user/group (it
overrides `daycare.max-per-player-default`). Requires LuckPerms.

**SQLite vs MySQL?**
Default SQLite (`data.db`). For networks/shared data, set `storage.type: mysql`
and fill `storage.mysql`. Pooled via HikariCP.

**Do hourglasses / the upgrade item work yet?**
Yes. Admin-issued hourglasses advance breeding progress on right-click, and
the upgrade item expands the scan radius. Upgrade refunds on block break are
controlled by `daycare.upgrade.drop-on-break`.

**Can Hatchery duplicate parent Pokemon or held items?**
The 1.21.1 build includes guards for the known duplicate paths: parent
placement clears Pixelmon's active/original party storage and refuses placement
if the parent UUID is still present, stale daycare copies are cleared on
retrieve, and generated eggs have held items cleared before delivery.

**Will it break when Pixelmon updates?**
Pixelmon storage/API behavior can change between major Minecraft versions. Most
of the version-sensitive work is isolated in `PixelmonHook`.

**Is it really free / open source?**
Yes — **MIT**. No proprietary code or jars are bundled; you supply Pixelmon/MC
jars only to *build* (see [Building from Source](Building-from-Source)).

**Particles aren't showing.**
Check `particles.*.enabled`, `particles.*.type`, and the interval settings in
`config.yml`. Particle spawning is active in 1.1.2 and throttled by the
configured intervals.
