package data.datasource.remote;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.datasource.remote.callback.ServerCallback;
import data.datasource.remote.network.Server;

import java.io.IOException;

/**
 * 서버와 연결하고 데이터를 조작하는 클래스
 *
 * @author 조재영
 */
public class RemoteDataSourceImpl implements RemoteDataSource {
    private static RemoteDataSource INSTANCE = null;
    private Server server;
    private JsonParser parser = new JsonParser();

    private RemoteDataSourceImpl(Server server) {
        this.server = server;
    }

    public static RemoteDataSource getInstance(final Server server) {
        if (INSTANCE == null) INSTANCE = new RemoteDataSourceImpl(server);
        return INSTANCE;
    }

    /**
     * 데이터를 받아 변환, 정리 후 상위 레이어로 돌려준다.
     *
     * @param callback
     */
    @Override
    public void openServer(ServerCallback callback) {
        server.startServer();
        server.ReceiveData(data -> {
            JsonObject object = parser.parse(data).getAsJsonObject();
            System.out.println(object.toString());
            if (object.get("login") != null) {
                callback.login();
            } else if (object.get("select") != null) {
                callback.selectItem(object.get("select").toString());
            } else if (object.get("minus") != null) {
                callback.minusItem(object.get("minus").toString());
            } else if (object.get("exit") != null) {
                callback.exitCallback(object.get("exit").toString(), object.get("count").getAsInt());
            } else if (object.get("buy") != null) {
                callback.buy(object.get("buy").getAsInt());
            } else if (object.get("ingredient") != null) {
                callback.ingredient();
            } else if (object.get("total") != null) {
                callback.total();
            } else {
                callback.error();
            }

        });

    }

    @Override
    public void sendData(final String data) {
        server.send(data);
    }

    @Override
    public void closeServer() throws IOException {
        server.close();
    }

}
