package ir.ac.modares.model;

import java.io.Serializable;
import java.math.BigInteger;

public class MoneyOrderModel implements Serializable {
    private BigInteger amount;
    private String serialId;
    private IdentityModel.HPair[] identityList;


    public MoneyOrderModel(BigInteger amount, String serialId, IdentityModel.HPair[] identityList) {
        this.amount = amount;
        this.serialId = serialId;
        this.identityList = identityList;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public String getSerialId() {
        return serialId;
    }

    public IdentityModel.HPair[] getIdentityList() {
        return identityList;
    }
}
