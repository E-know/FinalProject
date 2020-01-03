package data;

/**
 * 데이터의 분기를 위한 Repository interface
 * 기존 non_blocking 으로 이루어져있었고, 다중 클라이언트를 지원할 예정이었기 때문에 여러 함수들이 사용되지 않고 남아있다.
 *
 * @author 조재영
 */
public interface Repository {
    void connectClient();

    void broadCastClients(String data);


    void closeServer();
}
