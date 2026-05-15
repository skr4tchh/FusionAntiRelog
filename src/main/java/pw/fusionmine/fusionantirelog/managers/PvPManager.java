package pw.fusionmine.fusionantirelog.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import pw.fusionmine.fusionantirelog.FusionAntiRelog;
import pw.fusionmine.fusionantirelog.config.Locale;
import pw.fusionmine.fusionantirelog.config.Settings;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PvPManager {

    @Getter
    private final Map<String, Long> tagged = new HashMap<>();

    private FusionAntiRelog plugin;

    private Settings settings;

    private Locale locale;

    public PvPManager(
            FusionAntiRelog plugin,
            Settings settings,
            Locale locale
    ) {
        this.plugin = plugin;
        this.settings = settings;
        this.locale = locale;
    }

    public void load() {
        plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, this::checkTags, 20L, 20L);
    }

    public void unload() {
        plugin.getServer().getOnlinePlayers().forEach(this::clear);
    }

    public void clear(Player player) {
        String name = player.getName();
        this.tagged.remove(name);
        for(Material material : plugin.getCooldownManager().getCooldowns().columnKeySet()) {
            player.setCooldown(material, 0);
        }
        plugin.getCooldownManager().getCooldowns().row(name).clear();
        plugin.getBossBarManager().removeBossBar(name);
    }

    public void checkTags() {
        for (UnmodifiableIterator<String> unmodifiableIterator = ImmutableList.copyOf(this.tagged.keySet()).iterator(); unmodifiableIterator.hasNext(); ) {
            String key = unmodifiableIterator.next();
            Player player = Bukkit.getPlayerExact(key);
            if (player == null)
                continue;
            int timeLeft = getTagTimeleft(player);
            if (timeLeft <= 0) {
                clear(player);
                player.sendMessage(locale.getMessage("pvp-ended"));
                continue;
            }
            plugin.getBossBarManager().updateBossBar(player, timeLeft);
        }
    }

    public int getTagTimeleft(Player player) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(getTagTimeRaw(player));
    }

    public long getTagTimeRaw(Player player) {
        return this.tagged.getOrDefault(player.getName(), System.currentTimeMillis()) - System.currentTimeMillis();
    }

    public void setTagged(Player damager, Player... damaged) {
        setTagged(damager, Arrays.asList(damaged));
    }

    public void setTagged(Player damager, Collection<Player> damaged) {
        List<Player> participants = Stream.concat(Stream.of(damager), damaged.stream().filter(p -> (p != damager))).filter(Objects::nonNull).collect(Collectors.toList());
        if (participants.size() <= 1)
            return;
        participants.forEach(player -> {
            if(hasBypass(player)) return;
            boolean started = (this.tagged.put(player.getName(), Long.valueOf(System.currentTimeMillis() + this.settings.getTagTime())) == null);
            if (started) {
                player.sendMessage(locale.getMessage("pvp-entered"));
                plugin.getBossBarManager().createBossBar(player);
            }
        });
    }

    public boolean isTagged(Player player) {
        return getTagTimeRaw(player) > 0L;
    }

    public Player getDamager(Entity damager) {
        return Optional.of(damager)
                .map(e -> (e instanceof Projectile) ? ((Projectile)e).getShooter() : e)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .orElse(null);
    }

    public boolean hasBypass(Player damager) {
        if (damager.hasPermission("fusionantirelog.bypass"))
            return true;
        damager.setGameMode(GameMode.SURVIVAL);
        damager.setFlying(false);
        damager.setAllowFlight(false);
        return false;
    }

}
