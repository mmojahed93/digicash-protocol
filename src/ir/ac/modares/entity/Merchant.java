package ir.ac.modares.entity;

import ir.ac.modares.MoneyOrderHandler;
import ir.ac.modares.model.MoneyOrderModel;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.Random;

public class Merchant {

    private BigInteger signedMoneyOrder;
    private MoneyOrderModel moneyOrderModel;

    private String generatedRandomBits;

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

        generatedRandomBits = randomBits.toString();
        return generatedRandomBits;
    }

    private boolean checkHalves(BigInteger[] halves) throws Exception {
        if (halves == null) {
            throw new Exception("Halves is null!");
        }

        if (halves.length != moneyOrderModel.getIdentityList().length) {
            throw new Exception("Halves length not matched!");
        }

        for (int i = 0; i < generatedRandomBits.length(); i++) {
            int b = Integer.parseInt(String.valueOf(generatedRandomBits.charAt(i)));

            BigInteger hashedHalve = new BigInteger(DigestUtils.sha256(halves[i].toByteArray()));
            if (b == 0) {
                if (!this.moneyOrderModel.getIdentityList()[i].getHl().equals(hashedHalve)) {
                    return false;
                }

            } else if (b == 1) {
                if (!this.moneyOrderModel.getIdentityList()[i].getHr().equals(hashedHalve)) {
                    return false;
                }

            } else {
                return false;

            }
        }

        return true;
    }


    public boolean checkMoneyOrder(BigInteger[] halves) throws Exception {
        boolean isSignatureValid = checkMoneyOrderSignature();

        if (!isSignatureValid) {
            throw new Exception("Signature is not valid!");
        }

        boolean isIdentityListValid = checkHalves(halves);
        if (!isIdentityListValid) {
            throw new Exception("Identity list is not valid!");
        }

        return true;
    }

    public void spendMoneyOrder(){
        Bank bank = new Bank();

    }

}