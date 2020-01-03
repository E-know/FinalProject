package data.datasource.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysql.cj.protocol.Resultset;

import java.sql.*;
/*
    Database Info
        Schemas Name : javadb
            Table Name : Product    /   Ingredient
 */
/*
    Table Info
        Table Name : Product
    =====================================================
    | PrCode(int) | PrName(char(40)) | PrPrice(int) | PrNumber(int) | PrIngredient(char(40)) |
    =====================================================
        *PrCode - 상품 코드(4자리 숫자 1001~)
        *PrIngredient - 상품에 들어가는 재료목록
        ex) 101-30/103-50/105-35
            101번 재료 30g / 103번 재료 50g / 105번 재료 35g

        Table Name : Ingredient
    =====================================================
    | IgCode(int) | IgName(char(40)) | IgNumber(int) | IgPrice(int) | IgProduct(char(40)) |
    ====================================================
        *IgCode : 재료 코드(3자리 숫자 101~)
        *IgProduct : - 재료가 들어가는 상품 코드 목록
        ex) 1001-40/1005-30/1006-4
            1001번 상품에 해당 재료 40g / 1005번 상품에 해당 재료 30g / 1006번 재료에 해당 재료 4g

 */

public class DataBaseImpl implements DataBase {
    private static DataBaseImpl Instance = null;
    private String jdbcUrl;
    private Connection conn;
    private PreparedStatement pstmt;
    private String ID, Password;

    public static DataBaseImpl getInstance(String ID, String Password) {
        if (Instance == null) {
            Instance = new DataBaseImpl(ID, Password);
        }
        return Instance;
    }

    public DataBaseImpl(String ID, String Password) {
        jdbcUrl = "jdbc:mysql://localhost/javadb?serverTimezone=UTC";
        this.ID = ID;
        this.Password = Password;
    }
    /*
    이름 : connectDB
    설명 : Mysql에 연결해주는 함수 실패시 false를 반환한다.
     */
    private boolean connectDB() {
        boolean result = false;
        try {
            conn = DriverManager.getConnection(jdbcUrl, ID, Password);
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private void closeDB() {
        try {
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*
    이름 : registerProduct
    파라미터 : JsonObject : 제품 데이터 [ PrCode PrName PrPrice PrNumber PrIngredient]
    설명 : 제품을 JsonObject로 가져와 데이터 베이스 product Table 에 저장한다.
     */
    @Override
    public void registerProduct(JsonObject data_Product) {
        if (!connectDB()) {
            System.out.println("Connect DB is fail in DataBaseImpl at registerProduct");
            System.out.println("Registration Product is fail");
        }
        String sql = "insert into product values(?,?,?,?,?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, data_Product.get("PrCode").getAsInt());
            pstmt.setString(2, data_Product.get("PrName").getAsString());
            pstmt.setInt(3, data_Product.get("PrPrice").getAsInt());
            pstmt.setInt(4, data_Product.get("PrNumber").getAsInt());
            pstmt.setString(5, data_Product.get("PrIngredient").getAsString());
            pstmt.execute();
            System.out.println("DB에 [Product]가 저장되었습니다.");
        } catch (SQLException e) {
            System.out.println("DB 저장 실패[Product]");
            e.printStackTrace();
        }
    }//registerProduct

    /*
    이름 : registerIngredient
    파라미터 : JsonObject : 제품 데이터 [ IgCode IgName IgPrice IgNumber IgProduct]
    설명 : 제품을 JsonObject로 가져와 데이터 베이스에 ingredient Table에  등록한다.
     */

    @Override
    public void registerIngredient(JsonObject data_Ingredient) {
        if (!connectDB()) {
            System.out.println("Connect DB is fail in DataBaseImpl at registerIngredient");
            System.out.println("Registration Ingredient fail");
        }
        String sql = "insert into ingredient values(?,?,?,?,?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, data_Ingredient.get("IgCode").getAsInt());
            pstmt.setString(2, data_Ingredient.get("IgName").getAsString());
            pstmt.setInt(3, data_Ingredient.get("IgNumber").getAsInt());
            pstmt.setInt(4, data_Ingredient.get("IgPrice").getAsInt());
            pstmt.setString(5, data_Ingredient.get("IgProduct").getAsString());
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//registerIngredient

    /*
    이름 : updateIngredient
    파라미터 :  JsonArray - 최산화 할 JsonArray [ IgCode , IgNumber] 로 이루어진 배열
               String - plus or minus
    설명 : 데이터를 받아서 DB속 ingredient Table에 최신화 시켜준다.
    */
    @Override
    public boolean updateIngredient(JsonArray toupdate, String sign) {
        boolean result = true;
        connectDB();
        for (JsonElement elem : toupdate) {
            JsonObject obj = elem.getAsJsonObject();
            String sql = "update ingredient set IgNumber = IgNumber " + sign + " " + obj.get("IgNumber") + " where IgCode = " + obj.get("IgCode");
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
        }
        closeDB();
        return result;
    }
    /*
    이름 : updateIngredient
    파라미터 :  JsonArray - 최산화 할 JsonArray [ IgCode , IgNumber] 로 이루어진 배열
               int  - 몇 배수로 삭제 할지 정한다.
    설명 : 데이터를 받아서 DB속 ingredient Table에 최신화 시켜준다.
            장바구니에서 물품을 제거 했을 시 connectDB의 횟수를 줄이고자 파라미터를 다르게 끔 설정하였다.
    */
    @Override
    public boolean updateIngredient(JsonArray toupdate, int num) {
        boolean result = true;
        connectDB();
        for (JsonElement elem : toupdate) {
            JsonObject obj = elem.getAsJsonObject();
            String sql = "update ingredient set IgNumber = IgNumber + " + (obj.get("IgNumber").getAsInt() * num) + " where IgCode = " + obj.get("IgCode");
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
        }
        closeDB();
        return result;
    }
    /*
    이름 : getProductArray
    설명 : DB속 product Table의 모든 값을 JsonArray 형태로 반환해준다.
           [PrCode PrName Prprice PrNumber PrIngredient]
    */
    @Override
    public JsonArray getProductArray() {
        if (!connectDB()) {
            System.out.println("Connect DB is fail in DataBaseImpl at getProductArray");
            return null;
        }
        String sql = "select * from product";
        JsonArray result = new JsonArray();
        JsonArray ingredient = getIngredientArrayWithoutConnectServer();
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            JsonObject input;
            while (rs.next()) {
                input = new JsonObject();
                input.addProperty("PrCode", rs.getInt("PrCode"));
                input.addProperty("PrName", rs.getString("PrName"));
                input.addProperty("PrPrice", rs.getInt("PrPrice"));
                input.addProperty("PrNumber", rs.getInt("PrNumber"));
                input.addProperty("PrIngredient", rs.getString("PrIngredient"));
                if (new DataTransform(ID, Password).isSell(input, ingredient))
                    input.addProperty("IsSell", true);
                else
                    input.addProperty("IsSell", false);
                result.add(input);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB();
        return result;
    }//getProductArray

    /*
    이름 : getIngredientArray
    설명 : DB속 igredient Table의 모든 데이터를 반환해준다.
            [IgCode IgName IgPrice IgNumber IgProduct]
    */
    @Override
    public JsonArray getIngredientArray() {
        if (!connectDB()) {
            System.out.println("Connect DB is fail in DataBaseImpl at getIngredientArray");
            return null;
        }
        String sql = "select * from ingredient";
        JsonArray result = new JsonArray();
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            JsonObject input;
            while (rs.next()) {
                input = new JsonObject();
                input.addProperty("IgCode", rs.getInt("IgCode"));
                input.addProperty("IgName", rs.getString("IgName"));
                input.addProperty("IgNumber", rs.getInt("IgNumber"));
                input.addProperty("IgPrice", rs.getInt("IgPrice"));
                input.addProperty("IgProduct", rs.getString("IgProduct"));
                result.add(input);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB();
        return result;
    }//getIngredientArray

    /*
    이름 : getIngredientArrayWithoutConnectServer
    설명 : 서버가 연결되어있는 상태에서 불러오는 함수로 ingredient의 Table 자료를 반환한다.
    */
    private JsonArray getIngredientArrayWithoutConnectServer() {
        String sql = "select * from ingredient";
        JsonArray result = new JsonArray();
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            JsonObject input;
            while (rs.next()) {
                input = new JsonObject();
                input.addProperty("IgCode", rs.getInt("IgCode"));
                input.addProperty("IgName", rs.getString("IgName"));
                input.addProperty("IgNumber", rs.getInt("IgNumber"));
                input.addProperty("IgPrice", rs.getInt("IgPrice"));
                input.addProperty("IgProduct", rs.getString("IgProduct"));
                result.add(input);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }//getIngredientArray

    /*
    이름 : addIngredient
    파라미터 :  int - IgCode
    설명 : ingredient Table 속 IgCode와 같은 row의 IgNumber의 수를 100 올려준다.
    */
    @Override
    public boolean addIngredient(int IgCode) {
        String sql = "update ingredient set IgNumber = IgNumber + 100 where IgCode = " + IgCode;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        sql = "select * from ingredient where IgCode = " + IgCode;

        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            int price = rs.getInt("IgPrice");

            sql = "update money set Total = Total -" + price;
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
    이름 : getMoney
    설명 : money Tabel 의 모든 데이터를 받아온다.
            [Expense Income Total]
    */
    @Override
    public JsonObject getMoney() {
        String sql = "select * from money";
        JsonObject result = new JsonObject();
        connectDB();
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.addProperty("Expense", rs.getInt("Expense"));
                result.addProperty("Income", rs.getInt("Income"));
                result.addProperty("Total", rs.getInt("Total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB();
        return result;
    }
    /*
    이름 : changeTotal
    파라미터 :  int - 변화량
    설명 : money Table 속 total 의 값을 파라미터 값 만큼 변화시킨다.
    */
    @Override
    public void changeTotal(int total){
        String sql = "update money set Total = Total +" + total;
        connectDB();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "update money set Income = Income + " + total;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB();
    }
    /*
    이름 : changePrNumber
    파라미터 :  PrCode - 변화를 원하는 PrCode
               sing - Plus or Minus
    설명 : product Table 속 PrCode와 일치하는 row의 PrNumber를 1(Plus or Minus) 시킨다.
    */
    @Override
    public boolean changePrNumber(int PrCode,char sign) {
        String sql = "update product set PrNumber = PrNumber " + sign + " 1 where PrCode = " + PrCode;
        connectDB();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            closeDB();
            return false;
        }
        closeDB();
        return true;
    }
}// Class DataBase
