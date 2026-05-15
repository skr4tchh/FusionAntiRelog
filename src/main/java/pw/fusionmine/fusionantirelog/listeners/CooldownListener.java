package pw.fusionmine.fusionantirelog.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import pw.fusionmine.fusionantirelog.managers.CooldownManager;
import pw.fusionmine.fusionantirelog.managers.PvPManager;

public class CooldownListener implements Listener {

    private CooldownManager cooldownManager;

    private PvPManager pvpManager;

    public CooldownListener(CooldownManager cooldownManager, PvPManager pvpManager) {
        this.cooldownManager = cooldownManager;
        this.pvpManager = pvpManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntityType() == EntityType.ENDER_PEARL && pvpManager.getDamager(e.getEntity()) instanceof Player && cooldownManager.checkItemUse(pvpManager.getDamager(e.getEntity()), Material.ENDER_PEARL))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onResurrect(EntityResurrectEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if(cooldownManager.checkItemUse(player, Material.TOTEM_OF_UNDYING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (cooldownManager.checkItemUse(e.getPlayer(), e.getItem().getType()))
            e.setCancelled(true);
    }

}
