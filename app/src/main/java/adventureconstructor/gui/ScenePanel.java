package adventureconstructor.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class ScenePanel extends JPanel {
    private String locName = "";
    private String locType = "";

    public void setScene(String name, String type) {
        this.locName = name;
        this.locType = type;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg, border;
        if ("peaceful".equals(locType)) {
            bg = new Color(30, 70, 40);
            border = Color.GREEN;
        } else if ("hostile".equals(locType)) {
            bg = new Color(80, 20, 20);
            border = Color.RED;
        } else if ("exploratory".equals(locType)) {
            bg = new Color(80, 70, 20);
            border = Color.YELLOW;
        } else if ("GAMEOVER".equals(locType)) {
            bg = new Color(40, 10, 10);
            border = Color.RED;
        } else {
            bg = new Color(30, 30, 50);
            border = Color.CYAN;
        }
        g2.setColor(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(border);
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(3, 3, getWidth() - 6, getHeight() - 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(locName);
        g2.drawString(locName, (getWidth() - tw) / 2, getHeight() / 2 + 10);
        // Type label
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String tl = "";
        if ("peaceful".equals(locType)) tl = "PEACEFUL";
        else if ("hostile".equals(locType)) tl = "HOSTILE";
        else if ("exploratory".equals(locType)) tl = "EXPLORATORY";
        if (!tl.isEmpty()) {
            int tw2 = g2.getFontMetrics().stringWidth(tl);
            g2.drawString(tl, (getWidth() - tw2) / 2, getHeight() - 20);
        }
    }
}
