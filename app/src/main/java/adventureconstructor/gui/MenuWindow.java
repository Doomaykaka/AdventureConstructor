package adventureconstructor.gui;

import adventureconstructor.utils.Logger;
import adventureconstructor.utils.SupportFunctions;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MenuWindow extends JFrame {
    private ScreensaverWindow screensaverWindow = null;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 375;
    private static final String WINDOW_TITLE = "Adventure Builder";

    public MenuWindow() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        setIconImage(SupportFunctions.getAppIcon());

        List<JButton> controls = fillWindowElements();
        addButtonsActionListeners(controls);
    }

    public void showScreensaver() {
        screensaverWindow = new ScreensaverWindow();
        screensaverWindow.create();
    }

    public void showWindow() {
        if (screensaverWindow != null) screensaverWindow.close();
        setVisible(true);
    }

    private List<JButton> fillWindowElements() {
        List<JButton> controls = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JButton btnNewGame = new JButton("New game");
        JButton btnLoadGame = new JButton("Load game");
        JButton btnSaveGame = new JButton("Save game");
        JButton btnStatistic = new JButton("Statistic");
        JButton btnExit = new JButton("Exit");

        int gap = 15;
        SupportFunctions.addButtonWithGap(panel, btnNewGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnLoadGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnSaveGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnStatistic, gap);
        SupportFunctions.addButtonWithGap(panel, btnExit, gap);

        controls.add(btnNewGame);
        controls.add(btnLoadGame);
        controls.add(btnSaveGame);
        controls.add(btnStatistic);
        controls.add(btnExit);

        add(panel);

        return controls;
    }

    private void addButtonsActionListeners(List<JButton> buttons) {
        ActionListener listener = e -> {
            String command = e.getActionCommand();

            switch (command) {
                case "New game":
                    AdditionalGameWindows.startNewGame();
                    break;
                case "Load game":
                    AdditionalGameWindows.openSaveLoadWindow();
                    break;
                case "Save game":
                    SaveLoadWindow.exportCurrentPlayer();
                    break;
                case "Statistic":
                    AdditionalGameWindows.openStatsWindow();
                    break;
                case "Exit":
                    int confirm = JOptionPane.showConfirmDialog(
                            this, "Do you really want to get out?", "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                    break;
            }
        };

        for (JButton button : buttons) {
            button.addActionListener(listener);
        }
    }

    public class ScreensaverWindow {
        private JFrame screensaverFrame;

        private static final int SCREENSAVER_WIDTH = 665;
        private static final int SCREENSAVER_HEIGHT = 380;

        private static final String SCREENSAVER_WINDOW_TITLE = "Wait please";
        private static final String SCREENSAVER_IMAGE = "screen.png";

        public ScreensaverWindow() {
            Logger.getInstance().info("Screensaver creating");
            init();
            insertComponents();
        }

        private void init() {
            Logger.getInstance().info("Screensaver initialization");

            Dimension preferredSize = new Dimension(SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT);

            screensaverFrame = new JFrame(SCREENSAVER_WINDOW_TITLE);
            screensaverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            screensaverFrame.setLayout(null);
            screensaverFrame.setBounds(SupportFunctions.getWindowBounds(SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT));
            screensaverFrame.setPreferredSize(preferredSize);
            screensaverFrame.setResizable(false);
            screensaverFrame.setIconImage(SupportFunctions.getAppIcon());
        }

        private void insertComponents() {
            Logger.getInstance().info("Screensaver components creation");

            Container container = screensaverFrame.getContentPane();

            ImageIcon screensaverImage = SupportFunctions.getResourceImage(SCREENSAVER_IMAGE);
            JLabel imageLabel = new JLabel();
            imageLabel.setIcon(screensaverImage);
            imageLabel.setBounds(0, 0, SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT);

            container.add(imageLabel);
            screensaverFrame.pack();
        }

        public void create() {
            Logger.getInstance().info("Screensaver launch");
            SwingUtilities.invokeLater(() -> screensaverFrame.setVisible(true));
        }

        public void close() {
            Logger.getInstance().info("Screensaver close");
            SwingUtilities.invokeLater(() -> {
                if (screensaverFrame != null) screensaverFrame.dispose();
            });
        }
    }
}
