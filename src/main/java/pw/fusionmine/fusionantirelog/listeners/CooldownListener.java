package pw.fusionmine.fusionantirelog.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import pw.fusionmine.fusionantirelog.managers.CooldownManager;

public class CooldownListener implements Listener {

    private CooldownManager cooldownManager;

    public CooldownListener(CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (cooldownManager.checkItemUse(player, Material.TOTEM_OF_UNDYING)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().isRightClick()) return;

        Player player = event.getPlayer();

        Material material = event.getItem().getType();
        switch (material) {
            case FIREWORK_ROCKET:
            case WIND_CHARGE:
                if (cooldownManager.checkItemUse(player, material)) {
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPerlLaunch(ProjectileLaunchEvent e) {
        if (e.getEntityType() == EntityType.ENDER_PEARL && e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            if (cooldownManager.checkItemUse(player, Material.ENDER_PEARL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        Material material = e.getItem().getType();
        switch (material) {
            case GOLDEN_APPLE:
            case ENCHANTED_GOLDEN_APPLE:
            case CHORUS_FRUIT:
                if (cooldownManager.checkItemUse(e.getPlayer(), material))
                    e.setCancelled(true);
        }
    }

}
