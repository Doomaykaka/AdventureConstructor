package adventureconstructor.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Logger {
    private static volatile String appLogPath = null;
    private static final int PARENT_ELEMENT_IN_STACK_TRACE_INDEX = 2;

    private static volatile Logger instance;

    private static final String LOG_FILES_DELIMETER = "-------------------------------------";
    private static final String NEW_LINE = "\n";
    private static final String LOG_FILE_PARENT_FOLDER_NAME = "user.dir";
    private static final String LOG_FILENAME = "app.log";

    public static synchronized Logger getInstance() {
        if (instance == null) {
            rotateLog();

            instance = new Logger();
        }

        return instance;
    }

    public synchronized void info(String message) {
        if (!ApplicationConfigReader.getLastConfig().getLogApp()) {
            return;
        }

        PrintWriter printWriter = null;
        boolean isNew = false;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        isNew = appLogPath == null;

        if (isNew) {
            appLogPath = prepareAppLogPath();
        }

        if (appLogPath != null) {
            try {
                boolean append = true;
                printWriter = new PrintWriter(new FileOutputStream(appLogPath, append));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (printWriter != null && isNew) {
            printWriter.write(LOG_FILES_DELIMETER + NEW_LINE);
            printWriter.write("<" + LocalDateTime.now() + "> - Log started" + NEW_LINE);
        }

        if (printWriter != null && message != null) {
            StackTraceElement parent = stackTrace[PARENT_ELEMENT_IN_STACK_TRACE_INDEX];

            printWriter.write("<" + LocalDateTime.now() + "> - [" + parent.getClassName() + "." + parent.getMethodName()
                    + "] " + NEW_LINE);
            printWriter.write("INFO - " + message + NEW_LINE);
        }

        printWriter.close();
        printWriter = null;
    }

    public synchronized void warning(String message) {
        if (!ApplicationConfigReader.getLastConfig().getLogApp()) {
            return;
        }

        PrintWriter printWriter = null;
        boolean isNew = false;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        isNew = appLogPath == null;

        if (isNew) {
            appLogPath = prepareAppLogPath();
        }

        if (appLogPath != null) {
            try {
                boolean append = true;
                printWriter = new PrintWriter(new FileOutputStream(appLogPath, append));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (printWriter != null && isNew) {
            printWriter.write(LOG_FILES_DELIMETER + NEW_LINE);
            printWriter.write("<" + LocalDateTime.now() + "> - Log started" + NEW_LINE);
        }

        if (printWriter != null && message != null) {
            StackTraceElement parent = stackTrace[PARENT_ELEMENT_IN_STACK_TRACE_INDEX];

            printWriter.write("<" + LocalDateTime.now() + "> - [" + parent.getClassName() + "." + parent.getMethodName()
                    + "] " + NEW_LINE);
            printWriter.write("WARNING - " + message + NEW_LINE);
        }

        printWriter.close();
        printWriter = null;
    }

    private synchronized String prepareAppLogPath() {
        String pathToAppLogFile = "";

        File userDirectory = new File(System.getProperty(LOG_FILE_PARENT_FOLDER_NAME));
        Path logFilePathObj = Path.of(userDirectory.getAbsolutePath().toString(), LOG_FILENAME);

        File appLogFile = logFilePathObj.toFile();

        if (!appLogFile.exists()) {
            try {
                appLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pathToAppLogFile = appLogFile.getAbsolutePath();

        return pathToAppLogFile;
    }

    private static synchronized void rotateLog() {
        File logFile = new File(LOG_FILENAME);

        if (logFile.exists() && !logFile.isDirectory()) {
            if (!logFile.delete()) {
                ;
            }
        }
    }
}
