package pw.fusionmine.fusionantirelog.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;
import pw.fusionmine.fusionantirelog.managers.PvPManager;
import pw.fusionmine.fusionantirelog.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class PvPListener implements Listener {

    private PvPManager pvpManager;

    private Settings settings;

    private Locale locale;

    public PvPListener(PvPManager pvpManager, Settings settings, Locale locale) {
        this.pvpManager = pvpManager;
        this.settings = settings;
        this.locale = locale;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT || e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        if (pvpManager.isTagged(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamagerByPlayer(EntityDamageByEntityEvent e) {
        if (e.getEntityType() != EntityType.PLAYER)
            return;
        Player damaged = (Player) e.getEntity();
        Player damager = pvpManager.getDamager(e.getDamager());
        if (damager == null || damaged == damager)
            return;
        pvpManager.setTagged(damager, damaged);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        pvpManager.clear(e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player target = (Player) e.getEntity();

        if (!(e.getCombuster() instanceof Player)) return;
        Player damager = (Player) e.getCombuster();

        if (damager == target) return;

        pvpManager.setTagged(damager, target);
    }

    @EventHandler
    public void onCommandUse(PlayerCommandPreprocessEvent e) {
        if (pvpManager.isTagged(e.getPlayer())) {
            String command = e.getMessage().split(" ")[0].replaceFirst("/", "");
            if(!settings.getCommandsWhitelist().contains(command)) {
                e.getPlayer().sendMessage(locale.getMessage("cmd-blocked"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (!Utils.isHarmPotion(e.getPotion()))
            return;
        Player damager = pvpManager.getDamager(e.getPotion());
        List<Player> damaged = e.getAffectedEntities().stream().filter(le -> (le != damager)).filter(Player.class::isInstance).map(Player.class::cast).collect(Collectors.toList());
        if (damager == null || damaged.isEmpty())
            return;
        pvpManager.setTagged(damager, damaged);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (pvpManager.getTagTimeleft(player) > 0) {
            player.setHealth(0.0D);
            pvpManager.clear(player);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        settings.getValidKickReasons()
                .stream()
                .filter(s -> e.getReason().contains(s))
                .findAny()
                .ifPresent(__ -> pvpManager.getTagged().remove(e.getPlayer().getName()));
    }

}
