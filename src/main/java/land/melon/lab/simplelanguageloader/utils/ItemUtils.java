package land.melon.lab.simplelanguageloader.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static Component itemTextWithHover(ItemStack itemStack) {
        var itemComponent = LocaleUtils.getTranslatableItemComponent(itemStack);
        return itemComponent.hoverEvent(itemStack);
    }

}
