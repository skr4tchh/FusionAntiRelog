package pw.fusionmine.fusionantirelog;

import com.comphenix.protocol.ProtocolLib;
import lombok.Getter;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;
import pw.fusionmine.fusionantirelog.listeners.CooldownListener;
import pw.fusionmine.fusionantirelog.listeners.PvPListener;
import pw.fusionmine.fusionantirelog.managers.BossBarManager;
import pw.fusionmine.fusionantirelog.managers.CooldownManager;
import pw.fusionmine.fusionantirelog.managers.PvPManager;
import pw.fusionmine.fusionantirelog.utils.ProtocolLibUtils;

import java.util.*;

@Getter
public class FusionAntiRelog extends JavaPlugin {

    private Settings settings;
    private Locale locale;
    private CooldownManager cooldownManager;
    private PvPManager pvpManager;
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        (this.settings = new Settings(this)).load();
        (this.locale = new Locale(this)).load();

        this.pvpManager = new PvPManager(this, settings, locale);
        this.pvpManager.load();

        this.cooldownManager = new CooldownManager(pvpManager, locale, settings);
        this.bossBarManager = new BossBarManager(pvpManager, locale, settings);

        loadListeners();
    }

    private void loadListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new CooldownListener(cooldownManager), this);
        pluginManager.registerEvents(new PvPListener(pvpManager, settings, locale), this);

        ProtocolLibUtils.createListener(this, this.cooldownManager);
    }

    @Override
    public void onDisable() {
        pvpManager.unload();
    }

}
