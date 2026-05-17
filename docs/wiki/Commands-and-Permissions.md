# Commands & Permissions

## Commands

| Command | Aliases | Perm | Description |
|---------|---------|------|-------------|
| `/daycares` | `/dc` | `hatchery.use` | Open your daycare list / GUI. |
| `/hatchery reload` | `/osb`, `/oldschoolbreeding` | `hatchery.admin` | Reload all configs. |
| `/hatchery list` | | `hatchery.admin` | List registered daycares. |
| `/hatchery give-hourglass` | | `hatchery.admin` | Give an hourglass item. *(planned)* |
| `/hatchery give-upgrade` | | `hatchery.admin` | Give an upgrade item. *(planned)* |
| `/hatchery force-egg` | | `hatchery.admin` | Force an egg at a daycare. *(planned)* |
| `/hatchery remove` | | `hatchery.admin` | Remove a daycare. *(planned)* |

Items marked *(planned)* are on the [Roadmap](Roadmap).

## Permissions

| Node | Default | Purpose |
|------|---------|---------|
| `hatchery.use` | `true` | Create/use daycares (`/daycares`). |
| `hatchery.admin` | `op` | Admin command + reload. |
| `hatchery.hourglass.bronze` | `true` | Use bronze hourglasses. |
| `hatchery.hourglass.silver` | `true` | Use silver hourglasses. |
| `hatchery.hourglass.gold` | `true` | Use gold hourglasses. |

Custom hourglass tiers use whatever `permission:` you set in `hourglasses.yml`.

## LuckPerms meta (daycare cap)

The per-player cap is **meta**, not a permission. Key is
`daycare.permission-meta-key` (default `hatchery.maxdaycares`):

```
/lp group default meta set hatchery.maxdaycares 1
/lp group vip     meta set hatchery.maxdaycares 3
/lp user  Alex    meta set hatchery.maxdaycares 10
```

The resolved meta value overrides `daycare.max-per-player-default`. Without
LuckPerms, the default applies to everyone.
