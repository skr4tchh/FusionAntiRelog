package pw.fusionmine.fusionantirelog.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Config {

    JavaPlugin plugin;

    private File file;

    YamlConfiguration conf;

    public Config(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    public void load() {
        YamlConfiguration def = null;
        try (InputStream in = this.plugin.getResource(this.file.getName())) {
            if (!this.file.exists()) {
                this.plugin.getDataFolder().mkdirs();
                Files.copy(in, this.file.toPath());
            } else {
                def = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.conf = YamlConfiguration.loadConfiguration(this.file);
        if (def != null && checkKeys(def.getKeys(false), def, this.conf, new AtomicBoolean(false)).get())
            save();
    }

    private AtomicBoolean checkKeys(Collection<String> keys, ConfigurationSection def, ConfigurationSection current, AtomicBoolean found) {
        for (String key : keys) {
            if (!current.contains(key)) {
                current.set(key, def.get(key));
                found.set(true);
                continue;
            }
            Object nDef = def.get(key);
            Object nCurrent = current.get(key);
            if (nCurrent == null || !nDef.getClass().isInstance(nCurrent)) {
                current.set(key, nDef);
                continue;
            }
            if (!def.isConfigurationSection(key))
                continue;
            nDef = def.getConfigurationSection(key);
            checkKeys(((ConfigurationSection)nDef).getKeys(false), (ConfigurationSection)nDef, current.getConfigurationSection(key), found);
        }
        return found;
    }

    public String color(String string) {
        return (string != null) ? ChatColor.translateAlternateColorCodes('&', string) : null;
    }

    public void save() {
        try {
            this.conf.save(this.file);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Can't save " + this.file.getName(), e);
        }
    }

}
