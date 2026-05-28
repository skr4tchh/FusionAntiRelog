package pw.fusionmine.fusionantirelog.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public class PotionUtils {

    public boolean isHarmPotion(ThrownPotion potion) {
        return potion.getEffects().stream().map(PotionEffect::getType).anyMatch(PotionUtils::isHarmPotion);
    }

    public boolean isHarmPotion(PotionEffectType type) {
        return (type == PotionEffectType.BLINDNESS || type == PotionEffectType.WITHER || type == PotionEffectType.POISON || type == PotionEffectType.WEAKNESS || type == PotionEffectType.SLOWNESS || type == PotionEffectType.UNLUCK);
    }

}
