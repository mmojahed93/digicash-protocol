package ir.ac.modares.model;

import java.math.BigInteger;

public class IdentityModel {
    public static class XPair {

        private BigInteger xl;
        private BigInteger xr;

        public XPair(BigInteger xl, BigInteger xr) {
            this.xl = xl;
            this.xr = xr;
        }

        public BigInteger getXl() {
            return xl;
        }

        public BigInteger getXr() {
            return xr;
        }
    }

    public static class HPair {
        private BigInteger hl;
        private BigInteger hr;

        public HPair(BigInteger hl, BigInteger hr) {
            this.hl = hl;
            this.hr = hr;
        }

        public BigInteger getHl() {
            return hl;
        }

        public BigInteger getHr() {
            return hr;
        }
    }

    private XPair xPair;
    private HPair hPair;

    public IdentityModel(XPair xPair, HPair hPair) {
        this.xPair = xPair;
        this.hPair = hPair;
    }

    public XPair getxPair() {
        return xPair;
    }

    public HPair gethPair() {
        return hPair;
    }
}
