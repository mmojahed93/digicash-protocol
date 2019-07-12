package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;

import java.math.BigInteger;

public class User {

    public static final BigInteger USER_ID_1 = new BigInteger("1");
    public static final BigInteger USER_ID_2 = new BigInteger("2");

    private Bank.SignedMoneyOrder signedMoneyOrder;


    public User() {
    }

    public Bank.SignedMoneyOrder getSignedMoneyOrder() {
        return signedMoneyOrder;
    }

    public void createSignedMoneyOrder() {
        MoneyOrderHandler moneyOrderHandler = new MoneyOrderHandler("5000", User.USER_ID_1);
        moneyOrderHandler.createMoneyOrderList();

        Bank bank = new Bank(moneyOrderHandler.getEncryptedMoneyOrderList(), new Bank.CheckMoneyDelegate() {
            @Override
            public MoneyOrderModel getDecryptedOrderModel(int orderIndex) {
                try {
                    return moneyOrderHandler.getMoneyOrderModelAt(orderIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public IdentityModel.XPair getXPair(int orderIndex, int pairIndex) {
                return moneyOrderHandler.getXPair(orderIndex, pairIndex);
            }
        });

        try {
            signedMoneyOrder = bank.sign();
            MoneyOrderModel moneyOrderModel = moneyOrderHandler.getMoneyOrderModelAt(signedMoneyOrder.getIndexOfMoneyOrder());
            System.out.println("Amount: " + moneyOrderModel.getAmount());
            System.out.println("User1 Balance: " + Bank.accounts.get(User.USER_ID_1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
