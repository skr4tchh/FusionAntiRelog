package pw.fusionmine.fusionantirelog.managers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;
import pw.fusionmine.fusionantirelog.utils.ProtocolLibUtils;
import pw.fusionmine.fusionantirelog.utils.TimeUtils;

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

    public long getRemaining(Player player, Material material) {
        return (Optional.<Object>ofNullable(this.cooldowns.get(player.getName(), material))
                .map(t -> Long.valueOf(((Long) t).longValue() - System.currentTimeMillis()))
                .orElse(0L));
    }

    public boolean checkItemUse(Player player, Material material) {
        if (!pvpManager.isTagged(player))
            return false;
        long cd = settings.getItemCooldown(material);
        if (cd == 0f)
            return false;
        long remaining = getRemaining(player, material);
        if (remaining > 0) {
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(remaining);
            player.sendTitle(
                    locale.getMessage("cooldown.title"),
                    TimeUtils.replaceTime(locale.getMessage("cooldown.subtitle"), seconds),
                    5, 20, 5
            );
            return true;
        }

        this.cooldowns.put(player.getName(), material, System.currentTimeMillis() + cd);

        if(!ProtocolLibUtils.replaceCooldownList.contains(material))
            ProtocolLibUtils.sendItemCooldown(player, material, cd);

        return false;
    }

    public void clearCooldowns(Player player, String name) {
        cooldowns.row(name).forEach((material, t) -> {
            ProtocolLibUtils.sendItemCooldown(player, material, 0);
        });
        cooldowns.row(name).clear();
    }

}
