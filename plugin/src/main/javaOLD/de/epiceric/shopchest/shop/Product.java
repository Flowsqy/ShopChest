package de.epiceric.shopchest.shop;

import org.bukkit.inventory.ItemStack;

public class Product {

    private final ItemStack itemStack;
    private final ProductValue productValue;

    public Product(ItemStack itemStack, ProductValue productValue) {
        this.itemStack = itemStack;
        this.productValue = productValue;
    }



}
