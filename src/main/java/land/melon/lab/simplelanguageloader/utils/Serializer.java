package land.melon.lab.simplelanguageloader.utils;

import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;

public class Serializer {
    /**
     * https://github.com/sainttx/Auctions/blob/12533c9af0b1dba700473bf728895abb9ff5b33b/Auctions/src/main/java/com/sainttx/auctions/SimpleMessageFactory.java#L197
     * Convert an item to its JSON representation to be shown in chat.
     * NOTE: this method has no corresponding deserializer.
     */
    public static String itemStackToJson(ItemStack itemStack) throws RuntimeException {
        try {
            var nmsNbtTagCompound =
                    Class.forName("NBTTagCompound").getDeclaredConstructor((Class<?>) null).newInstance();
            var nmsItem =
                    Class.forName("CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
            return nmsItem.getClass().getDeclaredMethod("save", Class.forName("NBTTagCompound")).invoke(nmsItem, nmsNbtTagCompound).toString();
        } catch (Throwable t) {
            throw new RuntimeException("failed to serialize itemstack to nms item", t);
        }
    }

    public static Item itemStackToItem(ItemStack itemStack) {
        return new Item(itemStack.getType().name().toLowerCase(),itemStack.getAmount(),ItemTag.ofNbt(itemStackToJson(itemStack)));
    }
}
