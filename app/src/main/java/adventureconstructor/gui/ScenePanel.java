package adventureconstructor.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ScenePanel extends JPanel {
    private String locName = "";
    private String locType = "";
    private Image bgImage = null;

    public void setScene(String name, String type) {
        this.locName = name;
        this.locType = type;
        this.bgImage = null;

        repaint();
    }

    public void setScene(String name, String type, String imagePath) {
        this.locName = name;
        this.locType = type;
        this.bgImage = null;

        if (imagePath != null) {
            File f = new File(imagePath);

            if (f.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                this.bgImage = icon.getImage();
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Color border = getBorderColor();

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            drawBackgroundOnNoImage(g2);
        }

        g2.setPaint(new java.awt.GradientPaint(
                0, getHeight() - 120, new Color(0, 0, 0, 0), 0, getHeight(), new Color(0, 0, 0, 180)));
        g2.fillRect(0, getHeight() - 120, getWidth(), 120);

        g2.setColor(border);
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(3, 3, getWidth() - 6, getHeight() - 6);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(locName);
        g2.drawString(locName, (getWidth() - tw) / 2, getHeight() - 50);

        drawLocationType(g2);
    }

    private void drawLocationType(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String tl = "";

        if ("peaceful".equals(locType)) tl = "PEACEFUL";
        else if ("hostile".equals(locType)) tl = "HOSTILE";
        else if ("exploratory".equals(locType)) tl = "EXPLORATORY";
        if (!tl.isEmpty()) {
            int tw2 = g2.getFontMetrics().stringWidth(tl);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(tl, (getWidth() - tw2) / 2, getHeight() - 22);
        }
    }

    private Color getBorderColor() {
        if ("peaceful".equals(locType)) {
            return new Color(50, 200, 80);
        } else if ("hostile".equals(locType)) {
            return new Color(220, 50, 50);
        } else if ("exploratory".equals(locType)) {
            return new Color(220, 200, 50);
        } else if ("GAMEOVER".equals(locType)) {
            return new Color(150, 30, 30);
        }

        return new Color(80, 180, 220);
    }

    private void drawBackgroundOnNoImage(Graphics2D g2) {
        Color bg;

        if ("peaceful".equals(locType)) bg = new Color(30, 70, 40);
        else if ("hostile".equals(locType)) bg = new Color(80, 20, 20);
        else if ("exploratory".equals(locType)) bg = new Color(80, 70, 20);
        else if ("GAMEOVER".equals(locType)) bg = new Color(40, 10, 10);
        else bg = new Color(30, 30, 50);

        g2.setColor(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(200, 200, 200, 120));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));

        FontMetrics fm0 = g2.getFontMetrics();
        String noImg = "[ no image ]";

        g2.drawString(noImg, (getWidth() - fm0.stringWidth(noImg)) / 2, getHeight() / 2 - 40);
    }
}
