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
    public static final BigInteger USER_ID_4 = BigInteger.valueOf(4);

    public static void main(String[] args) {

        UserModel consumerUser = new UserModel(Main.USER_ID_1, "Saber", "Eskandari", "s_eskandari", "123", new BigInteger("1000000"), EntityEnum.CONSUMER);
        Bank.addUser(consumerUser);

        Consumer consumer = new Consumer(consumerUser);
        consumer.createSignedMoneyOrder("5000");
        BigInteger signedMoneyOrder = consumer.getSignedMoneyOrder();
        MoneyOrderModel moneyOrder = consumer.getMoneyOrder();

        UserModel merchantUser = new UserModel(Main.USER_ID_3, "Ali", "Nazari", "ali_nazari", "123", new BigInteger("3000000"), EntityEnum.MERCHANT);
        Bank.addUser(merchantUser);

        Merchant merchant = new Merchant(merchantUser, signedMoneyOrder, moneyOrder);
        String merchantRandomBits = merchant.getRandomBits();

        try {
            BigInteger[] halves = consumer.getHalveXPairs(merchantRandomBits);
            boolean spendMoneyOrder = merchant.spendMoneyOrder(halves);
            System.out.println("spendMoneyOrder: " + spendMoneyOrder);

            boolean spendMoneyOrder2 = merchant.spendMoneyOrder(halves);
            System.out.println("spendMoneyOrder2: " + spendMoneyOrder2);


        } catch (Exception e) {
            e.printStackTrace();
        }



        UserModel merchantUser2 = new UserModel(Main.USER_ID_4, "Reza", "Taghavi", "reza_taghavi", "123", new BigInteger("3000000"), EntityEnum.MERCHANT);
        Bank.addUser(merchantUser2);

        Merchant merchant2 = new Merchant(merchantUser2, signedMoneyOrder, moneyOrder);
        String merchantRandomBits2 = merchant2.getRandomBits();

        try {
            BigInteger[] halves = consumer.getHalveXPairs(merchantRandomBits2);
            boolean spendMoneyOrder = merchant2.spendMoneyOrder(halves);
            System.out.println("spendMoneyOrder2: " + spendMoneyOrder);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
