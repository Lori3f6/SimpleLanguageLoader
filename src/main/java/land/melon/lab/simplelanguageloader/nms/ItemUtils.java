package land.melon.lab.simplelanguageloader.nms;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class ItemUtils {
    public static Item itemStackToContent(ItemStack itemStack) {
        var clone = itemStack.clone();
        if (clone.hasItemMeta() && clone.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.setPages(new ArrayList<>());
            clone.setItemMeta(bookMeta);
        }
        return new Item(clone.getType().name().toLowerCase(), clone.getAmount(), ItemTag.ofNbt(itemStackToJson(clone)));
    }

    public static BaseComponent itemTextWithHover(ItemStack itemStack){
        var itemComponent = LocaleUtils.getTranslatableItemComponent(itemStack);
        itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(ItemUtils.itemStackToJson(itemStack))}));
        return itemComponent;
    }

    public static String itemStackToJson(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()).toString();
    }
}
