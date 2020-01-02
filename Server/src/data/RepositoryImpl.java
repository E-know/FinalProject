package data;

import data.datasource.local.DataBase;
import data.datasource.local.DataTransform;
import data.datasource.remote.RemoteDataSource;
import data.datasource.remote.callback.ServerCallback;

import java.io.IOException;

public class RepositoryImpl implements Repository {
    private static Repository INSTANCE = null;
    private DataBase local;
    private RemoteDataSource remote;

    private RepositoryImpl(DataBase local, RemoteDataSource remote) {
        this.local = local;
        this.remote = remote;
    }

    public static Repository getInstance(DataBase local, RemoteDataSource remote) {
        if (INSTANCE == null) INSTANCE = new RepositoryImpl(local, remote);
        return INSTANCE;
    }

    @Override
    public void connectClient() {
        remote.openServer(new ServerCallback() {
            @Override
            public void login() {
                //TODO 로컬로 연결후 성공하면
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
                new DataTransform("java7","java8").removeBasket(Integer.parseInt(select),count);
                remote.sendData(local.getProductArray().toString());
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
