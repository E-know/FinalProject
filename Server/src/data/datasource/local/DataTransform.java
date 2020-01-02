package data.datasource.local;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.dao.IngredientModel;

import java.util.ArrayList;

public class DataTransform {
    private String ID;
    private String Password;
    private ArrayList<IngredientModel> ingredient;
    private DataBaseImpl DB;

    public DataTransform(String ID, String Password) {
        this.ID = ID;
        this.Password = Password;
        ingredient = new ArrayList<IngredientModel>();
        DB = DataBaseImpl.getInstance(ID, Password);
    }

    public JsonArray returnIngredient(JsonObject product) {
        String str = product.get("PrIngredient").getAsString();
        JsonArray result = new JsonArray();
        int IgCode;
        String ig[] = str.split("/");
        for (String elem : ig) {
            String s[] = elem.split("-");
            JsonObject obj = new JsonObject();
            obj.addProperty("IgCode", Integer.parseInt(s[0]));
            obj.addProperty("IgNumber", Integer.parseInt(s[1]));
            result.add(obj);
        }
        return result;
    }

    public JsonObject returnProductObject(int PrCode) {
        JsonArray arr = DB.getProductArray();
        for (JsonElement obj : arr) {
            if (obj.getAsJsonObject().get("PrCode").getAsInt() == PrCode) {
                return obj.getAsJsonObject();
            }
        }
        return null;
    }

    public boolean buyProduct(int PrCode) {
        JsonArray needArr = returnIngredient(returnProductObject(PrCode));
        if (needArr.isJsonNull()) return false;

        JsonArray ingredientArr = DB.getIngredientArray();
        JsonArray toUpdate = new JsonArray();
        for (JsonElement elem : ingredientArr) {
            for (JsonElement need : needArr) {
                if (elem.getAsJsonObject().get("IgCode").getAsInt() == need.getAsJsonObject().get("IgCode").getAsInt()) {
                    if (elem.getAsJsonObject().get("IgNumber").getAsInt() >= need.getAsJsonObject().get("IgNumber").getAsInt()) {
                        toUpdate.add(need);
                    } else {
                        System.out.println("재료가 부족한 게 있습니다.");
                        return false;
                    }
                }
            }
        }
        DB.updateIngredient(toUpdate, "-");
        return true;
    }

    public boolean cancelProduct(int PrCode) {
        JsonArray needArr = returnIngredient(returnProductObject(PrCode));
        if (needArr.isJsonNull()) return false;
        if (DB.updateIngredient(needArr, "+"))
            System.out.println("취소에 성공했습니다.");
        else
            System.out.println("취소에 실패했습니다.");
        return true;
    }

    public boolean removeBasket(int PrCode,int num){
        JsonArray needArr = returnIngredient(returnProductObject(PrCode));
        if(needArr.isJsonNull()) return false;
        if(DB.updateIngredient(needArr,num)){
            System.out.println("장바구니에 취소에서 재료 값 변동되었습니다");
            return true;
        }
        else {
            System.out.println("장바구니에서 재료 변동이 실패했습니다.");
            return false;
        }
    }

    public boolean isSell(JsonObject product, JsonArray ingredient) {
        JsonArray supply = returnIngredient(product);

        for (JsonElement elem : ingredient) {
            JsonObject obj = elem.getAsJsonObject();
            for (JsonElement needelem : supply) {
                JsonObject need = needelem.getAsJsonObject();
                if (obj.get("IgCode").getAsInt() == need.get("IgCode").getAsInt()) {
                    if (obj.get("IgNumber").getAsInt() < need.get("IgNumber").getAsInt())
                        return false;
                }
            }
        }
        return true;
    }

    public JsonArray updateIsSell() {
        JsonArray productArr = DB.getProductArray();
        JsonArray ingredientArr = DB.getIngredientArray();
        JsonArray result = new JsonArray();
        for (JsonElement elem : productArr) {
            JsonObject obj = elem.getAsJsonObject();
            boolean sell = isSell(obj, ingredientArr);
            obj.addProperty("IsSell", sell);
            result.add(obj);
        }
        return result;
    }

    public void buyIngredient(int IgCode) {
        if (DB.addIngredient(IgCode))
            System.out.println("재료 구매에 성공했습니다.");
        else
            System.out.println("재료 구매에 실패했습니다.");
    }

}
