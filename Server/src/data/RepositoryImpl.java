package data;

import com.google.gson.JsonObject;
import data.datasource.local.DataBase;
import data.datasource.local.DataTransform;
import data.datasource.remote.RemoteDataSource;
import data.datasource.remote.callback.ServerCallback;

import java.io.IOException;

/**
 * Remote와 local에 대한 정보를 담고있는 클래스
 *
 * @author 조재영
 */
public class RepositoryImpl implements Repository {
    private static Repository INSTANCE = null;
    private DataBase local;
    private RemoteDataSource remote;

    /**
     * 기본 생성자
     *
     * @param local  DataBase 연결을 위한 LocalDataSource
     * @param remote Client와 연결을 위한 RemoteDataSource
     */
    private RepositoryImpl(DataBase local, RemoteDataSource remote) {
        this.local = local;
        this.remote = remote;
    }

    public static Repository getInstance(DataBase local, RemoteDataSource remote) {
        if (INSTANCE == null) INSTANCE = new RepositoryImpl(local, remote);
        return INSTANCE;
    }

    /**
     * 서버를 열고 대기하는 함수
     * 서버의 controller 역할을 하고있는 부분
     */
    @Override
    public void connectClient() {
        remote.openServer(new ServerCallback() {
            @Override
            public void login() {
                remote.sendData(local.getProductArray().toString());
            }

            @Override
            public void selectItem(String select) {
                System.out.println(select);
                (new DataTransform("java7", "java8")).buyProduct(Integer.parseInt(select));
                remote.sendData(local.getProductArray().toString());
            }

            @Override
            public void minusItem(String select) {
                System.out.println(select);
                new DataTransform("java7", "java8").cancelProduct(Integer.parseInt(select));
                remote.sendData(local.getProductArray().toString());
            }

            @Override
            public void exitCallback(String select, int count) {
                System.out.println(select);
                new DataTransform("java7", "java8").removeBasket(Integer.parseInt(select), count);
                remote.sendData(local.getProductArray().toString());
            }

            @Override
            public void buy(int total) {
                local.changeTotal(total);
            }

            @Override
            public void ingredient() {
                remote.sendData(local.getIngredientArray().toString());
            }

            @Override
            public void total() {
                JsonObject obj = new JsonObject();
                obj.addProperty("total",local.getMoney().get("Income").getAsInt());
                String str = local.getProductArray().toString();
                obj.addProperty("array",str);
                remote.sendData(obj.toString());
            }

            @Override
            public void error() {
                System.out.println("로그인 실패!!!");
            }
        });
    }

    @Override
    public void broadCastClients(String data) {
        remote.sendData(data);
    }


    @Override
    public void closeServer() {
        try {
            remote.closeServer();
            // Remove DB close
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
