package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.config.GlobalConfig;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;

import java.util.function.Supplier;

public class BuyInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.OUT_OF_STOCK));
    }

    @Override
    public void sendNotEnoughSpace() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
    }

    @Override
    public void sendInitiatorSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (seller.isRepresented()) {
            buyer.sendMessage(() -> LanguageUtils.getMessage(Message.BUY_SUCCESS,
                    new Replacement(Placeholder.AMOUNT, amount),
                    new Replacement(Placeholder.ITEM_NAME, productName),
                    new Replacement(Placeholder.BUY_PRICE, String.valueOf(moneyAmountRequired)),
                    new Replacement(Placeholder.VENDOR, seller.getName())
            ));
            return;
        }

        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.BUY_SUCCESS_ADMIN,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.BUY_PRICE, String.valueOf(moneyAmountRequired))
        ));
    }

    @Override
    public void sendTargetSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        final Supplier<String> messageSupplier = () -> LanguageUtils.getMessage(Message.SOMEONE_BOUGHT,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.BUY_PRICE, String.valueOf(moneyAmountGiven)),
                new Replacement(Placeholder.PLAYER, buyer.getName())
        );

        if (seller.canReceiveServerMessage()) {
            if (GlobalConfig.enableVendorMessages) {
                seller.sendMessage(messageSupplier);
            }
        } else if (GlobalConfig.enableVendorBungeeMessages) {
            seller.sendBungeeMessage(messageSupplier);
        }
    }

}
