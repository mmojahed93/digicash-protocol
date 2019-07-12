package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;

import java.math.BigInteger;
import java.util.Random;

public class User {

    public static final BigInteger USER_ID_1 = new BigInteger("1");
    public static final BigInteger USER_ID_2 = new BigInteger("2");

    private BigInteger secretKey;
    private BigInteger inverseOfSecretKey;

    private MoneyOrderHandler moneyOrderHandler;

    private MoneyOrderModel moneyOrder;
    private BigInteger signedMoneyOrder;
    private int indexOfSelectedMoneyOrder;


    public User(String amount, BigInteger userId) {

        secretKey = findGcdOne(Bank.publicKeySpec.getModulus());
        inverseOfSecretKey = secretKey.modPow(BigInteger.valueOf(-1), Bank.publicKeySpec.getModulus());
        moneyOrderHandler = new MoneyOrderHandler(amount, userId, secretKey);
    }

    public MoneyOrderModel getMoneyOrder() {
        return moneyOrder;
    }

    public BigInteger getSignedMoneyOrder() {
        return signedMoneyOrder;
    }

    public void createSignedMoneyOrder() {

        moneyOrderHandler.createMoneyOrderList();

        Bank bank = new Bank(moneyOrderHandler.getEncryptedMoneyOrderList(), new Bank.CheckMoneyDelegate() {
            @Override
            public MoneyOrderModel getDecryptedOrderModel(int orderIndex) {
                try {
                    return moneyOrderHandler.getMoneyOrderAt(orderIndex);
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
            Bank.SignedMoneyOrder signedMoneyOrderObj = bank.sign();
            this.indexOfSelectedMoneyOrder = signedMoneyOrderObj.getIndexOfMoneyOrder();
            this.moneyOrder = moneyOrderHandler.getMoneyOrderAt(this.indexOfSelectedMoneyOrder);

            // Remove blind factor (secretKey)
            this.signedMoneyOrder = signedMoneyOrderObj.getSignedMoneyOrder().multiply(inverseOfSecretKey);

            System.out.println("Amount: " + this.moneyOrder.getAmount());
            System.out.println("User1 Balance: " + Bank.accounts.get(User.USER_ID_1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigInteger[] getHalveXPairs(String randomBits) throws Exception {
        if (randomBits == null) {
            throw new Exception("Random bit is null!");
        }

        if (randomBits.length() != this.moneyOrder.getIdentityList().length) {
            throw new Exception("Random bit length not matched!");
        }

        IdentityModel[] identityList = this.moneyOrderHandler.getIdentityList(this.indexOfSelectedMoneyOrder);
        BigInteger[] result = new BigInteger[randomBits.length()];

        for (int i = 0; i < randomBits.length(); i++) {
            int b = Integer.parseInt(String.valueOf(randomBits.charAt(i)));

            IdentityModel.XPair xPairs = identityList[i].getxPair();
            if (b == 0) {
                result[i] = xPairs.getXl();

            } else if (b == 1) {
                result[i] = xPairs.getXr();

            } else {
                return null;
            }
        }

        return result;
    }


    private BigInteger findGcdOne(BigInteger number) {
        int upperBitLength = number.bitLength();
        Random randomSource = new Random();
        BigInteger randomNumber;

        do {
            randomNumber = new BigInteger(upperBitLength, randomSource);

        } while (randomNumber.compareTo(number) != -1
                || randomNumber.compareTo(BigInteger.ONE) != 1
                || randomNumber.gcd(number).compareTo(BigInteger.ONE) != 0); // continue until gcd = 1 (1 < randomNumber < number)

        return randomNumber;
    }
}
