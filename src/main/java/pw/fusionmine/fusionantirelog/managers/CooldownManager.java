package pw.fusionmine.fusionantirelog.managers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;
import pw.fusionmine.fusionantirelog.utils.Utils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
public class CooldownManager {

    private PvPManager pvpManager;

    private Locale locale;

    private Settings settings;

    private final Table<String, Material, Long> cooldowns = HashBasedTable.create();

    public CooldownManager(
            PvPManager pvpManager,
            Locale locale,
            Settings settings
    ) {
        this.pvpManager = pvpManager;
        this.locale = locale;
        this.settings = settings;
    }

    public int getCooldownTime(Player player, Material material) {
        return (Optional.<Object>ofNullable(this.cooldowns.get(player.getName(), material))
                .map(t -> Long.valueOf(((Long) t).longValue() - System.currentTimeMillis()))
                .map(t -> Integer.valueOf((int) TimeUnit.MILLISECONDS.toSeconds(t.longValue())))
                .orElse(0));
    }

    public boolean checkItemUse(Player player, Material material) {
        if (!pvpManager.isTagged(player))
            return false;
        long cd = settings.getItemCooldown(material);
        if (cd == 0f)
            return false;
        int timeleft = getCooldownTime(player, material);
        if (timeleft > 0) {
            player.sendTitle(
                    locale.getMessage("cooldown.title"),
                    Utils.replaceTime(locale.getMessage("cooldown.subtitle"), timeleft),
                    5, 20, 5
            );
            return true;
        }
        this.cooldowns.put(player.getName(), material, System.currentTimeMillis() + cd);
        player.setCooldown(material, (int) (cd / 50));
        return false;
    }

}
