package org.openspaces.itest.core.space.datasource;

import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class MockManagedDataSource implements ManagedDataSource {

    private boolean initCalled;

    public void init(Properties properties) throws DataSourceException {
        initCalled = true;
    }

    public void shutdown() throws DataSourceException {
    }

    public boolean isInitCalled() {
        return initCalled;
    }
}
