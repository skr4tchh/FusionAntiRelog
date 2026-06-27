package pw.fusionmine.fusionantirelog.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pw.fusionmine.fusionantirelog.managers.CooldownManager;

import java.lang.reflect.Method;
import java.util.*;

@UtilityClass
public class ProtocolLibUtils {

    private static Class<?> RESOURCE_LOCATION_CLASS;

    private static Method PARSE_METHOD;

    static {
        try {
            RESOURCE_LOCATION_CLASS = MinecraftReflection.getMinecraftClass("resources.Identifier");
            PARSE_METHOD = RESOURCE_LOCATION_CLASS.getMethod("parse", String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendItemCooldown(Player player, Material material, long milliseconds) {

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SET_COOLDOWN);

        int ticks = MathUtils.millisToTicks(milliseconds);

        try {
            Object resourceLocation = PARSE_METHOD.invoke(null, material.getKey().toString());
            packet.getModifier().withType(RESOURCE_LOCATION_CLASS).write(0, resourceLocation);
            packet.getIntegers().write(0, ticks);

            protocolManager.sendServerPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Material> replaceCooldownList = List.of(
            Material.CHORUS_FRUIT,
            Material.WIND_CHARGE,
            Material.ENDER_PEARL
    );

    public void createListener(Plugin plugin, CooldownManager cooldownManager) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.SET_COOLDOWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                int duration = event.getPacket().getIntegers().read(0);
                duration = duration * 50;

                try {
                    Object resourceLocation = packet.getModifier().withType(RESOURCE_LOCATION_CLASS).read(0);
                    if (resourceLocation == null) return;

                    Material material = Material.matchMaterial(resourceLocation.toString());
                    if (material == null) return;

                    if (replaceCooldownList.contains(material)) {
                        long remaining = cooldownManager.getRemaining(player, material);
                        if (Math.abs(remaining - duration) > 100) {
                            if (duration == 0) {
                                event.setCancelled(true);
                                return;
                            }
                            int ticks = MathUtils.millisToTicks(remaining);
                            event.getPacket().getIntegers().write(0, ticks);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}