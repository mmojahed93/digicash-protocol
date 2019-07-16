package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Bank {
    private static class DepositedMoneyOrder {
        private MoneyOrderModel moneyOrder;
        private BigInteger[] identityHalveList;

        public DepositedMoneyOrder(MoneyOrderModel moneyOrder, BigInteger[] identityHalveList) {
            this.moneyOrder = moneyOrder;
            this.identityHalveList = identityHalveList;
        }

        public MoneyOrderModel getMoneyOrder() {
            return moneyOrder;
        }

        public BigInteger[] getIdentityHalveList() {
            return identityHalveList;
        }
    }


    public static class SignedMoneyOrder {
        private int indexOfMoneyOrder;
        private BigInteger signedMoneyOrder;

        public SignedMoneyOrder(int indexOfMoneyOrder, BigInteger signedMoneyOrder) {
            this.indexOfMoneyOrder = indexOfMoneyOrder;
            this.signedMoneyOrder = signedMoneyOrder;
        }

        public int getIndexOfMoneyOrder() {
            return indexOfMoneyOrder;
        }

        public BigInteger getSignedMoneyOrder() {
            return signedMoneyOrder;
        }
    }


    // user account info (userId, balance)
    public static HashMap<BigInteger, BigInteger> accounts = new HashMap<>();

    // Deposited Money Order (coinSerialId, depositedMoneyOrder)
    public static HashMap<String, DepositedMoneyOrder> depositedMoneyOrders = new HashMap<>();

    public static PublicKey publicKey;
    public static RSAPublicKeySpec publicKeySpec;
    private static RSAPrivateKeySpec privateKeySpec;
    private static KeyPair keys;

    static {
        accounts.put(User.USER_ID_1, new BigInteger("1000000"));
        accounts.put(User.USER_ID_2, new BigInteger("2000000"));

        initKeys();
    }

    private static void initKeys() {
        try {
            // Get an instance of the RSA key generator
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);

            // Generate the KeyPair
            keys = keyPairGenerator.generateKeyPair();

            // Get the public and private key
            publicKey = keys.getPublic();
            PrivateKey privateKey = keys.getPrivate();

            // Get the RSAPublicKeySpec and RSAPrivateKeySpec
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            privateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CheckMoneyDelegate {
        MoneyOrderModel getDecryptedOrderModel(int orderIndex);

        IdentityModel.XPair getXPair(int orderIndex, int pairIndex);
    }

    private BigInteger[] encryptedMoneyOrderList;
    private CheckMoneyDelegate checkMoneyOrderDelegate;

    private BigInteger moneyOrderAmount;
    private BigInteger userId;

    private ArrayList<Integer> possibleIndexes;

    public Bank() {
    }

    public void setEncryptedMoneyOrderList(BigInteger[] encryptedMoneyOrderList) {
        this.encryptedMoneyOrderList = encryptedMoneyOrderList;
    }

    public void setCheckMoneyOrderDelegate(CheckMoneyDelegate checkMoneyOrderDelegate) {
        this.checkMoneyOrderDelegate = checkMoneyOrderDelegate;
    }

    ////////////////////////////
    // Sign user money order
    ////////////////////////////
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

        int moneyOrderIndex = possibleIndexes.get(0);
        BigInteger encryptedMoneyOrder = encryptedMoneyOrderList[moneyOrderIndex];
        BigInteger signedMoneyOrder = encryptedMoneyOrder.modPow(privateKeySpec.getPrivateExponent(), privateKeySpec.getModulus());


        //
        // Update user balance
        //
        BigInteger newBalance = userBalance.subtract(this.moneyOrderAmount);
        accounts.put(this.userId, newBalance);

        return new SignedMoneyOrder(moneyOrderIndex, signedMoneyOrder);

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

    ////////////////////////////
    // Deposit merchant money
    ////////////////////////////
    public boolean deposit(BigInteger userId, BigInteger signedMoneyOrder, MoneyOrderModel moneyOrder, BigInteger[] identityHalveList) {

        if (checkMoneyOrderSignature(signedMoneyOrder, moneyOrder)) {
            System.out.println("Signature validation failed!");
            return false;
        }

        String orderSerialId = moneyOrder.getSerialId();
        if (depositedMoneyOrders.containsKey(orderSerialId)) {
            EntityEnum cheater = findCheater(moneyOrder, identityHalveList);
            System.out.println("Money order has been spend! Cheater is: " + cheater);
            return false;
        }

        BigInteger balance = accounts.get(userId);
        if (balance.equals(BigInteger.ZERO)) {
            balance = BigInteger.valueOf(0);
        }
        BigInteger newBalance = balance.add(moneyOrder.getAmount());

        // Add money to deposited list
        depositedMoneyOrders.put(moneyOrder.getSerialId(), new DepositedMoneyOrder(moneyOrder, identityHalveList));

        // Update merchant balance
        accounts.put(userId, newBalance);

        return true;
    }

    private boolean checkMoneyOrderSignature(BigInteger signedMoneyOrder, MoneyOrderModel moneyOrder) {

        // Fixme check
        BigInteger designed = signedMoneyOrder.modPow(publicKeySpec.getPublicExponent(), publicKeySpec.getModulus());
        BigInteger moneyOrderDigest = new BigInteger(DigestUtils.sha256(MoneyOrderHandler.serialize(moneyOrder)));
        return designed.equals(moneyOrderDigest);

    }

    private EntityEnum findCheater(MoneyOrderModel newMoneyOrder, BigInteger[] newIdentityHalveList) {
        DepositedMoneyOrder depositedMoneyOrder = depositedMoneyOrders.get(newMoneyOrder.getSerialId());
        if (depositedMoneyOrder == null) {
            return EntityEnum.NONE;
        }

        BigInteger[] oldIdentityHalveList = depositedMoneyOrder.getIdentityHalveList();

        for (int i = 0; i < oldIdentityHalveList.length; i++) {
            if (!oldIdentityHalveList[i].equals(newIdentityHalveList[i])) {
                // todo find user id
                return EntityEnum.USER;
            }
        }

        return EntityEnum.MERCHANT;
    }

}
