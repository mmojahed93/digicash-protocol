package ir.ac.modares;

import ir.ac.modares.entity.Bank;
import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MoneyOrderHandler {

    private class OrderCreationResult {
        MoneyOrderModel moneyOrderModel;
        IdentityModel[] identities;

        public OrderCreationResult(MoneyOrderModel moneyOrderModel, IdentityModel[] identities) {
            this.moneyOrderModel = moneyOrderModel;
            this.identities = identities;
        }

    }

    private final BigInteger upperLimit = new BigInteger("2455155546008943817740293915197451784769108058161191238065");

    private final int moneyOrderLen = 100;
    private final int identityLen = 100;

    private MoneyOrderModel[] moneyOrderList = new MoneyOrderModel[moneyOrderLen];
    private BigInteger[] encryptedMoneyOrderList = new BigInteger[moneyOrderLen];

    private Set<Integer> checkedMoneyOrderIndexes = new HashSet<>();

    private BigInteger userId;
    private String amount;
    private BigInteger secretKey;

    private IdentityModel[][] identityList = new IdentityModel[moneyOrderLen][identityLen];


    public MoneyOrderHandler(String amount, BigInteger userId, BigInteger secretKey) {
        this.amount = amount;
        this.userId = userId;
        this.secretKey = secretKey;
    }

    public MoneyOrderModel[] getMoneyOrderList() {
        return moneyOrderList;
    }

    public BigInteger[] getEncryptedMoneyOrderList() {
        return encryptedMoneyOrderList;
    }

    public void createMoneyOrderList() {

        BigInteger blindFactor = secretKey.modPow(Bank.publicKeySpec.getPublicExponent(), Bank.publicKeySpec.getModulus());
        for (int i = 0; i < moneyOrderList.length; i++) {
            OrderCreationResult result = createMoneyOrder();
            moneyOrderList[i] = result.moneyOrderModel;
            identityList[i] = result.identities;

            byte[] moneyOrderDigest = DigestUtils.sha256(serialize(result.moneyOrderModel));
            // Make money order blind
            encryptedMoneyOrderList[i] = new BigInteger(moneyOrderDigest).multiply(blindFactor);
        }

    }

    public MoneyOrderModel getMoneyOrderAt(int index) throws Exception {
        if (index < 0 || index >= moneyOrderList.length) {
            throw new Exception("Index out of range!");
        }
        if (checkedMoneyOrderIndexes.size() >= moneyOrderList.length - 1) {
            throw new Exception("Request limitation!");
        }
        return moneyOrderList[index];
    }

    public IdentityModel[] getIdentityList(int orderIndex) {
        return identityList[orderIndex];
    }

    public IdentityModel.XPair getXPair(int orderIndex, int pairIndex) {
        return identityList[orderIndex][pairIndex].getxPair();
    }

    private OrderCreationResult createMoneyOrder() {

        IdentityModel[] iMoneyOrderIdentityList = new IdentityModel[this.identityLen];
        IdentityModel.HPair[] HIdentityList = new IdentityModel.HPair[this.identityLen];
        for (int i = 0; i < this.identityLen; i++) {
            BigInteger xl = generateRandomNumber(upperLimit);
            BigInteger xr = xl.xor(this.userId);

            IdentityModel.XPair xPair = new IdentityModel.XPair(xl, xr);
            IdentityModel.HPair hPair = new IdentityModel.HPair(new BigInteger(DigestUtils.sha256(xl.toByteArray())),
                    new BigInteger(DigestUtils.sha256(xr.toByteArray())));

            IdentityModel identityModel = new IdentityModel(xPair, hPair);
            iMoneyOrderIdentityList[i] = identityModel;
            HIdentityList[i] = identityModel.gethPair();
        }

        MoneyOrderModel moneyOrderModel = new MoneyOrderModel(
                new BigInteger(this.amount),
                generateRandomNumber(upperLimit) + "",
                HIdentityList
        );

        return new OrderCreationResult(moneyOrderModel, iMoneyOrderIdentityList);
    }

    private BigInteger generateRandomNumber(BigInteger upperLimit) {
        return generateRandomNumber(BigInteger.ZERO, upperLimit);
    }

    private BigInteger generateRandomNumber(BigInteger lowerLimit, BigInteger upperLimit) {
        Random randomSource = new Random();
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(upperLimit.bitLength(), randomSource);
        } while (randomNumber.compareTo(lowerLimit) < 0
                || randomNumber.compareTo(upperLimit) > 0); // continue until randomNumber is bigger than lowerLimit and smaller than upperLimit

        return randomNumber;
    }


    public static byte[] serialize(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] result = null;

        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            result = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Object result = null;

        try {
            in = new ObjectInputStream(bis);
            result = in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
