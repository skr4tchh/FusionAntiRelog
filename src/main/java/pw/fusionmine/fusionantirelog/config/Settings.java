package pw.fusionmine.fusionantirelog.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Settings extends Config {

    public Settings(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }

    public long getTagTime() {
        return TimeUnit.SECONDS.toMillis(this.conf.getInt("pvp-time"));
    }

    public int getRawTagTime() {
        return this.conf.getInt("pvp-time");
    }

    public List<String> getValidKickReasons() {
        return this.conf.getStringList("valid-kick-reasons");
    }

    public List<String> getCommandsWhitelist() {
        return this.conf.getStringList("commands-whitelist");
    }

    public long getItemCooldown(Material material) {
        return TimeUnit.SECONDS.toMillis(this.conf.getInt("item-cooldowns." + material.name().toUpperCase(), 0));
    }

    public BarColor getBarColor() {
        return BarColor.valueOf(this.conf.getString("bar-color"));
    }

}
