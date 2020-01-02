package inject;

import data.Repository;
import data.RepositoryImpl;
import data.datasource.local.DataBase;
import data.datasource.local.DataBaseImpl;
import data.datasource.remote.RemoteDataSource;
import data.datasource.remote.RemoteDataSourceImpl;
import data.datasource.remote.network.Server;

public class Injection implements Injector {
    private static Injection INSTANCE = null;

    private DataBase local = null;
    private RemoteDataSource remote = null;
    private Repository repository = null;

    private Injection() {

    }

    public static Injection getInstance() {
        if (INSTANCE == null) INSTANCE = new Injection();
        return INSTANCE;
    }

    @Override
    public Repository injectRepository() {
        if (repository == null)
            repository = RepositoryImpl.getInstance(injectLocalDataSource(), injectRemoteDataSource());
        return repository;
    }

    @Override
    public DataBase injectLocalDataSource() {
        if (local == null) local = DataBaseImpl.getInstance("java7","java8");
        return local;
    }

    @Override
    public RemoteDataSource injectRemoteDataSource() {
        if (remote == null) remote = RemoteDataSourceImpl.getInstance(new Server());
        return remote;
    }
}
