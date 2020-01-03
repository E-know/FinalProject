package data.datasource.remote.callback;

/**
 * 서버에서 받은 데이터들을 분기해주는 콜백
 */
public interface ServerCallback {
    void login();

    void selectItem(String select);

    void minusItem(String select);

    void exitCallback(String select, int count);

    void buy(int total);

    void ingredient();

    void total();

    void error();
}
