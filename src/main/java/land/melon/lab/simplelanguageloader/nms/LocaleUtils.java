package land.melon.lab.simplelanguageloader.nms;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * from: cat.nyaa.nyaacore.utils
 * https://github.com/NyaaCat/NyaaCore/blob/1.18/src/main/java/cat/nyaa/nyaacore/utils/LocaleUtils.java
 */
public class LocaleUtils {
    public static String getUnlocalizedName(Material material) {
        if (material == null) throw new IllegalArgumentException();
        return namespaceKeyToTranslationKey(material.isBlock() ? "block" : "item", material.getKey());
    }

    public static BaseComponent getTranslatableItemComponent(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return new TextComponent(itemStack.getItemMeta().getDisplayName());
        if (itemStack.getItemMeta() instanceof SkullMeta && ((SkullMeta) itemStack.getItemMeta()).hasOwner()) {
            String key = getUnlocalizedName(itemStack.getType()) + ".named";
            return new TranslatableComponent(key, ((SkullMeta) itemStack.getItemMeta()).getOwningPlayer().getName());
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return new TranslatableComponent(nmsItemStack.getItem().getDescriptionId(nmsItemStack));
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
