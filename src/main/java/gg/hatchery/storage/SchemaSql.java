package gg.hatchery.storage;

final class SchemaSql {
    private SchemaSql() {}

    static final String CREATE_DAYCARES =
            "CREATE TABLE IF NOT EXISTS daycares (" +
                    "  id        TEXT PRIMARY KEY," +
                    "  owner     TEXT NOT NULL," +
                    "  world     TEXT NOT NULL," +
                    "  x         INTEGER NOT NULL," +
                    "  y         INTEGER NOT NULL," +
                    "  z         INTEGER NOT NULL," +
                    "  upgrades  INTEGER NOT NULL DEFAULT 0," +
                    "  points    INTEGER NOT NULL DEFAULT 0," +
                    "  eggs      INTEGER NOT NULL DEFAULT 0," +
                    "  pair_json TEXT" +
                    ")";

    static final String CREATE_DAYCARES_MYSQL =
            "CREATE TABLE IF NOT EXISTS daycares (" +
                    "  id        VARCHAR(36) PRIMARY KEY," +
                    "  owner     VARCHAR(36) NOT NULL," +
                    "  world     VARCHAR(64) NOT NULL," +
                    "  x         INT NOT NULL," +
                    "  y         INT NOT NULL," +
                    "  z         INT NOT NULL," +
                    "  upgrades  INT NOT NULL DEFAULT 0," +
                    "  points    INT NOT NULL DEFAULT 0," +
                    "  eggs      INT NOT NULL DEFAULT 0," +
                    "  pair_json TEXT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
}
