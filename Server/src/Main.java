import data.Repository;
import inject.Injection;
/*
ReadMe Before Run
    Gson과 mySQL 에서 지원하는 connector 드라이버 모듈을 참조해야 합니다.
    connector 는 5.8 이후버젼을 쓰서야 에러가 안날것 같습니다.
 */
public class Main {
    public static void main(String[] args) {
        Repository repository = Injection.getInstance().injectRepository();
        repository.connectClient();
    }
}
