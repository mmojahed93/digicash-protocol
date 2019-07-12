package ir.ac.modares;

import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MoneyOrderHandler {

    private final BigInteger secretKey = new BigInteger("15454885155544065937824519745029394769131510805818");
    private final BigInteger upperLimit = new BigInteger("2455155546008943817740293915197451784769108058161191238065");

    private final int generalSize = 100;
    private MoneyOrderModel[] moneyOrderList = new MoneyOrderModel[generalSize];
    private BigInteger[] encryptedMoneyOrderList = new BigInteger[generalSize];

    private Set<Integer> checkedMoneyOrderIndexes = new HashSet<>();

    private BigInteger userId;
    private String amount;
    private int identityLen = generalSize;
    private IdentityModel[] identityList;


    public MoneyOrderHandler(String amount, BigInteger userId) {
        this.amount = amount;
        this.userId = userId;
        this.identityList = new IdentityModel[identityLen];
    }

    public MoneyOrderModel[] getMoneyOrderList() {
        return moneyOrderList;
    }

    public BigInteger[] getEncryptedMoneyOrderList() {
        return encryptedMoneyOrderList;
    }

    public void createMoneyOrderList() {
        for (int i = 0; i < moneyOrderList.length; i++) {
            MoneyOrderModel moneyOrderModel = createMoneyOrder();
            moneyOrderList[i] = moneyOrderModel;
            encryptedMoneyOrderList[i] = new BigInteger(serialize(moneyOrderModel)).multiply(secretKey);
        }

    }

    public MoneyOrderModel getDecryptedMoneyOrderModel(int index) throws Exception {
        if (index < 0 || index >= moneyOrderList.length) {
            throw new Exception("Index out of range!");
        }
        if (checkedMoneyOrderIndexes.size() >= moneyOrderList.length - 1) {
            throw new Exception("Request limitation!");
        }
        return moneyOrderList[index];
    }

    private MoneyOrderModel createMoneyOrder() {

        IdentityModel.HPair[] hashIdentityList = new IdentityModel.HPair[this.identityLen];
        for (int i = 0; i < this.identityList.length; i++) {
            BigInteger xl = generateRandomNumber(upperLimit);
            BigInteger xr = xl.xor(this.userId);

            IdentityModel.XPair xPair = new IdentityModel.XPair(xl, xr);
            IdentityModel.HPair hPair = new IdentityModel.HPair(new BigInteger(DigestUtils.sha256(xl.toByteArray())),
                    new BigInteger(DigestUtils.sha256(xr.toByteArray())));

            IdentityModel identityModel = new IdentityModel(xPair, hPair);
            this.identityList[i] = identityModel;
            hashIdentityList[i] = identityModel.gethPair();
        }

        MoneyOrderModel moneyOrderModel = new MoneyOrderModel(
                new BigInteger(this.amount),
                generateRandomNumber(upperLimit) + "",
                hashIdentityList
        );

        return moneyOrderModel;
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
