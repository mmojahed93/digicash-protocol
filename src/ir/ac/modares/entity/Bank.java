package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import ir.ac.modares.model.UserModel;
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


    // user account info (userId, userModel)
    private static HashMap<BigInteger, UserModel> accounts = new HashMap<>();

    // Deposited Money Order (coinSerialId, depositedMoneyOrder)
    private static HashMap<String, DepositedMoneyOrder> depositedMoneyOrders = new HashMap<>();

    public static PublicKey publicKey;
    public static RSAPublicKeySpec publicKeySpec;
    private static RSAPrivateKeySpec privateKeySpec;
    private static KeyPair keys;

    static {
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

    public static void addUser(UserModel userModel) {
        if (accounts.get(userModel.getId()) != null) {
            System.out.println("User id exist!");
            return;
        }
        accounts.put(userModel.getId(), userModel);
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
        UserModel userModel = accounts.get(userId);
        BigInteger userBalance = userModel.getBalance();
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
        userModel.setBalance(newBalance);
        accounts.put(this.userId, userModel);

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
    public boolean deposit(BigInteger merchantId, BigInteger signedMoneyOrder, MoneyOrderModel moneyOrder, BigInteger[] identityHalveList) {
        UserModel merchantUser = accounts.get(merchantId);
        if (merchantUser == null) {
            System.out.println("Merchant user not found!");
            return false;
        }
        if (merchantUser.getUserType() != EntityEnum.MERCHANT) {
            System.out.println("User is not merchant!");
            return false;
        }

        if (checkMoneyOrderSignature(signedMoneyOrder, moneyOrder)) {
            System.out.println("Signature validation failed!");
            return false;
        }

        String orderSerialId = moneyOrder.getSerialId();
        if (depositedMoneyOrders.containsKey(orderSerialId)) {
            UserModel cheater = findCheater(merchantId, moneyOrder, identityHalveList);
            if (cheater != null) {
                System.out.println("[deposit] Money order has been spend! Cheater name: " +
                        cheater.getFirstName() + " " + cheater.getLastName() +
                        " username: " + cheater.getUsername());
            }

            return false;
        }

        BigInteger balance = merchantUser.getBalance();
        if (balance.equals(BigInteger.ZERO)) {
            balance = BigInteger.valueOf(0);
        }
        BigInteger newBalance = balance.add(moneyOrder.getAmount());
        merchantUser.setBalance(newBalance);

        // Add money to deposited list
        depositedMoneyOrders.put(moneyOrder.getSerialId(), new DepositedMoneyOrder(moneyOrder, identityHalveList));

        // Update merchant balance
        accounts.put(merchantId, merchantUser);

        System.out.println("[deposit] Merchant user: " + merchantUser.getUsername() + " added amount: " + moneyOrder.getAmount() + " new balance: " + newBalance);

        return true;
    }

    private boolean checkMoneyOrderSignature(BigInteger signedMoneyOrder, MoneyOrderModel moneyOrder) {

        // Fixme check
        BigInteger designed = signedMoneyOrder.modPow(publicKeySpec.getPublicExponent(), publicKeySpec.getModulus());
        BigInteger moneyOrderDigest = new BigInteger(DigestUtils.sha256(MoneyOrderHandler.serialize(moneyOrder)));
        return designed.equals(moneyOrderDigest);

    }

    private UserModel findCheater(BigInteger merchantId, MoneyOrderModel newMoneyOrder, BigInteger[] newIdentityHalveList) {
        DepositedMoneyOrder depositedMoneyOrder = depositedMoneyOrders.get(newMoneyOrder.getSerialId());
        if (depositedMoneyOrder == null) {
            return null;
        }

        BigInteger[] oldIdentityHalveList = depositedMoneyOrder.getIdentityHalveList();

        for (int i = 0; i < oldIdentityHalveList.length; i++) {
            if (!oldIdentityHalveList[i].equals(newIdentityHalveList[i])) {
                // Consumer is cheater, find his id
                BigInteger cheaterId = oldIdentityHalveList[i].xor(newIdentityHalveList[i]);
                UserModel cheaterUserModel = accounts.get(cheaterId);
                if (cheaterUserModel != null) {
                    return cheaterUserModel;
                } else {
                    System.out.println("Some problem occurred! Cheater Consumer info not found!");
                    return null;
                }
            }
        }

        UserModel merchantUserModel = accounts.get(merchantId);
        if (merchantUserModel != null) {
            return merchantUserModel;
        } else {
            System.out.println("Some problem occurred! Cheater Merchant info not found!");
            return null;
        }

    }

    public BigInteger getUserBalance(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        for (BigInteger userId : accounts.keySet()) {
            UserModel userModel = accounts.get(userId);
            if (userModel.getUsername().equals(username) && userModel.getPassword().equals(password)) {
                return userModel.getBalance();
            }
        }

        return null;
    }

}
