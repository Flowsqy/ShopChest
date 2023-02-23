package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.shop.Product;

public class TransactionRequest {

    private final Actor buyer, seller;
    private final Product product;
    private final int amount;

    public TransactionRequest(Actor buyer, Actor seller, Product product, int amount) {
        this.buyer = buyer;
        this.seller = seller;
        this.product = product;
        this.amount = amount;
    }

    public Transaction prepare() {
        // Calculate price

        // Apply taxes

        return null;
    }

}
