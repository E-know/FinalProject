package inject;

import data.Repository;
import data.datasource.local.DataBase;
import data.datasource.remote.RemoteDataSource;

public interface Injector {
    Repository injectRepository();

    DataBase injectLocalDataSource();

    RemoteDataSource injectRemoteDataSource();


}
