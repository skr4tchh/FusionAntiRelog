package pw.fusionmine.fusionantirelog.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCooldown;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pw.fusionmine.fusionantirelog.managers.CooldownManager;

import java.util.List;

@UtilityClass
public class PacketEventsUtils {

    public void sendItemCooldown(Player player, Material material, long milliseconds) {
        int ticks = MathUtils.millisToTicks(milliseconds);

        ItemType itemType = SpigotConversionUtil.fromBukkitItemMaterial(material);
        if (itemType == null) return;

        WrapperPlayServerSetCooldown packet = new WrapperPlayServerSetCooldown(itemType, ticks);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public final List<Material> replaceCooldownList = List.of(
            Material.CHORUS_FRUIT,
            Material.WIND_CHARGE,
            Material.ENDER_PEARL
    );

    public void createListener(CooldownManager cooldownManager) {
        PacketEvents.getAPI().getEventManager().registerListener(
                new SimplePacketListenerAbstract(PacketListenerPriority.LOWEST) {
                    @Override
                    public void onPacketPlaySend(PacketPlaySendEvent event) {
                        if (event.getPacketType() != PacketType.Play.Server.SET_COOLDOWN) return;

                        Player player = event.getPlayer();
                        if (player == null) return;

                        WrapperPlayServerSetCooldown packet = new WrapperPlayServerSetCooldown(event);

                        int durationTicks = packet.getCooldownTicks();
                        long durationMs = (long) durationTicks * 50;

                        Material material = SpigotConversionUtil.toBukkitItemMaterial(packet.getItem());
                        if (material == null) return;

                        if (replaceCooldownList.contains(material)) {
                            long remaining = cooldownManager.getRemaining(player, material);

                            if (Math.abs(remaining - durationMs) > 100) {
                                if (durationTicks == 0) {
                                    event.setCancelled(true);
                                    return;
                                }

                                int newTicks = MathUtils.millisToTicks(remaining);
                                packet.setCooldownTicks(newTicks);
                            }
                        }
                    }
                }
        );
    }

}