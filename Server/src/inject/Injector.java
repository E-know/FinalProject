package inject;

import data.Repository;
import data.datasource.local.DataBase;
import data.datasource.remote.RemoteDataSource;

/**
 * Injection class의 인터페이스
 *
 * @see Injection
 */
public interface Injector {
    Repository injectRepository();

    DataBase injectLocalDataSource();

    RemoteDataSource injectRemoteDataSource();


}
