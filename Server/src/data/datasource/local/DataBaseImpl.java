package data.datasource.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.xml.crypto.Data;
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
    // TODO: 2020-01-01 마지막 IgCode PrCode 갖고 있어야 하지 않을까?

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

    public boolean connectDB() {
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


    @Override
    public JsonArray getProductArray() {
        if (!connectDB()) {
            System.out.println("Connect DB is fail in DataBaseImpl at getProductArray");
            return null;
        }
        String sql = "select * from product";
        JsonArray result = new JsonArray();
        JsonArray ingredient = getIngredientArray2();
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

    private JsonArray getIngredientArray2() {
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

    public boolean addIngredient(int IgCode) {
        // TODO: 2020-01-01 Code 추가하기
        return true;
    }

    public int getTotal(){
        int total = 0;
        String sql = "select * from money";
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                total = rs.getInt("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public JsonObject getMoney(){
        String sql = "select * from money";
        JsonObject result = new JsonObject();
        try {
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                result.addProperty("Expense",rs.getInt("Expense"));
                result.addProperty("Income",rs.getInt("Income"));
                result.addProperty("Total",rs.getInt("Total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }//getMoney

}// Class DataBase
