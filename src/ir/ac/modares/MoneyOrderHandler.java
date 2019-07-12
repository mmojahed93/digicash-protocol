package ir.ac.modares;

import ir.ac.modares.model.IdentityModel;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.Random;

public class MoneyOrderHandler {


    private final BigInteger upperLimit = new BigInteger("2455155546008943817740293915197451784769108058161191238065");

    private MoneyOrderModel[] moneyOrderList = new MoneyOrderModel[100];

    private BigInteger userId;
    private String amount;
    private int identityLen=100;
    private IdentityModel[] identityList;


    public MoneyOrderHandler(String amount, BigInteger userId) {
        this.amount = amount;
        this.userId = userId;
        this.identityList = new IdentityModel[identityLen];
    }

    public MoneyOrderModel[] createMoneyOrderList() {
        for (int i = 0; i < moneyOrderList.length; i++) {
            moneyOrderList[i] = createMoneyOrder();
        }

        return moneyOrderList;
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
}
