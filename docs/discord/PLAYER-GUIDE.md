# 🥚 Hatchery — Player Guide

Welcome! Hatchery is the server's Pokédex-themed daycare system. Drop a Pixelmon **Day Care** block, put two compatible Pokémon inside, and let them produce eggs while you play. Below is everything you need to know.

> 💡 The `---` dividers between sections are message-break friendly if you're posting this guide to Discord — each chunk fits comfortably in one message.

---

## 🏠 1. Placing your first daycare

Place any of Pixelmon's 16 dyed **Day Care** blocks in a non-blacklisted world. You'll see:

> *[Hatchery] Your daycare has been registered!*

The colour doesn't matter — black, blue, brown, cyan, gray, green, light_blue, light_gray, lime, magenta, orange, pink, purple, red, white, yellow — they all work the same in Hatchery.

**If placement is rejected, you'll get one of these:**
- `Daycares are disabled in this world.` — wrong world (build/adventure servers usually are blacklisted)
- `You have reached your daycare limit (X).` — you've hit your cap. Ask staff to raise it for you.

> ⚠️ Pixelmon's vanilla Day Care GUI is **suppressed** while Hatchery is installed. Right-clicking opens Hatchery's menu instead. This is intentional.

---

## 📋 2. Opening the Daycare Menu

Right-click your Day Care block. A 27-slot menu opens:

```
+---+---+---+---+---+---+---+---+---+
|   |   |   |   |🥚 |   |   |   |   |  ← row 1 (Collect Egg button)
+---+---+---+---+---+---+---+---+---+
|   |   | A |   |   |   | B |   |   |  ← row 2 (Parent A & Parent B)
+---+---+---+---+---+---+---+---+---+
|   |   |   |   |📖 |   |   |   |   |  ← row 3 (Status book)
+---+---+---+---+---+---+---+---+---+
```

- **A** = Parent A slot (click to add)
- **B** = Parent B slot (click to add)
- **📖** = Daycare Status book — hover to see progress, satisfaction, eggs, scan radius
- **🥚** = Collect Egg button — only appears when an egg is ready

Only the **owner** of the daycare can add/remove Pokémon or collect eggs. Other players see read-only.

---

## 🐣 3. Adding parents

1. Click an empty Parent slot. A 9-slot **Party Picker** opens showing your party.
2. Click any party slot to move that Pokémon into the daycare.
3. Click the **Back** arrow (right side of the picker) to cancel.

To **retrieve a parent later**: shift-click that parent in the daycare menu. They go back to your party (if there's room).

If an older daycare record already contains a Pokemon that is still in your party, Hatchery clears the daycare copy instead of giving you a duplicate. You'll see: `That Pokemon was already in your party, so the daycare copy was cleared.`

**Compatibility rules:**
- Both parents must share an **egg group**, OR exactly one is a **Ditto**
- Two Dittos can't breed each other
- Neither parent in the **Undiscovered** egg group
- **Opposite genders**, OR one is a Ditto

If the pair is incompatible the status book shows a red `These Pokemon are not compatible.` line and breeding won't progress.

---

## 🔄 4. Breeding & environment points

Every 60 seconds Hatchery looks at each active daycare and:

1. **Scans the area** around your Day Care block (default 5 blocks horizontal, ±2 vertical)
2. Adds up **environment points** based on what blocks are nearby and your parents' types — fire types love magma blocks, water types love water and kelp, grass types love grass blocks, etc.
3. Maps that total to a **satisfaction level** (see next section)
4. Adds progress proportional to env points × satisfaction multiplier
5. When progress hits **100**, one egg is generated

> 🌍 **Chunks must be loaded** for breeding to tick. If you walk away from your base for hours, your daycare pauses. Walk back, or ask staff about keep-loaded options.

---

## 😊 5. Satisfaction levels

The blocks around your daycare determine how happy the parents are. Higher satisfaction = faster breeding:

- **Unhappy** (0–24 env points) — `0.5× speed`
- **Content** (25–49) — `1.0× speed`
- **Happy** (50–74) — `1.5× speed`
- **Ecstatic** (75–99) — `2.0× speed`
- **Blissful** (100+) — `3.0× speed`

Open the Status book in your daycare menu to see your current level live.

**Tip:** match blocks to your parents' types. A water+fire pair benefits from both magma blocks AND water sources nearby. Stack as many as fit in the scan radius.

---

## ⏳ 6. Hourglasses — speed it up

Hourglasses are admin-issued items that **jump your breeding progress forward** when right-clicked on the Day Care block:

- **Bronze** — `+10` ticks
- **Silver** — `+25` ticks
- **Gold** — `+50` ticks

**How to use:**
1. Hold the hourglass in your hand
2. Right-click your Day Care block

**Possible outcomes:**
- ✅ `Breeding advanced by N ticks!` — progress jumps, 1 hourglass consumed
- ❌ `This daycare has no active breeding pair.` — add parents first, no item consumed
- ❌ `You don't have permission to use this hourglass.` — wrong tier for your rank

> ⚠️ **Important:** Hourglasses must be issued via `/hatchery give-hourglass` to work. A hourglass with the right *display name* but no hidden plugin tag will **not** be recognized. If your stack stops working, get a fresh one from staff.

---

## 💎 7. Upgrades — bigger scan radius

Upgrade items (default: tagged **Diamond Block**) **expand your daycare's environment scan radius** when right-clicked on the block:

- Each upgrade adds **+2 blocks** to the scan radius
- Maximum **5 upgrades** per daycare (radius goes 5 → 15 blocks)

**How to use:**
1. Hold the upgrade item
2. Right-click your Day Care block

**Possible outcomes:**
- ✅ `Daycare upgraded! Radius: N blocks.` — applied, 1 item consumed
- ❌ `This daycare is fully upgraded.` — already at max

> 💰 **Bonus:** if you ever break your Day Care, all applied upgrades drop back as items (by default). You don't lose your investment when relocating.

---

## 🥚 8. Collecting the egg

When the Status book shows `Eggs ready: 1` (or more):

1. A green **Collect Egg** button appears in the top row of the menu
2. Click it
3. The egg lands in your party as a Pixelmon egg

Hatchery clears held items from generated eggs, so parent-held items cannot be copied onto eggs.

**Failure cases:**
- `Your party is full — make room first.` → the egg stays parked until you free a slot
- `Failed to generate egg.` → check both parents are still present and compatible

**What species hatches?**
- Ditto + non-Ditto → species of the non-Ditto parent
- Male non-Ditto + Female non-Ditto → species of the **female** parent (the mother)

---

## 💥 9. Removing or breaking a daycare

**To take parents back without unregistering:**
- Shift-click the parent slot in the daycare menu — they go back to your party.

**To dismantle the daycare entirely:**
- Break the Day Care block like any other block.
- Your applied upgrades drop as items at the block's position.
- **Pull parents and eggs out first** — anything still inside the daycare is lost on break.

You'll see `[Hatchery] Daycare removed.` on success.

---

## 📜 10. The `/daycares` command

Alias: `/dc`

Lists every daycare you own:

```
Daycare — 1/3
#1  world (152, 71, -310)  eggs: 2
```

- The header shows `<owned> / <max>` (your daycare cap)
- Each line shows world + coordinates + ready-to-collect egg count

Use it to find lost daycares or quickly check how many eggs are waiting for you.

---

## 🔑 11. Permissions reference

Most players have these by default — flagged only if staff has restricted them on your rank.

- **`hatchery.use`** — basic daycare use (place, open menu, breed)
- **`hatchery.hourglass.bronze`** — use bronze hourglasses
- **`hatchery.hourglass.silver`** — use silver hourglasses
- **`hatchery.hourglass.gold`** — use gold hourglasses

If a hourglass tier rejects you with "no permission", ask staff to grant the matching node.

---

## ❓ 12. FAQ & common issues

**Q: Which Day Care colour should I use?**
Any of them. All 16 work identically. Pick whatever fits your build.

**Q: I right-clicked and got Pixelmon's vanilla menu, not Hatchery's.**
That specific colour block isn't in the server's Hatchery config. Ask staff to add it or pick a different colour.

**Q: My breeding hasn't moved in hours.**
Three usual suspects:
- The **chunk is unloaded** (you're too far from the daycare)
- The pair is **incompatible** — check the status book for the red warning
- You've hit your **egg cap** — collect the eggs first

**Q: Satisfaction stuck at "Unhappy".**
The blocks around your Day Care don't match either parent's types. Look up your Pokémon's types and stack matching blocks within the scan radius.

**Q: I right-clicked with a hourglass and nothing happened.**
The hourglass needs the hidden plugin tag from `/hatchery give-hourglass`. A vanilla `/give` of the same item won't work. Get one from staff.

**Q: Can I get more than 1 daycare?**
Server policy. Ask staff — they can raise your cap with a LuckPerms meta value.

**Q: Can I use Pokémon from my PC?**
Not in v1.x — parents come from your in-game party only. Withdraw from PC first.

**Q: I broke my daycare with eggs inside. Did I lose them?**
Yes. Always collect eggs and pull parents out before breaking. The block break unregisters everything in one shot.

---

## 🆘 Need help?

Ping staff in #help-and-support with:
- Your daycare's world + coordinates (from `/daycares`)
- A screenshot of the Status book if you're stuck on breeding speed

Have fun, trainer! 🎮
