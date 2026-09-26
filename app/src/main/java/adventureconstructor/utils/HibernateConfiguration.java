package adventureconstructor.utils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public class HibernateConfiguration {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static ApplicationConfigReader configReader;

    private static String lastUsedConnectionURL;

    private static final String DB_URL_PREFIX = "jdbc:";
    private static final String DIALECT_NAME = "nail.gun.configurations.SQLiteDialect";
    private static final String PERSISTENCE_PROVIDER_NAME = "org.hibernate.jpa.HibernatePersistenceProvider";
    private static final String PERSISTENCE_UNIT_NAME = "NailGun";
    private static final String SQLITE_DB_TYPE = "sqlite";

    private static final String DEFAULT_DB_PARENT_FOLDER_NAME = "user.dir";

    private static final String DB_URL_ADDRESS_SEPARATOR = "://";

    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String EMPTY = "";

    private HibernateConfiguration() {}

    private static List<Class> entities = new ArrayList<Class>() {};

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static void shutdown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    public static void build(ApplicationConfigReader inputConfigReader) {
        configReader = inputConfigReader;

        lastUsedConnectionURL = buildDatabaseUrl(inputConfigReader);

        entityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(
                        archiverPersistenceUnitInfo(inputConfigReader), new HashMap<String, Object>() {
                            {
                                put(AvailableSettings.DRIVER, configReader.getDbDriver());
                                put(AvailableSettings.URL, lastUsedConnectionURL);

                                put(AvailableSettings.DIALECT, CustomSQLiteDialect.class);
                                put(AvailableSettings.SHOW_SQL, false);
                                put(AvailableSettings.QUERY_STARTUP_CHECKING, false);

                                put(AvailableSettings.USER, configReader.getDbUser());
                                put(AvailableSettings.PASS, configReader.getDbPassword());

                                put(AvailableSettings.GENERATE_STATISTICS, false);
                                put(AvailableSettings.USE_REFLECTION_OPTIMIZER, false);
                                put(AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
                                put(AvailableSettings.USE_QUERY_CACHE, false);
                                put(AvailableSettings.USE_STRUCTURED_CACHE, false);
                                put(AvailableSettings.STATEMENT_BATCH_SIZE, 20);

                                put(AvailableSettings.HBM2DDL_AUTO, configReader.getDbUsingType());
                            }
                        });

        entityManager = entityManagerFactory.createEntityManager();
    }

    private static String buildDatabaseUrl(ApplicationConfigReader inputConfigReader) {
        String databaseURL = EMPTY;

        String dbAddress = configReader.getDbAddress();

        if (dbAddress.startsWith(DOT)) {
            File databaseDir = new File(System.getProperty(DEFAULT_DB_PARENT_FOLDER_NAME));

            dbAddress = Paths.get(databaseDir.getAbsolutePath(), dbAddress.substring(2))
                    .toString();
        }

        databaseURL += DB_URL_PREFIX;
        databaseURL += inputConfigReader.getDbType();
        databaseURL += DB_URL_ADDRESS_SEPARATOR;
        databaseURL += dbAddress;

        if (!configReader.getDbType().equals(SQLITE_DB_TYPE)) {
            databaseURL += configReader.getDbPort();
            databaseURL += SLASH;
            databaseURL += configReader.getDbName();
        }

        return databaseURL;
    }

    private static PersistenceUnitInfo archiverPersistenceUnitInfo(ApplicationConfigReader inputConfigReader) {
        return new HibernatePersistenceUnitInfo(inputConfigReader);
    }

    private static Properties hibernateProperties(ApplicationConfigReader inputConfigReader) {
        final Properties properties = new Properties();

        properties.put(AvailableSettings.HBM2DDL_AUTO, configReader.getDbUsingType());
        properties.put(AvailableSettings.SHOW_SQL, true);
        properties.put(AvailableSettings.DRIVER, configReader.getDbDriver());
        properties.put(AvailableSettings.DIALECT, DIALECT_NAME);
        properties.put(AvailableSettings.DATASOURCE, dataSource(inputConfigReader));

        return properties;
    }

    private static DataSource dataSource(ApplicationConfigReader inputConfigReader) {
        final SQLiteDataSource dataSource = new SQLiteDataSource();

        dataSource.setUrl(buildDatabaseUrl(inputConfigReader));
        dataSource.setJournalMode(SQLiteConfig.JournalMode.WAL.getValue());
        dataSource.setLockingMode(SQLiteConfig.LockingMode.NORMAL.getValue());

        return dataSource;
    }

    private static List<String> entityClassNames() {
        return entities.stream().map(Class::getName).collect(Collectors.toList());
    }

    public static void addEntity(Class entityClass) {
        entities.add(entityClass);
    }

    public static String getLastUsedConnectionURL() {
        return lastUsedConnectionURL;
    }

    public static String getDbUrlPrefix() {
        return DB_URL_PREFIX + SQLITE_DB_TYPE + DB_URL_ADDRESS_SEPARATOR;
    }

    private static class HibernatePersistenceUnitInfo implements PersistenceUnitInfo {
        private ApplicationConfigReader configReader;

        public HibernatePersistenceUnitInfo(ApplicationConfigReader inputConfigReader) {
            configReader = inputConfigReader;
        }

        @Override
        public String getPersistenceUnitName() {
            return PERSISTENCE_UNIT_NAME;
        }

        @Override
        public String getPersistenceProviderClassName() {
            return PERSISTENCE_PROVIDER_NAME;
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return PersistenceUnitTransactionType.RESOURCE_LOCAL;
        }

        @Override
        public DataSource getJtaDataSource() {
            return null;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return null;
        }

        @Override
        public List<String> getMappingFileNames() {
            return Collections.emptyList();
        }

        @Override
        public List<URL> getJarFileUrls() {
            try {
                return Collections.list(this.getClass().getClassLoader().getResources(""));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return null;
        }

        @Override
        public List<String> getManagedClassNames() {
            return entityClassNames();
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return false;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return null;
        }

        @Override
        public ValidationMode getValidationMode() {
            return null;
        }

        @Override
        public Properties getProperties() {
            return hibernateProperties(configReader);
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void addTransformer(ClassTransformer transformer) {}

        @Override
        public ClassLoader getNewTempClassLoader() {
            return null;
        }
    }
}
