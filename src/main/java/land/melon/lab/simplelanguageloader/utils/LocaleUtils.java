package land.melon.lab.simplelanguageloader.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * From: <a href="https://github.com/NyaaCat/NyaaCore/blob/1.18/src/main/java/cat/nyaa/nyaacore/utils/LocaleUtils.java">cat.nyaa.nyaacore.utils.LocaleUtils.java</a>
 */
public class
LocaleUtils {
    public static String getUnlocalizedName(Material material) {
        if (material == null) throw new IllegalArgumentException();
        return namespaceKeyToTranslationKey(material.isBlock() ? "block" : "item", material.getKey());
    }

    public static Component getTranslatableItemComponent(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().displayName();
        if (itemStack.getItemMeta() instanceof SkullMeta && ((SkullMeta) itemStack.getItemMeta()).hasOwner()) {
            String key = getUnlocalizedName(itemStack.getType()) + ".named";
            return Component.translatable(key, ((SkullMeta) itemStack.getItemMeta()).getOwningPlayer().getName());
        }
        return Component.translatable(itemStack.translationKey());
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
