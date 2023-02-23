package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.event.ShopBuySellEvent;
import org.bukkit.inventory.ItemStack;

// TODO Moved outclass :
// - Update hologram
// - Log economy in database

public class Transaction {

    private final Actor buyer, seller;
    private final TransactionInformer informer;
    private final ItemStack itemStack;
    private int amount;
    private final double moneyAmountRequired, moneyAmountGiven;

    public Transaction(Actor buyer, Actor seller, TransactionInformer informer, ItemStack itemStack, double moneyAmountRequired, double moneyAmountGiven) {
        this.buyer = buyer;
        this.seller = seller;
        this.informer = informer;
        this.itemStack = itemStack;
        this.moneyAmountRequired = moneyAmountRequired;
        this.moneyAmountGiven = moneyAmountGiven;
    }

    /**
     * Process to the transaction
     *
     * @return {@code true} if the transaction occurs. {@code false} otherwise
     */
    public boolean process() {
        //plugin.getDebugLogger().debug(executor.getName() + " is buying (#" + shop.getID() + ")");
        //plugin.getDebugLogger().debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        if (!check()) {
            return false;
        }

        // Call buy or sell event
        if (processEvent()) {
            return false;
        }

        //database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

        transferMoney();

        transferItems();

        inform();

        // log

        return true;
    }

    /**
     * Check if the transaction can be performed and inform actors
     *
     * @return {@code true} if the transaction can be performed
     */
    private boolean check() {
        // Check buyer money
        if (!buyer.hasMoney(moneyAmountRequired)) {
            informer.sendNotEnoughMoney();
            return false;
        }

        //plugin.getDebugLogger().debug(executor.getName() + " has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

        // Check seller item quantity
        if (!seller.hasProductAmount(itemStack, amount)) {
            informer.sendNotEnoughItem();
            return false;
        }

        // Check buyer inventory space
        if (!buyer.hasEnoughInventorySpace(itemStack, amount)) {
            informer.sendNotEnoughSpace();
            return false;
        }

        //plugin.getDebugLogger().debug(executor.getName() + " has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");


        /*
        plugin.getDebugLogger().debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
        plugin.getLogger().info(String.format("%s bought %d of %s from %s", executor.getName(), finalNewAmount1, newProduct.getItemStack().toString(), "ADMIN"));

        plugin.getDebugLogger().debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
        plugin.getLogger().info(String.format("%s sold %d of %s from %s", executor.getName(), finalNewAmount, newProduct.getItemStack().toString(), vendorName));
        */
        return true;
    }

    /**
     * Call {@link ShopBuySellEvent}
     *
     * @return {@code true} if the event is cancelled. {@code false} otherwise.
     */
    private boolean processEvent() {
        /*
        final ShopBuySellEvent transactionEvent = new ShopBuySellEvent();
        Bukkit.getPluginManager().callEvent(transactionEvent);
        if (plugin.getDebugLogger().debug("Buy event cancelled (#" + shop.getID() + ")");
        return transactionEvent.isCancelled();
        */
        return true;
    }

    private void transferMoney() {
        // TODO Check for error during transaction

        /*
        plugin.getDebugLogger().debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
        executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
         */

        // Remove buyer money

        // Add seller money
    }

    private void transferItems() {
        // Remove items from seller inventory

        // Add items in buyer inventory
    }

    private void inform() {
        String amountStr = null;
        String productName = null;
        informer.sendInitiatorSuccess(amountStr, productName, moneyAmountGiven, moneyAmountRequired);
        informer.sendTargetSuccess(amountStr, productName, moneyAmountGiven, moneyAmountRequired);
    }

}
