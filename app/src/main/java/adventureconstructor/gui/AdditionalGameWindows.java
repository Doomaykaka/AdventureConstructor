package adventureconstructor.gui;

import adventureconstructor.controllers.GameOperationsController;
import adventureconstructor.dao.ItemsDAO;
import adventureconstructor.dao.PlayersDAO;
import adventureconstructor.models.Player;
import adventureconstructor.utils.HibernateConfiguration;
import adventureconstructor.utils.Logger;
import javax.swing.*;

public class AdditionalGameWindows {
    private static volatile GameOperationsController gameOperationsController;

    public static void startNewGame() {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(
                    null, "Enter the player's name:", "Player Creation", JOptionPane.QUESTION_MESSAGE);

            if (name == null || name.trim().isEmpty()) return;

            Logger.getInstance().info("Start new game: " + name);

            GameOperationsController controller = getGameOperationsController();

            SwingUtilities.invokeLater(() -> {
                adventureconstructor.models.db.Player dbPlayer = controller.createNewPlayer(name);
                Player bizPlayer = Player.fromDb(dbPlayer);

                GameUI ui = new GameUI(bizPlayer);
                ui.setLocationRelativeTo(null);
                ui.setVisible(true);
            });
        });
    }

    public static void loadGame(Player bizPlayer) {
        if (bizPlayer == null) {
            Logger.getInstance().info("Cannot load game: player is null");
            return;
        }

        Logger.getInstance().info("Load game: " + bizPlayer.getName());

        SwingUtilities.invokeLater(() -> {
            GameUI ui = new GameUI(bizPlayer);
            ui.setLocationRelativeTo(null);
            ui.setVisible(true);
        });
    }

    public static void loadGameFromDb(adventureconstructor.models.db.Player dbPlayer) {
        if (dbPlayer == null) {
            Logger.getInstance().info("Cannot load game: db player is null");
            return;
        }

        loadGame(Player.fromDb(dbPlayer));
    }

    public static void openSaveLoadWindow() {
        SwingUtilities.invokeLater(() -> {
            SaveLoadWindow window = new SaveLoadWindow(false);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    public static void openStatsWindow() {
        SwingUtilities.invokeLater(() -> {
            SaveLoadWindow window = new SaveLoadWindow(true);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    public static synchronized GameOperationsController getGameOperationsController() {
        if (gameOperationsController != null) {
            return gameOperationsController;
        }

        PlayersDAO playersDAO = new PlayersDAO(HibernateConfiguration.getEntityManagerFactory());
        ItemsDAO itemsDAO = new ItemsDAO(HibernateConfiguration.getEntityManagerFactory());

        gameOperationsController = new GameOperationsController(playersDAO, itemsDAO);

        return gameOperationsController;
    }
}
