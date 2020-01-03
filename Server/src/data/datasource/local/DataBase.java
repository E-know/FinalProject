package data.datasource.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import data.Repository;
import data.datasource.remote.RemoteDataSource;

public interface DataBase {
    /*
    Interface의 대한 Comment는 DataBaseImpl 를 참조하시길 바랍니다.
     */

    void registerProduct(JsonObject data_Product);//registerProduct

    void registerIngredient(JsonObject data_Ingredient);//registerIngredient

    JsonArray getIngredientArray();//getIngredientArray

    JsonArray getProductArray();

    boolean updateIngredient(JsonArray toupdate, String sign);

    boolean updateIngredient(JsonArray toupdate, int num);

    boolean addIngredient(int IgCode);

    JsonObject getMoney();

    void changeTotal(int total);

    boolean changePrNumber(int PrCode,char sign);
}
