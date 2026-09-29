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
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ScenePanel extends JPanel {
    private String locName = "";
    private String locType = "";
    private String locDescription = "";
    private Image bgImage = null;

    public void setScene(String name, String type) {
        this.locName = name;
        this.locType = type;
        this.locDescription = "";
        this.bgImage = null;

        repaint();
    }

    public void setScene(String name, String type, String imagePath) {
        setScene(name, type, imagePath, "");
    }

    public void setScene(String name, String type, String imagePath, String description) {
        this.locName = name;
        this.locType = type;
        this.locDescription = description == null ? "" : description.trim();
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

        g2.setColor(border);
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(3, 3, getWidth() - 6, getHeight() - 6);

        drawSceneCaption(g2, border);
    }

    private void drawSceneCaption(Graphics2D g2, Color border) {
        int cardWidth = Math.max(0, getWidth() - 48);
        int cardX = (getWidth() - cardWidth) / 2;
        int textWidth = Math.max(0, cardWidth - 36);
        Font titleFont = fitTitleFont(g2, textWidth);
        Font descriptionFont = new Font("SansSerif", Font.PLAIN, 15);
        Font typeFont = new Font("SansSerif", Font.BOLD, 12);
        FontMetrics descriptionMetrics = g2.getFontMetrics(descriptionFont);
        List<String> descriptionLines = wrapDescription(descriptionMetrics, textWidth, 3);

        int topPadding = 12;
        int bottomPadding = 10;
        int titleHeight = 30;
        int descriptionGap = descriptionLines.isEmpty() ? 0 : 7;
        int descriptionHeight = descriptionLines.size() * 20;
        int typeGap = 5;
        int typeHeight = 16;
        int cardHeight =
                topPadding + titleHeight + descriptionGap + descriptionHeight + typeGap + typeHeight + bottomPadding;
        int cardY = getHeight() - cardHeight - 18;

        int gradientY = Math.max(0, cardY - 55);
        g2.setPaint(new java.awt.GradientPaint(
                0, gradientY, new Color(0, 0, 0, 0), 0, getHeight(), new Color(0, 0, 0, 135)));
        g2.fillRect(0, gradientY, getWidth(), getHeight() - gradientY);

        g2.setColor(new Color(10, 14, 22, 205));
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 22, 22);
        g2.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), 135));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 22, 22);

        int baseline = cardY + topPadding;
        g2.setFont(titleFont);
        g2.setColor(Color.WHITE);
        baseline += g2.getFontMetrics().getAscent();
        drawCentered(g2, locName, baseline);

        baseline += descriptionGap;
        g2.setFont(descriptionFont);
        g2.setColor(new Color(245, 246, 250, 235));
        for (String line : descriptionLines) {
            baseline += 20;
            drawCentered(g2, line, baseline);
        }

        baseline += typeGap + g2.getFontMetrics(typeFont).getAscent();
        drawLocationType(g2, typeFont, baseline);
    }

    private Font fitTitleFont(Graphics2D g2, int maxWidth) {
        int fontSize = 26;
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        while (fontSize > 18) {
            g2.setFont(font);
            if (g2.getFontMetrics().stringWidth(locName) <= maxWidth) break;
            font = new Font("SansSerif", Font.BOLD, --fontSize);
        }
        return font;
    }

    private List<String> wrapDescription(FontMetrics metrics, int maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (locDescription.isEmpty() || maxWidth <= 0) return lines;

        String[] words = locDescription.replaceAll("\\s+", " ").split(" ");
        String line = "";
        int nextWord = 0;
        while (nextWord < words.length && lines.size() < maxLines) {
            String candidate = line.isEmpty() ? words[nextWord] : line + " " + words[nextWord];
            if (metrics.stringWidth(candidate) <= maxWidth || line.isEmpty()) {
                line = candidate;
                nextWord++;
            } else {
                lines.add(line);
                line = "";
            }
        }
        if (!line.isEmpty() && lines.size() < maxLines) lines.add(line);

        if (nextWord < words.length && !lines.isEmpty()) {
            int lastIndex = lines.size() - 1;
            String lastLine = lines.get(lastIndex);
            while (!lastLine.isEmpty() && metrics.stringWidth(lastLine + "…") > maxWidth) {
                lastLine = lastLine.substring(0, lastLine.length() - 1).trim();
            }
            lines.set(lastIndex, lastLine + "…");
        }
        return lines;
    }

    private void drawCentered(Graphics2D g2, String text, int baseline) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - textWidth) / 2, baseline);
    }

    private void drawLocationType(Graphics2D g2, Font font, int baseline) {
        g2.setFont(font);
        String tl = "";

        if ("peaceful".equals(locType)) tl = "PEACEFUL";
        else if ("hostile".equals(locType)) tl = "HOSTILE";
        else if ("exploratory".equals(locType)) tl = "EXPLORATORY";
        if (!tl.isEmpty()) {
            int tw2 = g2.getFontMetrics().stringWidth(tl);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(tl, (getWidth() - tw2) / 2, baseline);
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
