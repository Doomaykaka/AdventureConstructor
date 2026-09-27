package adventureconstructor.utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class SupportFunctions {
    public static void writeContentInNewFile(File folderToSave, String name, List<String> content) {
        try {
            File file = new File(folderToSave, name);

            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);

                for (String line : content) {
                    writer.append(line + System.lineSeparator());
                }

                writer.close();
            }
        } catch (NullPointerException | IOException e) {
            System.out.println("Error: save file problems");
        }
    }

    public static List<String> readFileContent(FileReader file) throws FileNotFoundException {
        List<String> fileContent = new ArrayList<>();

        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            fileContent.add(scanner.nextLine());
        }

        scanner.close();

        return fileContent;
    }

    public static Rectangle getWindowBounds(int width, int height) {
        Rectangle bounds = null;

        Rectangle screenSize = getScreenSize();
        int xCenter = (int) (screenSize.getWidth() / 2);
        int yCenter = (int) (screenSize.getHeight() / 2);

        int windowX = xCenter - width / 2;
        int windowY = yCenter - height / 2;

        bounds = new Rectangle(windowX, windowY, width, height);

        return bounds;
    }

    public static Rectangle getScreenSize() {
        Rectangle screenSize = null;

        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize = new Rectangle((int) screenDimension.getWidth(), (int) screenDimension.getHeight());

        return screenSize;
    }

    public static Image getAppIcon() {
        Image appIcon = null;

        URL appIconUrl = SupportFunctions.class.getResource("/adventureconstructor/images/app_icon.jpg");
        appIcon = Toolkit.getDefaultToolkit().getImage(appIconUrl);

        return appIcon;
    }

    public static ImageIcon getResourceImage(String name) {
        String path = "adventureconstructor/images/" + name;
        URL resourceURL = SupportFunctions.class.getClassLoader().getResource(path);

        return new ImageIcon(resourceURL);
    }

    public static JPanel getEntityWindowCheckbox(String label) {
        JPanel result = null;

        result = new JPanel();
        BoxLayout layout = new BoxLayout(result, BoxLayout.X_AXIS);
        result.setLayout(layout);

        JCheckBox input = new JCheckBox();

        result.add(new JLabel(label));
        result.add(input);

        return result;
    }

    public static void setEntityWindowCheckboxValue(JPanel panel, boolean value) {
        int checkboxIndex = 1;

        JCheckBox checkbox = (JCheckBox) panel.getComponent(checkboxIndex);
        checkbox.getModel().setSelected(value);
    }

    public static boolean getEntityWindowCheckboxValue(JPanel panel) {
        int checkboxIndex = 1;

        JCheckBox checkbox = (JCheckBox) panel.getComponent(checkboxIndex);
        return checkbox.isSelected();
    }

    public static void addButtonWithGap(JPanel panel, JButton button, int gap) {
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(gap, gap)));
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void addChildPanelWithGap(JPanel panel, JPanel child, int gap) {
        panel.add(child);
        panel.add(Box.createRigidArea(new Dimension(gap, gap)));
    }

    public static File chooseFile() {
        File selectedFile = null;

        JFileChooser fileChooser = new JFileChooser();
        int state = fileChooser.showOpenDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
        }

        return selectedFile;
    }

    public static File saveFile() {
        File selectedFile = null;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showSaveDialog(null);
        selectedFile = fileChooser.getSelectedFile();

        return selectedFile;
    }

    public static MouseListener getOnClickListener(Runnable callback) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callback.run();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ;
            }
        };
    }
}
