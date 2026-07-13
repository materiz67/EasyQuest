package pl.materiz66.easyquests.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class GlowUtil {
    private static Method setGlintMethod = null;

    static {
        try {
            setGlintMethod = ItemMeta.class.getMethod("setEnchantmentGlintOverride", Boolean.class);
        } catch (NoSuchMethodException ignored) {}
    }

    public static void applyGlow(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (setGlintMethod != null) {
            try {
                setGlintMethod.invoke(meta, true);
            } catch (Exception e) {
                applyLegacyGlow(meta);
            }
        } else {
            applyLegacyGlow(meta);
        }
        item.setItemMeta(meta);
    }

    private static void applyLegacyGlow(ItemMeta meta) {
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
}