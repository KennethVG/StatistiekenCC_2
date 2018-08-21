package be.somedi.statistiekencc.model;

public enum Typiste {

    LAMOTTE("4876"), VERMEULEN("952"), CROONENBORGHS("5382"), TSIJEN("9988"), VANONCKELEN("5383"), VERSTAPPEN("10465"), CALANT("19223"), GYZEMANS("19559"), DE_CEULAER("20018"), VAN_GENECHTEN("19911");

    private final String id;

    Typiste(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}