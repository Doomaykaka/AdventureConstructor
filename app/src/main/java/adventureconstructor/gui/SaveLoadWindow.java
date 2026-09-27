package adventureconstructor.gui;

import adventureconstructor.controllers.GameOperationsController;
import adventureconstructor.dao.ItemsDAO;
import adventureconstructor.dao.PlayersDAO;
import adventureconstructor.models.db.Item;
import adventureconstructor.models.db.Player;
import adventureconstructor.utils.HibernateConfiguration;
import adventureconstructor.utils.SupportFunctions;
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class SaveLoadWindow extends JFrame {
    private static GameOperationsController gameOperationsController;
    private final boolean runInStatisticMode;
    private final JTable table;
    private final DefaultTableModel tableModel;

    private static volatile Player currentPlayer;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;
    private static final String WINDOW_TITLE = "Load game";

    public SaveLoadWindow(boolean runInStatisticMode) {
        this.runInStatisticMode = runInStatisticMode;

        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        String[] columnNames;
        if (runInStatisticMode) {
            columnNames = new String[] {"Player name", "View stats"};
        } else {
            columnNames = new String[] {"Player name", "Load", "Delete"};
        }

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        table.setFillsViewportHeight(true);
        table.setRowHeight(45);
        table.setAutoCreateRowSorter(true);

        if (runInStatisticMode) {
            table.getColumnModel().getColumn(1).setCellRenderer(getButtonRenderer("View stats"));
        } else {
            table.getColumnModel().getColumn(1).setCellRenderer(getButtonRenderer("Load"));
            table.getColumnModel().getColumn(2).setCellRenderer(getButtonRenderer("Delete"));
        }

        table.getSelectionModel().addListSelectionListener(getTableListSelectionListener());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomRightPanel = new JPanel();
        bottomRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnLoadFromFile = new JButton("Import from file");
        btnLoadFromFile.addActionListener(e -> loadPlayerFromFile());
        bottomRightPanel.add(btnLoadFromFile);

        if (!runInStatisticMode) {
            add(bottomRightPanel, BorderLayout.SOUTH);
        }

        initController();
        loadPlayers();
    }

    private TableCellRenderer getButtonRenderer(String text) {
        return new TableCellRenderer() {
            private JButton button;

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (button == null) {
                    button = new JButton();
                    button.setPreferredSize(new Dimension(100, 35));
                }
                button.setText(text);
                return button;
            }
        };
    }

    private ListSelectionListener getTableListSelectionListener() {
        return e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) return;

                int viewColumn = table.getSelectedColumn();
                if (viewColumn < 0) return;

                int modelRow = table.convertRowIndexToModel(viewRow);
                int modelColumn = table.convertColumnIndexToModel(viewColumn);

                Player player = (Player) tableModel.getValueAt(modelRow, 1);

                if (runInStatisticMode) {
                    viewStatsClick(modelColumn, player);
                } else {
                    loadDeleteClick(modelColumn, player);
                }
            }
        };
    }

    private void loadDeleteClick(int modelColumn, Player player) {
        if (modelColumn == 1) {
            setCurrentPlayer(player);
            SupportFunctions.showMessage("Player loaded: " + player.getName());
            this.dispose();

            AdditionalGameWindows.loadGameFromDb(player);
        } else if (modelColumn == 2) {
            removePlayer(player);
        }
    }

    private void viewStatsClick(int modelColumn, Player player) {
        if (modelColumn != 1) return;

        StringBuilder builder = new StringBuilder();

        builder.append("Player: ").append(player.getName()).append("\n\n");
        builder.append("Created: ").append(player.getCreationDate()).append("\n");
        builder.append("Level: ").append(player.getLevel()).append("\n");
        builder.append("EXP: ")
                .append(player.getExp())
                .append(" / ")
                .append(player.getExpNext())
                .append("\n");
        builder.append("Gold: ").append(player.getGold()).append("\n");
        builder.append("Score: ").append(player.getScore()).append("\n");
        builder.append("Alive: ").append(player.isAlive() ? "Yes" : "No").append("\n\n");
        builder.append("HP: ")
                .append(player.getHp())
                .append(" / ")
                .append(player.getMaxHp())
                .append("\n");
        builder.append("STR: ").append(player.getStr()).append("\n");
        builder.append("AGI: ").append(player.getAgi()).append("\n");
        builder.append("INT: ").append(player.getIntl()).append("\n");
        builder.append("END: ").append(player.getEnd()).append("\n");
        builder.append("SP: ").append(player.getSp()).append("\n\n");

        builder.append("Inventory (").append(player.getInventory().size()).append(" items):\n");
        for (Item item : player.getInventory()) {
            builder.append("  ");
            builder.append(item.getName());
            builder.append(" [").append(item.getType()).append("]");
            builder.append(" +").append(item.getValue());
            if (item.isEquipped()) builder.append(" (equipped)");
            builder.append("\n");
        }

        SupportFunctions.showMessage(builder.toString());
    }

    private void removePlayer(Player player) {
        Object[] options = {"Remove", "Cancel"};

        SwingUtilities.invokeLater(() -> {
            int answer = JOptionPane.showOptionDialog(
                    null,
                    "Remove player \"" + player.getName() + "\"?",
                    "Remove player",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (answer == 0) {
                gameOperationsController.removePlayer(player.getId());
                SupportFunctions.showMessage("Player removed");
            }

            refreshTable();
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        loadPlayers();
    }

    private void initController() {
        PlayersDAO playersDAO = new PlayersDAO(HibernateConfiguration.getEntityManagerFactory());
        ItemsDAO itemsDAO = new ItemsDAO(HibernateConfiguration.getEntityManagerFactory());
        gameOperationsController = new GameOperationsController(playersDAO, itemsDAO);
    }

    private void loadPlayers() {
        List<Player> players = gameOperationsController.getAllPlayers();
        for (Player player : players) {
            addPlayerRow(player);
        }
    }

    private void addPlayerRow(Player player) {
        StringBuilder builder = new StringBuilder();
        builder.append(player.getName());
        builder.append(" [level ").append(player.getLevel()).append("]");
        builder.append(" - score: ").append(player.getScore());

        if (runInStatisticMode) {
            tableModel.addRow(new Object[] {builder.toString(), player});
        } else {
            tableModel.addRow(new Object[] {builder.toString(), player, player});
        }
    }

    private void loadPlayerFromFile() {
        File saveFile = SupportFunctions.chooseFile();
        if (saveFile == null) return;

        Player loadedPlayer = gameOperationsController.importPlayer(saveFile);
        if (loadedPlayer != null) {
            gameOperationsController.savePlayer(loadedPlayer);
            refreshTable();
            SupportFunctions.showMessage("Player imported from file");
        } else {
            SupportFunctions.showMessage("Failed to import player");
        }
    }

    public void showWindow() {
        setVisible(true);
    }

    public static void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    public static void exportCurrentPlayer() {
        if (getCurrentPlayer() == null || gameOperationsController == null) {
            SupportFunctions.showMessage("Load a player before export");
            return;
        }

        File saveFile = SupportFunctions.saveFile();
        if (saveFile == null) {
            SupportFunctions.showMessage("Invalid save file");
            return;
        }

        gameOperationsController.exportPlayer(getCurrentPlayer(), saveFile);
        SupportFunctions.showMessage("Player exported to file");
    }
}
