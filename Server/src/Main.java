import data.Repository;
import inject.Injection;

public class Main {
    public static void main(String[] args) {
        Repository repository = Injection.getInstance().injectRepository();
        repository.connectClient();
    }
}
