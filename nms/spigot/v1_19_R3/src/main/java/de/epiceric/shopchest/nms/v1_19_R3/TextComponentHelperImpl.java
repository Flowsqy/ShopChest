package de.epiceric.shopchest.nms.v1_19_R3;

import de.epiceric.shopchest.nms.TextComponentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class TextComponentHelperImpl implements TextComponentHelper {
    @Override
    public String getNbt(ItemStack itemStack) {
        final Tag tag = CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()).get("tag");
        return tag == null ? null : tag.getAsString();
    }
}
