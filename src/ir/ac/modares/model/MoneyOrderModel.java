package ir.ac.modares.model;

import java.math.BigInteger;

public class MoneyOrderModel {
    BigInteger amount;
    String serialId;
    IdentityModel[] identityList;

    public MoneyOrderModel(BigInteger amount, String serialId, IdentityModel[] identityList) {
        this.amount = amount;
        this.serialId = serialId;
        this.identityList = identityList;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public IdentityModel[] getIdentityList() {
        return identityList;
    }

    public void setIdentityList(IdentityModel[] identityList) {
        this.identityList = identityList;
    }
}
