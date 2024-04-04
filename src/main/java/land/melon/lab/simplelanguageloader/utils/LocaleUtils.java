package land.melon.lab.simplelanguageloader.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * From: <a href="https://github.com/NyaaCat/NyaaCore/blob/1.18/src/main/java/cat/nyaa/nyaacore/utils/LocaleUtils.java">cat.nyaa.nyaacore.utils.LocaleUtils.java</a>
 */
public class LocaleUtils {
    public static String getUnlocalizedName(Material material) {
//        if (material == null) throw new IllegalArgumentException();
//        return namespaceKeyToTranslationKey(material.isBlock() ? "block" : "item", material.getKey());
        return material.translationKey();
    }

    public static Component getTranslatableItemComponent(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return Component.text().style(Style.style(TextDecoration.ITALIC)).append(itemStack.getItemMeta().displayName()).asComponent();
        if (itemStack.getItemMeta() instanceof SkullMeta && ((SkullMeta) itemStack.getItemMeta()).hasOwner()) {
            String key = getUnlocalizedName(itemStack.getType()) + ".named";
            return Component.translatable(key, (String) null, Component.text(((SkullMeta) itemStack.getItemMeta()).getOwningPlayer().getName()));
        }
        return Component.translatable(itemStack.translationKey());
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
