package ir.ac.modares;

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
import java.util.Random;

public class Bank {

    static {

    }

    public interface CheckMoneyDelegate {
        public MoneyOrderModel getDecryptedOrderModel(int index);

        public IdentityModel.XPair getRealPair(int index);
    }

    private BigInteger[] encryptedMoneyOrderList;
    private CheckMoneyDelegate checkMoneyOrderDelegate;

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

    public byte[] sign() throws Exception {
        byte[] signedMoneyOrder = null;

        boolean check = checkMoneyOrderList();
        if (!check) {
            return signedMoneyOrder;
        }

        //
        // Check possibleIndex has valid item
        //
        if (possibleIndexes == null || possibleIndexes.size() != 1) {
            throw new Exception("Something is wrong!");
        }

        BigInteger finalMoneyOrder = encryptedMoneyOrderList[possibleIndexes.get(0)];


        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keys.getPrivate());
            signedMoneyOrder = cipher.doFinal(finalMoneyOrder.toByteArray());


        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {

            e.printStackTrace();
        }

        return signedMoneyOrder;

    }

    private boolean checkMoneyOrderList() {

        possibleIndexes = new ArrayList<>();
        for (int i = 0; i < encryptedMoneyOrderList.length; i++) {
            possibleIndexes.add(i);
        }

        BigInteger amount = null;
        BigInteger userId = null;

        //
        // Check for all order_len - 1 (until size of possibleIndexes makes 1)
        //
        while (possibleIndexes.size() != 1) {

            int upperRandomLimit = possibleIndexes.size();
            int randomIndex = possibleIndexes.get(new Random(upperRandomLimit).nextInt());
            possibleIndexes.remove(randomIndex);

            MoneyOrderModel decryptedOrderModel = checkMoneyOrderDelegate.getDecryptedOrderModel(randomIndex);

            BigInteger orderAmount = decryptedOrderModel.getAmount();
            if (amount == null) {
                amount = orderAmount;
            }

            //
            // Check all amount is same
            //
            if (!amount.equals(orderAmount)) {
                return false;
            }

            //
            // Check identity pairs
            //
            IdentityModel.HPair[] hPairs = decryptedOrderModel.getIdentityList();
            for (int i = 0; i < hPairs.length; i++) {
                IdentityModel.HPair hPair = hPairs[i];
                IdentityModel.XPair xPair = checkMoneyOrderDelegate.getRealPair(i);

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
