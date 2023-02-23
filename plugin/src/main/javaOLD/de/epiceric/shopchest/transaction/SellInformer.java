package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.config.GlobalConfig;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;

import java.util.function.Supplier;

public class SellInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
    }

    @Override
    public void sendNotEnoughSpace() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
    }

    @Override
    public void sendInitiatorSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (buyer.isRepresented()) {
            seller.sendMessage(() -> LanguageUtils.getMessage(Message.SELL_SUCCESS,
                    new Replacement(Placeholder.AMOUNT, amount),
                    new Replacement(Placeholder.ITEM_NAME, productName),
                    new Replacement(Placeholder.SELL_PRICE, String.valueOf(moneyAmountGiven)),
                    new Replacement(Placeholder.VENDOR, buyer.getName())
            ));
            return;
        }

        // IF ADMIN SHOP
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.SELL_SUCCESS_ADMIN,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.SELL_PRICE, String.valueOf(moneyAmountGiven))
        ));
    }

    @Override
    public void sendTargetSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        final Supplier<String> messageSupplier = () -> LanguageUtils.getMessage(Message.SOMEONE_SOLD,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.SELL_PRICE, String.valueOf(moneyAmountGiven)),
                new Replacement(Placeholder.PLAYER, buyer.getName())
        );
        if (buyer.canReceiveServerMessage()) {
            if (GlobalConfig.enableVendorMessages) {
                buyer.sendMessage(messageSupplier);
            }
        } else if (GlobalConfig.enableVendorBungeeMessages) {
            buyer.sendBungeeMessage(messageSupplier);
        }
    }

}
