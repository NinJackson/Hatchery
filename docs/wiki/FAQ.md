# FAQ

**Does Hatchery add the Day Care item/recipe?**
No. It reacts to Pixelmon **Day Care blocks** being placed. Provide the item via
a crafting datapack, shop, crate, kit, or `/give`. All 16 colours are
recognised by default (`daycare.blocks`).

**Which servers does it run on?**
Built for **Arclight 1.20.2** (Bukkit↔Forge) with **Pixelmon 9.2.x**, Java 17.
Pixelmon itself requires Forge/Arclight, so plain Paper/Spigot can't run the
Pixelmon side.

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
Configs are fully defined and read; the right-click **apply** handlers
(hourglass consumption, upgrade apply, drop-on-break) and some `/hatchery`
subcommands are on the [Roadmap](Roadmap).

**Will it break when Pixelmon updates?**
All Pixelmon API use is reflective and isolated in `PixelmonHook`. A major
Pixelmon bump (e.g. 9.3+/1.21) may need that single file adjusted.

**Is it really free / open source?**
Yes — **MIT**. No proprietary code or jars are bundled; you supply Pixelmon/MC
jars only to *build* (see [Building from Source](Building-from-Source)).

**Particles aren't showing.**
Particle config is wired but visual polish is on the [Roadmap](Roadmap).
