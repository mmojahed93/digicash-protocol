package ir.ac.modares;

import ir.ac.modares.entity.Consumer;
import ir.ac.modares.entity.Merchant;
import ir.ac.modares.model.MoneyOrderModel;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {

        Consumer consumer = new Consumer("5000", Consumer.USER_ID_1);
        consumer.createSignedMoneyOrder();
        BigInteger signedMoneyOrder = consumer.getSignedMoneyOrder();
        MoneyOrderModel moneyOrder = consumer.getMoneyOrder();

        Merchant merchant = new Merchant(signedMoneyOrder, moneyOrder);
        String merchantRandomBits = merchant.getRandomBits();

        try {
            BigInteger[] halves = consumer.getHalveXPairs(merchantRandomBits);
            boolean moneyOrderCheckResult = merchant.checkMoneyOrder(halves);
            System.out.println("moneyOrderCheckResult: " + moneyOrderCheckResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
