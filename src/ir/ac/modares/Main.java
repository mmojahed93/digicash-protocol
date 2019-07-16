package ir.ac.modares;

import ir.ac.modares.entity.Bank;
import ir.ac.modares.entity.Consumer;
import ir.ac.modares.entity.EntityEnum;
import ir.ac.modares.entity.Merchant;
import ir.ac.modares.model.MoneyOrderModel;
import ir.ac.modares.model.UserModel;

import java.math.BigInteger;

public class Main {


    public static final BigInteger USER_ID_1 = BigInteger.valueOf(1);
    public static final BigInteger USER_ID_2 = BigInteger.valueOf(2);
    public static final BigInteger USER_ID_3 = BigInteger.valueOf(3);

    public static void main(String[] args) {

        UserModel consumerUser = new UserModel(Main.USER_ID_1, "Saber", "Eskandari", "s_eskandari", "123", new BigInteger("1000000"), EntityEnum.CONSUMER);
        Bank.addUser(consumerUser);

        Consumer consumer = new Consumer(consumerUser, "5000");
        consumer.createSignedMoneyOrder();
        BigInteger signedMoneyOrder = consumer.getSignedMoneyOrder();
        MoneyOrderModel moneyOrder = consumer.getMoneyOrder();

        UserModel merchantUser = new UserModel(Main.USER_ID_3, "Ali", "Nazari", "ali_nazari", "123", new BigInteger("3000000"), EntityEnum.MERCHANT);
        Bank.addUser(merchantUser);

        Merchant merchant = new Merchant(merchantUser,signedMoneyOrder, moneyOrder);
        String merchantRandomBits = merchant.getRandomBits();

        try {
            BigInteger[] halves = consumer.getHalveXPairs(merchantRandomBits);
            boolean moneyOrderCheckResult = merchant.spendMoneyOrder(halves);
            System.out.println("spendMoneyOrder: " + moneyOrderCheckResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
