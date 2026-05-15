package pw.fusionmine.fusionantirelog.config;

import org.bukkit.plugin.java.JavaPlugin;

public class Locale extends Config {

    public Locale(JavaPlugin plugin) {
        super(plugin, "messages.yml");
    }

    public String getMessage(String key) {
        return color(this.conf.getString(key));
    }

}
