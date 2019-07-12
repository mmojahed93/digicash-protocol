package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.Random;

public class Merchant {

    private BigInteger signedMoneyOrder;
    private MoneyOrderModel moneyOrderModel;

    public Merchant(BigInteger signedMoneyOrder, MoneyOrderModel moneyOrderModel) {
        this.signedMoneyOrder = signedMoneyOrder;
        this.moneyOrderModel = moneyOrderModel;
    }

    private boolean checkMoneyOrderSignature() {

        BigInteger designed = signedMoneyOrder.modPow(Bank.publicKeySpec.getPublicExponent(), Bank.publicKeySpec.getModulus());
        BigInteger moneyOrderDigest = new BigInteger(DigestUtils.sha256(MoneyOrderHandler.serialize(moneyOrderModel)));
        return designed.equals(moneyOrderDigest);

    }

    public String getRandomBits() {

        StringBuilder randomBits = new StringBuilder();
        int lenOfRandomBit = moneyOrderModel.getIdentityList().length;

        Random random = new Random();
        for (int i = 0; i < lenOfRandomBit; i++) {
            randomBits.append(random.nextInt(2));
        }

        return randomBits.toString();
    }
}
