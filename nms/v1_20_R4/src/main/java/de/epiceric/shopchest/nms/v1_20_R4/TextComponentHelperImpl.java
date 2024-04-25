package de.epiceric.shopchest.nms.v1_20_R4;

import de.epiceric.shopchest.nms.TextComponentHelper;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class TextComponentHelperImpl implements TextComponentHelper {
    @Override
    public String getNbt(ItemStack itemStack) {
        // Get NMS ItemStack
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);

        // Find the "tag" tag and retrieve its CompoundTag
        Optional<CompoundTag> tagOptional = nmsItemStack.getTags()
                .filter(tag -> tag.toString().equals("tag"))
                .map(tag -> (CompoundTag) CompoundTag.TYPE)
                .findFirst();

        // Return the string representation of the "tag" CompoundTag if present, or null otherwise
        return tagOptional.map(CompoundTag::getAsString).orElse(null);


        //final Tag tag = CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()).get("tag");
        //return tag == null ? null : tag.getAsString();
    }
}
