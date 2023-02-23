package de.epiceric.shopchest.shop;

import org.bukkit.inventory.ItemStack;

public class Shop_ {

    private final ItemStack itemStack;
    private final ProductValue buyValue, sellValue;

    public Shop_(ItemStack itemStack, ProductValue buyValue, ProductValue sellValue) {
        this.itemStack = itemStack;
        this.buyValue = buyValue;
        this.sellValue = sellValue;
    }

    public Product getBuyProduct() {
        return new Product(itemStack, buyValue);
    }

    public Product getSellProduct() {
        return new Product(itemStack, sellValue);
    }

}
