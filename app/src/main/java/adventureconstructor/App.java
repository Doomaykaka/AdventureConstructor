package adventureconstructor;

import adventureconstructor.gui.GameUI;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameUI ui = new GameUI();
            ui.setLocationRelativeTo(null);
            ui.setVisible(true);
        });
    }
}
