package de.epiceric.shopchest.transaction;

import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public interface Actor {

    /**
     * Check if this {@link Actor} is represented by an entity
     * <br>
     * (e.g. An admin shop is not represented; A player shop is represented (by a player owner))
     *
     * @return Whether this {@link Actor} is represented
     */
    boolean isRepresented();

    /**
     * Get the name of this {@link Actor}
     *
     * @return The name of this {@link Actor}
     */
    String getName();

    /**
     * Check if this {@link Actor} have a specified amount of money
     *
     * @param moneyAmount The money amount required
     * @return Whether the {@link Actor} have the money
     */
    boolean hasMoney(double moneyAmount);

    /**
     * Check if this {@link Actor} have a specified amount of an item
     *
     * @param itemStack The {@link ItemStack} that represent the product
     * @param amount    The amount to check
     * @return Whether the {@link Actor} have the amount of this item stack or more
     */
    boolean hasProductAmount(ItemStack itemStack, int amount);

    /**
     * Check if this {@link Actor} have enough inventory space to accept this item stack with this amount
     *
     * @param itemStack The {@link ItemStack} that represent the product
     * @param amount    The amount to check
     * @return Whether the {@link Actor} have the space to accept this amount of this item stack in its inventory
     */
    boolean hasEnoughInventorySpace(ItemStack itemStack, int amount);

    /**
     * Check if this {@link Actor} can receive a message on this server
     *
     * @return {@code true} if this {@link Actor} can receive a server message. {@code false} otherwise
     */
    boolean canReceiveServerMessage();

    /**
     * Send a message to this {@link Actor}
     *
     * @param messageSupplier The message supplier that provide the message to send
     */
    void sendMessage(Supplier<String> messageSupplier);

    /**
     * Send a message through BungeeCord to this {@link Actor}
     *
     * @param messageSupplier The message supplier that provide the message to send
     */
    void sendBungeeMessage(Supplier<String> messageSupplier);

}
