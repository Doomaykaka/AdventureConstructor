package adventureconstructor;

import adventureconstructor.gui.MenuWindow;
import adventureconstructor.models.db.Item;
import adventureconstructor.models.db.Player;
import adventureconstructor.utils.ApplicationConfigReader;
import adventureconstructor.utils.HibernateConfiguration;
import adventureconstructor.utils.Logger;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class App {
    private static final long SCREENSAVER_TIME = 3000;

    public static void main(String[] args) throws IOException {
        ApplicationConfigReader appConfigReader = readConfig();

        Logger.getInstance().info("App started");
        Logger.getInstance().info("Configs loaded");

        prepareDatabase(appConfigReader);
        prepareGUI(appConfigReader);

        Logger.getInstance().info("App configurated");

        startUpApplication(args);
    }

    private static ApplicationConfigReader readConfig() throws FileNotFoundException, IOException {
        return new ApplicationConfigReader();
    }

    private static void startUpApplication(String[] args) {
        MenuWindow window = new MenuWindow();
        SwingUtilities.invokeLater(() -> window.showScreensaver());

        try {
            Thread.sleep(SCREENSAVER_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> window.showWindow());
    }

    private static void prepareDatabase(ApplicationConfigReader appConfigReader) {
        Logger.getInstance().info("Start database prepare");

        registerEntities();
        HibernateConfiguration.build(appConfigReader);

        Logger.getInstance().info("Database ready");
    }

    private static void registerEntities() {
        HibernateConfiguration.addEntity(Item.class);
        HibernateConfiguration.addEntity(Player.class);
    }

    private static void prepareGUI(ApplicationConfigReader appConfigReader) {
        Logger.getInstance().info("Start GUI prepare");

        if (appConfigReader.getUseLAF()) {
            FlatIntelliJLaf.setup();
            try {
                if (appConfigReader.getUseDark()) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                }
            } catch (UnsupportedLookAndFeelException e) {
                Logger.getInstance().warning("GUI style setup error");
                Logger.getInstance().warning(e.getMessage());
                e.printStackTrace();
            }
        }

        Logger.getInstance().info("GUI ready");
    }
}
