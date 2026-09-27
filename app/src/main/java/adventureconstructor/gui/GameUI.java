package adventureconstructor.gui;

import adventureconstructor.controllers.GameEngine;
import adventureconstructor.controllers.GameOperationsController;
import adventureconstructor.dao.ItemsDAO;
import adventureconstructor.dao.PlayersDAO;
import adventureconstructor.models.DialogOption;
import adventureconstructor.models.Item;
import adventureconstructor.models.LocAct;
import adventureconstructor.models.Player;
import adventureconstructor.utils.HibernateConfiguration;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class GameUI extends JFrame {
    private GameEngine eng = new GameEngine();
    private ScenePanel scenePanel = new ScenePanel();
    private JTextArea textArea = new JTextArea();
    private JPanel statsPanel = new JPanel();
    private JPanel actionPanel = new JPanel();
    private JLabel[] statLabels = new JLabel[12];
    private JButton invBtn = new JButton("Inventory");
    private JButton lvlBtn = new JButton("Level Up");

    private Player currentPlayer;

    public GameUI(Player player) {
        this.currentPlayer = player;

        setTitle("Adventure Builder");
        setSize(900, 850);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                eng.stopAmbient();
            }
        });

        fillWindow();
        loadData();
        updateUI();
    }

    private void fillWindow() {
        scenePanel.setPreferredSize(new Dimension(900, 420));
        add(scenePanel, BorderLayout.NORTH);

        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(new Color(20, 20, 30));
        textArea.setForeground(Color.WHITE);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        statsPanel.setPreferredSize(new Dimension(200, 0));
        statsPanel.setBackground(new Color(25, 25, 35));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        for (int i = 0; i < 12; i++) {
            statLabels[i] = new JLabel(" ");
            statLabels[i].setForeground(Color.WHITE);
            statLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 13));
            statsPanel.add(statLabels[i]);
        }
        statsPanel.add(Box.createVerticalStrut(10));
        invBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        lvlBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(invBtn);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lvlBtn);
        invBtn.addActionListener(e -> showInventory());
        lvlBtn.addActionListener(e -> showLevelUp());
        add(statsPanel, BorderLayout.EAST);

        actionPanel.setPreferredSize(new Dimension(900, 60));
        actionPanel.setBackground(new Color(30, 30, 40));
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        PlayersDAO playersDAO = new PlayersDAO(HibernateConfiguration.getEntityManagerFactory());
        ItemsDAO itemsDAO = new ItemsDAO(HibernateConfiguration.getEntityManagerFactory());
        eng.setGameOperationsController(new GameOperationsController(playersDAO, itemsDAO));

        eng.loadData();

        if (currentPlayer != null && currentPlayer.getId() != null) {
            eng.setPlayer(currentPlayer);
            eng.genLocation(null);
            eng.enterLoc();
        }
    }

    void updateUI() {
        if (eng.getPlayer() != null) {
            currentPlayer = eng.getPlayer();
            SaveLoadWindow.setCurrentPlayer(currentPlayer.toDb());
        }

        updateStats();
        SwingUtilities.invokeLater(() -> {
            actionPanel.removeAll();
            textArea.setText("");

            eng.playAmbientForMode();

            switch (eng.getMode()) {
                case "START":
                    showStart();
                    break;
                case "EXPLORE":
                    showExplore();
                    break;
                case "COMBAT":
                    showCombat();
                    break;
                case "DIALOG":
                    showDialog();
                    break;
                case "TRANSITION":
                    showTransition();
                    break;
                case "GAMEOVER":
                    showGameOver();
                    break;
            }
            actionPanel.revalidate();
            actionPanel.repaint();
        });
    }

    void updateStats() {
        if (eng.getPlayer() == null) {
            for (JLabel l : statLabels) l.setText(" ");
            invBtn.setEnabled(false);
            lvlBtn.setEnabled(false);
            return;
        }
        Player p = eng.getPlayer();
        statLabels[0].setText(" HP: " + p.getHp() + "/" + p.getMaxHp());
        statLabels[1].setText(" Gold: " + p.getGold());
        statLabels[2].setText(" Level: " + p.getLevel());
        statLabels[3].setText(" EXP: " + p.getExp() + "/" + p.getExpNext());
        statLabels[4].setText(" SP: " + p.getSp());
        statLabels[5].setText(" STR: " + p.getStr());
        statLabels[6].setText(" AGI: " + p.getAgi());
        statLabels[7].setText(" INT: " + p.getIntl());
        statLabels[8].setText(" END: " + p.getEnd());
        String wpn = p.getWeapon() != null ? p.getWeapon().getName() : "-";
        String arm = p.getArmor() != null ? p.getArmor().getName() : "-";
        statLabels[9].setText(" WPN: " + wpn);
        statLabels[10].setText(" ARM: " + arm);
        statLabels[11].setText(" Items: " + p.getInv().size());
        invBtn.setEnabled(true);
        lvlBtn.setEnabled(p.getSp() > 0);
    }

    void addButton(String label, ActionListener al) {
        JButton b = new JButton(label);
        b.addActionListener(al);
        b.setPreferredSize(new Dimension(160, 35));
        actionPanel.add(b);
    }

    void showStart() {
        scenePanel.setScene("Adventure Builder", "start");
        textArea.setText("Welcome to Adventure Builder!\n\n"
                + "A procedurally generated RPG. Travel through endless locations,\n"
                + "fight enemies, talk to NPCs, explore ruins.\n\n" + "Death resets everything. Choose wisely.\n\n"
                + "Stats:\n" + "  STR - damage in combat\n" + "  AGI - block & flee chance\n"
                + "  INT - persuasion skill\n" + "  END - max HP\n\n" + "Click NEW GAME to begin.");
        addButton("New Game", e -> {
            eng.newGame();
            updateUI();
        });
    }

    void showExplore() {
        scenePanel.setScene(eng.getCurLoc().getName(), eng.getCurLoc().getType(), eng.getCurImagePath());
        textArea.setText(eng.formatText(eng.getCurLoc().getDesc()));
        for (LocAct a : eng.getCurLoc().getActions()) {
            addButton(eng.formatText(a.getText()), e -> {
                eng.execAction(a);
                updateUI();
            });
        }
    }

    void showCombat() {
        scenePanel.setScene("Combat: " + eng.getCurEnemy().getName(), "hostile", eng.getCurImagePath());
        StringBuilder sb = new StringBuilder();
        sb.append("Enemy: ")
                .append(eng.getCurEnemy().getName())
                .append(" (HP: ")
                .append(eng.getCurEnemy().getHp())
                .append("/")
                .append(eng.getCurEnemy().getMaxHp())
                .append(")\n");
        sb.append("Enemy DEF: ")
                .append(eng.getCurEnemy().getDef())
                .append("  DMG: ")
                .append(eng.getCurEnemy().getDmg())
                .append("\n\n");
        for (String log : eng.getCombatLog()) sb.append(log).append("\n");
        textArea.setText(sb.toString());
        addButton("Attack", e -> {
            eng.pAttack();
            updateUI();
        });
        addButton("Defend", e -> {
            eng.pDefend();
            updateUI();
        });
        addButton("Item", e -> {
            showCombatInventory();
        });
        addButton("Flee", e -> {
            eng.pFlee();
            updateUI();
        });
    }

    void showDialog() {
        scenePanel.setScene("Talk: " + eng.getCurDialog().getNpcName(), "peaceful", eng.getCurImagePath());
        StringBuilder sb = new StringBuilder();
        sb.append(eng.getCurDialog().getNpcName()).append(":\n");
        if (eng.getCurNode() != null) {
            sb.append(eng.formatText(eng.getCurNode().getText())).append("\n");
            textArea.setText(sb.toString());
            int idx = 1;
            for (DialogOption o : eng.getCurNode().getOptions()) {
                String label = "[" + idx + "] " + eng.formatText(o.getText());
                if ("dice".equals(o.getType())) label += " (DC:" + o.getDc() + ")";
                addButton(label, e -> {
                    eng.chooseOption(o);
                    updateUI();
                });
                idx++;
            }
        }
    }

    void showTransition() {
        scenePanel.setScene("...", "");
        textArea.setText(
                eng.getTransitionMsg() + "\n\nHP restored: +" + eng.getPlayer().getMaxHp() / 4);
        addButton("Continue", e -> {
            eng.continueAfterTransition();
            updateUI();
        });
    }

    void showGameOver() {
        scenePanel.setScene("YOU DIED", "GAMEOVER");
        textArea.setText("Your journey ends here.\nLevel reached: "
                + eng.getPlayer().getLevel() + "\nGold: " + eng.getPlayer().getGold() + "\n\nAll progress lost.");
        addButton("New Game", e -> {
            eng.newGame();
            updateUI();
        });
    }

    void showInventory() {
        if (eng.getPlayer() == null) return;
        JDialog d = new JDialog(this, "Inventory", true);
        d.setSize(350, 400);
        d.setLayout(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        for (Item it : eng.getPlayer().getInv()) model.addElement(it.toString());
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel bp = new JPanel();
        JButton eq = new JButton("Equip/Use");
        JButton cl = new JButton("Close");
        eq.addActionListener(e -> {
            int i = list.getSelectedIndex();
            if (i >= 0 && i < eng.getPlayer().getInv().size()) {
                Item it = eng.getPlayer().getInv().get(i);
                if ("consumable".equals(it.getType())) {
                    eng.getPlayer().heal(it.getValue());
                    eng.getPlayer().getInv().remove(i);
                } else {
                    eng.getPlayer().equip(it);
                }
                model.clear();
                for (Item it2 : eng.getPlayer().getInv()) model.addElement(it2.toString());
                updateStats();
                updateUI();
            }
        });
        cl.addActionListener(e -> d.dispose());
        bp.add(eq);
        bp.add(cl);
        d.add(bp, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    void showCombatInventory() {
        if (eng.getPlayer() == null || eng.getPlayer().getInv().isEmpty()) {
            textArea.setText("No consumables available.");
            return;
        }
        JDialog d = new JDialog(this, "Use Item", true);
        d.setSize(300, 350);
        d.setLayout(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        for (Item it : eng.getPlayer().getInv()) if ("consumable".equals(it.getType())) model.addElement(it.toString());
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel bp = new JPanel();
        JButton use = new JButton("Use");
        JButton cl = new JButton("Cancel");
        use.addActionListener(e -> {
            int i = list.getSelectedIndex();
            if (i >= 0) {
                int cnt = 0;
                for (Item it : eng.getPlayer().getInv()) {
                    if ("consumable".equals(it.getType())) {
                        if (cnt == i) {
                            eng.pUseItem(it);
                            d.dispose();
                            updateUI();
                            return;
                        }
                        cnt++;
                    }
                }
            }
        });
        cl.addActionListener(e -> d.dispose());
        bp.add(use);
        bp.add(cl);
        d.add(bp, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    void showLevelUp() {
        if (eng.getPlayer() == null || eng.getPlayer().getSp() <= 0) return;
        JDialog d = new JDialog(this, "Allocate SP (" + eng.getPlayer().getSp() + ")", true);
        d.setSize(250, 200);
        d.setLayout(new GridLayout(5, 1));
        JLabel info = new JLabel("SP: " + eng.getPlayer().getSp(), SwingConstants.CENTER);
        d.add(info);
        String[] stats = {"str", "agi", "intl", "end"};
        for (String st : stats) {
            JButton b = new JButton("+ " + st.toUpperCase());
            b.addActionListener(e -> {
                eng.getPlayer().allocSP(st);
                info.setText("SP: " + eng.getPlayer().getSp());
                if (eng.getPlayer().getSp() <= 0) {
                    d.dispose();
                }
                updateStats();
                updateUI();
            });
            d.add(b);
        }
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
