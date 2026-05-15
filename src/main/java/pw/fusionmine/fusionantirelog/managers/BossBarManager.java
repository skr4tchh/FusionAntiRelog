package pw.fusionmine.fusionantirelog.managers;


import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;
import pw.fusionmine.fusionantirelog.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BossBarManager {

    private PvPManager pvpManager;

    private Locale locale;

    private Settings settings;

    private final Map<String, BossBar> bossBarMap = new HashMap<>();

    public BossBarManager(
            PvPManager pvpManager,
            Locale locale,
            Settings settings
    ) {
        this.pvpManager = pvpManager;
        this.locale = locale;
        this.settings = settings;
    }

    public void removeBossBar(String name) {
        Optional.ofNullable(this.bossBarMap.remove(name)).ifPresent(BossBar::removeAll);
    }

    public void createBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(Utils.replaceTime(locale.getMessage("bossbar-time"), this.settings.getRawTagTime()), settings.getBarColor(), BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0D);
        this.bossBarMap.put(player.getName(), bossBar);
    }

    public void updateBossBar(Player player, int timeLeft) {
        BossBar bossBar = this.bossBarMap.get(player.getName());
        double progress = Math.max(0.0, Math.min(1.0, (double) pvpManager.getTagTimeRaw(player) / settings.getTagTime()));
        bossBar.setTitle(Utils.replaceTime(locale.getMessage("bossbar-time"), timeLeft));
        bossBar.setProgress(progress);
    }

}
