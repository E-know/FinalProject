package data.datasource.remote.callback;

public interface ServerCallback {
    void login();

    void selectItem(String select);

    void minusItem(String select);

    void exitCallback(String select, int count);

    void error();
}
