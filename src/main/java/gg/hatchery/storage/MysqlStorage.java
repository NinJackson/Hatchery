package gg.hatchery.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MysqlStorage implements Storage {

    private final Hatchery plugin;
    private final MainConfig cfg;
    private HikariDataSource ds;

    public MysqlStorage(Hatchery plugin, MainConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    @Override
    public void init() throws Exception {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl("jdbc:mysql://" + cfg.getMysqlHost() + ":" + cfg.getMysqlPort()
                + "/" + cfg.getMysqlDatabase() + "?useSSL=false&allowPublicKeyRetrieval=true");
        hc.setUsername(cfg.getMysqlUser());
        hc.setPassword(cfg.getMysqlPassword());
        hc.setMaximumPoolSize(5);
        hc.setPoolName("Hatchery-MySQL");
        this.ds = new HikariDataSource(hc);

        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(SchemaSql.CREATE_DAYCARES_MYSQL);
        }
    }

    @Override
    public void close() { if (ds != null) ds.close(); }

    @Override
    public void saveDaycare(Daycare d) {
        String sql = "INSERT INTO daycares (id, owner, world, x, y, z, upgrades, points, eggs, pair_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE owner=VALUES(owner), world=VALUES(world), " +
                     "x=VALUES(x), y=VALUES(y), z=VALUES(z), upgrades=VALUES(upgrades), " +
                     "points=VALUES(points), eggs=VALUES(eggs), pair_json=VALUES(pair_json)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getId().toString());
            ps.setString(2, d.getOwner().toString());
            ps.setString(3, d.getWorldName());
            ps.setInt   (4, d.getX());
            ps.setInt   (5, d.getY());
            ps.setInt   (6, d.getZ());
            ps.setInt   (7, d.getUpgradeLevel());
            ps.setInt   (8, d.getProgressPoints());
            ps.setInt   (9, d.getEggCount());
            ps.setString(10, d.getPairJson());
            ps.executeUpdate();
            d.clearDirty();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save daycare " + d.getId() + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteDaycare(UUID id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM daycares WHERE id = ?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to delete daycare " + id + ": " + e.getMessage());
        }
    }

    @Override
    public List<Daycare> loadAllDaycares() {
        List<Daycare> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM daycares")) {
            while (rs.next()) {
                out.add(Daycare.fromRow(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("owner")),
                        rs.getString("world"),
                        rs.getInt("x"), rs.getInt("y"), rs.getInt("z"),
                        rs.getInt("upgrades"),
                        rs.getInt("points"),
                        rs.getInt("eggs"),
                        rs.getString("pair_json")
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load daycares: " + e.getMessage());
        }
        return out;
    }
}
