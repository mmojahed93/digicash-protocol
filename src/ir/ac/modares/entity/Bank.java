package ir.ac.modares.entity;

import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Bank {

    public static class SignedMoneyOrder {
        private byte[] moneyOrder;
        private byte[] signedOrderMoney;

        public SignedMoneyOrder(byte[] moneyOrder, byte[] signedOrderMoney) {
            this.moneyOrder = moneyOrder;
            this.signedOrderMoney = signedOrderMoney;
        }

        public byte[] getMoneyOrder() {
            return moneyOrder;
        }

        public byte[] getSignedOrderMoney() {
            return signedOrderMoney;
        }
    }


    // user account info (userId, balance)
    private static HashMap<BigInteger, BigInteger> accounts = new HashMap<>();

    static {
        accounts.put(User.USER_ID_1, new BigInteger("1000000"));
        accounts.put(User.USER_ID_2, new BigInteger("2000000"));
    }

    public interface CheckMoneyDelegate {
        public MoneyOrderModel getDecryptedOrderModel(int orderIndex);

        public IdentityModel.XPair getXPair(int orderIndex, int pairIndex);
    }

    private BigInteger[] encryptedMoneyOrderList;
    private CheckMoneyDelegate checkMoneyOrderDelegate;

    private BigInteger moneyOrderAmount;
    private BigInteger userId;

    private ArrayList<Integer> possibleIndexes;

    private KeyPair keys;


    public Bank(BigInteger[] encryptedMoneyOrderList, CheckMoneyDelegate checkMoneyOrderDelegate) {
        this.encryptedMoneyOrderList = encryptedMoneyOrderList;
        this.checkMoneyOrderDelegate = checkMoneyOrderDelegate;

        initKey();
    }

    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    private void initKey() {
        try {
            keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public SignedMoneyOrder sign() throws Exception {
        boolean check = checkMoneyOrderList();
        if (!check) {
            return null;
        }

        //
        // Check possibleIndex has valid item
        //
        if (possibleIndexes == null || possibleIndexes.size() != 1) {
            throw new Exception("Something is wrong!");
        }

        //
        // Check user account exist and it's balance
        //
        BigInteger userBalance = accounts.get(userId);
        if (userBalance == null || userBalance.compareTo(this.moneyOrderAmount) < 0) {
            return null;
        }

        BigInteger finalMoneyOrder = encryptedMoneyOrderList[possibleIndexes.get(0)];


        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keys.getPrivate());
            byte[] finalMoneyOrderByteArr = finalMoneyOrder.toByteArray();
            byte[] signedMoneyOrder = cipher.doFinal(DigestUtils.sha256(finalMoneyOrderByteArr));
            SignedMoneyOrder signedMoneyOrderResult = new SignedMoneyOrder(finalMoneyOrderByteArr, signedMoneyOrder);

            //
            // Update user balance
            //
            BigInteger newBalance = userBalance.subtract(this.moneyOrderAmount);
            accounts.put(this.userId, newBalance);

            return signedMoneyOrderResult;

        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {

            e.printStackTrace();
        }

        return null;
    }

    private boolean checkMoneyOrderList() {

        possibleIndexes = new ArrayList<>();
        for (int i = 0; i < encryptedMoneyOrderList.length; i++) {
            possibleIndexes.add(i);
        }

        this.moneyOrderAmount = null;
        this.userId = null;

        Random random = new Random();
        //
        // Check for all order_len - 1 (until size of possibleIndexes makes 1)
        //
        while (possibleIndexes.size() != 1) {

            int upperRandomLimit = possibleIndexes.size();
            int randomOrderIndex = possibleIndexes.get(random.nextInt(upperRandomLimit));
            possibleIndexes.removeIf(s -> s.equals(randomOrderIndex));

            MoneyOrderModel decryptedOrderModel = checkMoneyOrderDelegate.getDecryptedOrderModel(randomOrderIndex);
            if (decryptedOrderModel == null) {
                return false;
            }

            BigInteger orderAmount = decryptedOrderModel.getAmount();
            if (moneyOrderAmount == null) {
                moneyOrderAmount = orderAmount;
            }

            //
            // Check all amount is same
            //
            if (!moneyOrderAmount.equals(orderAmount)) {
                return false;
            }

            //
            // Check identity pairs
            //
            IdentityModel.HPair[] hPairs = decryptedOrderModel.getIdentityList();
            for (int i = 0; i < hPairs.length; i++) {
                IdentityModel.HPair hPair = hPairs[i];
                IdentityModel.XPair xPair = checkMoneyOrderDelegate.getXPair(randomOrderIndex, i);

                //
                // Check money order HPair with corresponding hash of xPair
                //
                BigInteger newHl = new BigInteger(DigestUtils.sha256(xPair.getXl().toByteArray()));
                BigInteger newHr = new BigInteger(DigestUtils.sha256(xPair.getXr().toByteArray()));

                if (!newHl.equals(hPair.getHl()) || !newHr.equals(hPair.getHr())) {
                    return false;
                }

                //
                // Check all money order has save UserId
                //
                BigInteger orderUserId = xPair.getXl().xor(xPair.getXr());
                if (userId == null) {
                    userId = orderUserId;
                }

                if (!userId.equals(orderUserId)) {
                    return false;
                }
            }
        }

        return true;
    }

}
