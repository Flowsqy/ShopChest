package de.epiceric.shopchest.transaction;

public interface TransactionInformer {

    void sendNotEnoughMoney();

    void sendNotEnoughItem();

    void sendNotEnoughSpace();

    void sendInitiatorSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired);

    void sendTargetSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired);

}
