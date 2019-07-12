package ir.ac.modares;

import ir.ac.modares.entity.Merchant;
import ir.ac.modares.entity.User;
import ir.ac.modares.model.MoneyOrderModel;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {

        User user = new User("5000", User.USER_ID_1);
        user.createSignedMoneyOrder();
        BigInteger signedMoneyOrder = user.getSignedMoneyOrder();
        MoneyOrderModel moneyOrder = user.getMoneyOrder();

        Merchant merchant = new Merchant(signedMoneyOrder, moneyOrder);
        String merchantRandomBits = merchant.getRandomBits();

        try {
            BigInteger[] halves = user.getHalveXPairs(merchantRandomBits);
            boolean moneyOrderCheckResult = merchant.checkMoneyOrder(halves);
            System.out.println("moneyOrderCheckResult: " + moneyOrderCheckResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
