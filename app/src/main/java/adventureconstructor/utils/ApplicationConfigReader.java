package adventureconstructor.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class ApplicationConfigReader {
    private static final String PROPERTY_NAME_DB_TYPE = "db-type";
    private static final String PROPERTY_NAME_DB_DRIVER = "db-driver";
    private static final String PROPERTY_NAME_DB_ADDRESS = "db-address";
    private static final String PROPERTY_NAME_DB_PORT = "db-port";
    private static final String PROPERTY_NAME_DB_NAME = "db-name";
    private static final String PROPERTY_NAME_DB_USER = "db-user";
    private static final String PROPERTY_NAME_DB_PASS = "db-password";
    private static final String PROPERTY_NAME_DB_USING_TYPE = "db-using-type";

    private static final String PROPERTY_NAME_LOG_APP = "log-app";
    private static final String PROPERTY_NAME_USE_LAF = "use-laf";
    private static final String PROPERTY_NAME_USE_DARK = "use-dark";

    private static final String CONFIG_FILENAME = "settings.conf";

    private static final String CONFIG_PARENT_FOLDER_NAME = "user.dir";

    private static final String PARSE_EXCEPTION_PREFIX = "Param ";
    private static final String PARSE_EXCEPTION_POSTFIX = " not parsed";

    private String dbType = "sqlite";
    private String dbDriver = "org.sqlite.JDBC";
    private String dbAddress = ". ac.db";
    private String dbPort = "";
    private String dbName = "";
    private String dbUser = "";
    private String dbPassword = "";
    private String dbUsingType = "update";

    private Boolean logApp = false;
    private Boolean useLAF = true;
    private Boolean useDark = true;

    private Path pathToConfig;

    private static ApplicationConfigReader lastConfig;

    public ApplicationConfigReader() throws IOException, FileNotFoundException {
        onReaderCreate(System.getProperty(CONFIG_PARENT_FOLDER_NAME), CONFIG_FILENAME);
    }

    public ApplicationConfigReader(String pathToFile, String filename) throws IOException, FileNotFoundException {
        onReaderCreate(pathToFile, filename);
    }

    private void onReaderCreate(String pathToFile, String filename) throws IOException, FileNotFoundException {
        pathToConfig = Path.of(Path.of(pathToFile, filename).toFile().getAbsolutePath());

        if (!pathToConfig.toFile().exists()) {
            saveConfig();
        }

        FileInputStream configFileInputStream = new FileInputStream(pathToConfig.toString());
        Properties prop = new Properties();
        prop.load(configFileInputStream);

        dbType = getProperty(prop, PROPERTY_NAME_DB_TYPE);
        dbDriver = getProperty(prop, PROPERTY_NAME_DB_DRIVER);
        dbAddress = getProperty(prop, PROPERTY_NAME_DB_ADDRESS);
        dbPort = getProperty(prop, PROPERTY_NAME_DB_PORT);
        dbName = getProperty(prop, PROPERTY_NAME_DB_NAME);
        dbUser = getProperty(prop, PROPERTY_NAME_DB_USER);
        dbPassword = getProperty(prop, PROPERTY_NAME_DB_PASS);
        dbUsingType = getProperty(prop, PROPERTY_NAME_DB_USING_TYPE);

        logApp = Boolean.parseBoolean(getProperty(prop, PROPERTY_NAME_LOG_APP));
        useLAF = Boolean.parseBoolean(getProperty(prop, PROPERTY_NAME_USE_LAF));
        useDark = Boolean.parseBoolean(getProperty(prop, PROPERTY_NAME_USE_DARK));

        lastConfig = this;
    }

    public void saveConfig() throws IOException {
        pathToConfig = Path.of(Path.of(System.getProperty(CONFIG_PARENT_FOLDER_NAME), CONFIG_FILENAME)
                .toFile()
                .getAbsolutePath());

        if (!pathToConfig.toFile().exists() && !pathToConfig.toFile().createNewFile()) {
            throw new FileNotFoundException(Constants.CONFIG_CANT_BE_CREATED_MESSAGE + this.pathToConfig.toString());
        }

        FileOutputStream configFOS = new FileOutputStream(this.pathToConfig.toString());
        Properties properties = new Properties();

        setProperty(properties, PROPERTY_NAME_DB_TYPE, dbType);
        setProperty(properties, PROPERTY_NAME_DB_DRIVER, dbDriver);
        setProperty(properties, PROPERTY_NAME_DB_ADDRESS, dbAddress);
        setProperty(properties, PROPERTY_NAME_DB_PORT, dbPort);
        setProperty(properties, PROPERTY_NAME_DB_NAME, dbName);
        setProperty(properties, PROPERTY_NAME_DB_USER, dbUser);
        setProperty(properties, PROPERTY_NAME_DB_PASS, dbPassword);
        setProperty(properties, PROPERTY_NAME_DB_USING_TYPE, dbUsingType);

        setProperty(properties, PROPERTY_NAME_LOG_APP, logApp.toString());
        setProperty(properties, PROPERTY_NAME_USE_LAF, useLAF.toString());
        setProperty(properties, PROPERTY_NAME_USE_DARK, useDark.toString());

        properties.store(configFOS, Constants.DEFAULT_TEXT);
        configFOS.flush();
        configFOS.close();
    }

    private String getProperty(Properties prop, String propertyName) throws IOException {
        if (prop.getProperty(propertyName) == null) {
            throw new IOException(PARSE_EXCEPTION_PREFIX + propertyName + PARSE_EXCEPTION_POSTFIX);
        }

        return prop.getProperty(propertyName);
    }

    private void setProperty(Properties prop, String propertyName, String propertyValue) throws IOException {
        prop.setProperty(propertyName, propertyValue);
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUsingType() {
        return dbUsingType;
    }

    public Boolean getLogApp() {
        return logApp;
    }

    public Boolean getUseLAF() {
        return useLAF;
    }

    public Boolean getUseDark() {
        return useDark;
    }

    public void setLogApp(Boolean logApp) {
        this.logApp = logApp;
    }

    public void setUseLAF(Boolean useLAF) {
        this.useLAF = useLAF;
    }

    public void setUseDark(Boolean useDark) {
        this.useDark = useDark;
    }

    public static ApplicationConfigReader getLastConfig() {
        return lastConfig;
    }
}
