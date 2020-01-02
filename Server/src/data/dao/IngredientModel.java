package data.dao;

public class IngredientModel {
    public int IgCode;
    public String IgName;
    public int IgNumber;
    public int IgPrice;
    public String IgProduct;

    public IngredientModel(int IgCode, String IgName, int IgNumber, int IgPrice,String IgProduct) {
        this.IgCode = IgCode;
        this.IgName = IgName;
        this.IgNumber = IgNumber;
        this.IgPrice = IgPrice;
        this.IgProduct = IgProduct;
    }
}
