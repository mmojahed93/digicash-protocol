package ir.ac.modares.model;

public class IdentityModel {
    String hl;
    String hr;

    public IdentityModel(String hl, String hr) {
        this.hl = hl;
        this.hr = hr;
    }

    public String getHl() {
        return hl;
    }

    public void setHl(String hl) {
        this.hl = hl;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }
}
