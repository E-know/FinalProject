package data.datasource.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.dao.IngredientModel;

import java.util.ArrayList;

public class DataTransform {
    private String ID;
    private String Password;
    private ArrayList<IngredientModel> ingredient;
    private DataBase DB;
/*
Constructor
파라미터 : ID / Password of MySql [ ID : java7 PW java8 ]
 */
    public DataTransform(String ID, String Password) {
        this.ID = ID;
        this.Password = Password;
        ingredient = new ArrayList<IngredientModel>();
        DB = DataBaseImpl.getInstance(ID, Password);
    }
    /*
    이름 : returnIngredient
    Parameter : JsonObject - product Json Data
    Explain : Json 데이터 중 PrIngredient를 JsonObject에 IgCode 와 IgNumber 에 맞게 끔 나눈후 Array 형태로 나눠 반환한다.
            자세한 설명은 DataBaseImpl 주석 참조
     */
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

    /*
    Name : returnProductObjcet
    Parameter : int - PrCode | JsonArray productArray
    Explain : JsonArray 중 PrCode가 일치하는 JsonObject를 반환한다.
     */
    public JsonObject returnProductObject(int PrCode, JsonArray productArray) {
        for (JsonElement obj : productArray) {
            if (obj.getAsJsonObject().get("PrCode").getAsInt() == PrCode) {
                return obj.getAsJsonObject();
            }
        }
        return null;
    }

    /*
    Name buyProduct
    Parameter : PrCode(int)
    Explain : PrCode를 받아와서 PrCode가 일치한 DB에 PrNumber +1 해준 후 PrIngredient 를 갖고와 분석해서 맞는 Ingredient 의 수를 깍아준다.
                [판매 개수를 +1 해주고 재료를 조리법에 맞게끔 내려준다.]
     */
    public boolean buyProduct(int PrCode) {
        JsonArray productArr = DB.getProductArray();
        JsonArray needArr = returnIngredient(returnProductObject(PrCode, productArr));
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
        DB.changePrNumber(PrCode,'+');
        return true;
    }
    /*
    Name cancelProduct
    Parameter PrCode
    Explain : 구매하려고 했던 재료 값들을 장바구니에서 제회 혹은 개수를 줄일 때 그만큼의 재료 값을 다시 복구시킨다.
     */
    public boolean cancelProduct(int PrCode) {
        JsonArray productArr = DB.getProductArray();
        JsonArray needArr = returnIngredient(returnProductObject(PrCode, productArr));
        if (needArr.isJsonNull()) return false;
        if (DB.updateIngredient(needArr, "+")) {
            System.out.println("취소에 성공했습니다.");
            DB.changePrNumber(PrCode,'-');
        }
        else
            System.out.println("취소에 실패했습니다.");
        return true;
    }
    /*
    Name removeBasket
    Parameter PrCode(int) num(int)
    Explain cancleProduct의 로직을 n번 실행시킨 것과 같은 효과를 볼 수 있는데 connectDB의 중복을 줄이기 위해서 만든 함수
     */
    public boolean removeBasket(int PrCode, int num) {
        JsonArray productArr = DB.getProductArray();
        JsonArray needArr = returnIngredient(returnProductObject(PrCode, productArr));
        if (needArr.isJsonNull()) return false;
        if (DB.updateIngredient(needArr, num)) {
            System.out.println("장바구니에 취소에서 재료 값 변동되었습니다");
            return true;
        } else {
            System.out.println("장바구니에서 재료 변동이 실패했습니다.");
            return false;
        }
    }

    /*
    Name isSell
    Paramerte JsonObject product | JsonArray ingredient
    Explain 재료의 개수와 product가 생성되기 위한 재료의 개수를 비교해 구매가 가능하면 true 불가능하면 false 를 반환해주는 함수
     */
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

    /*
    Name buyIngredient
    Parameter IgCode(int)
    Explain 재료 구매하는 함수로 DataBase의 addIngredient를 호출한다.
    [추후에는 재료별로 구매하는 개수가 다르게 할려고 했으나 쓰일 일은 없었다고 한다 ㅠ]
    [프로젝트 축소]
     */
    public void buyIngredient(int IgCode) {
        if (DB.addIngredient(IgCode))
            System.out.println("재료 구매에 성공했습니다.");
        else
            System.out.println("재료 구매에 실패했습니다.");
    }



}
