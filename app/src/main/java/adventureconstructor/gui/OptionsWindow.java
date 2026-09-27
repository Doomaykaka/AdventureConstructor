package adventureconstructor.gui;

import adventureconstructor.utils.ApplicationConfigReader;
import adventureconstructor.utils.SupportFunctions;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class OptionsWindow extends JFrame {
    private static final int WIDTH = 250;
    private static final int HEIGHT = 375;
    private static final String WINDOW_TITLE = "Settings";

    public OptionsWindow() {
        boolean isResizable = false;

        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(isResizable);

        setIconImage(SupportFunctions.getAppIcon());

        JPanel windowLayer = new JPanel();
        windowLayer.setLayout(new BoxLayout(windowLayer, BoxLayout.Y_AXIS));

        List<JPanel> options = fillWindowOptions(windowLayer);
        List<JButton> controls = fillWindowControls(windowLayer);

        add(windowLayer);

        readOptionsStateAndSetInGUI(options);

        addButtonsActionListeners(controls, options);
    }

    public void showWindow() {
        setVisible(true);
    }

    private List<JPanel> fillWindowOptions(JPanel windowLayer) {
        List<JPanel> options = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JPanel chkLogApp = SupportFunctions.getEntityWindowCheckbox("Log app");
        JPanel chkUseLaf = SupportFunctions.getEntityWindowCheckbox("Use Laf");
        JPanel chkUseDark = SupportFunctions.getEntityWindowCheckbox("Use Dark theme");
        JPanel chkUseSound = SupportFunctions.getEntityWindowCheckbox("Use sound");

        int gap = 15;
        SupportFunctions.addChildPanelWithGap(panel, chkLogApp, gap);
        SupportFunctions.addChildPanelWithGap(panel, chkUseLaf, gap);
        SupportFunctions.addChildPanelWithGap(panel, chkUseDark, gap);
        SupportFunctions.addChildPanelWithGap(panel, chkUseSound, gap);

        options.add(chkLogApp);
        options.add(chkUseLaf);
        options.add(chkUseDark);
        options.add(chkUseSound);

        windowLayer.add(panel);

        return options;
    }

    private List<JButton> fillWindowControls(JPanel windowLayer) {
        List<JButton> controls = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JButton btnSaveOptions = new JButton("Save");
        JButton btnCancelOptions = new JButton("Cancel");

        int gap = 15;
        SupportFunctions.addButtonWithGap(panel, btnSaveOptions, gap);
        SupportFunctions.addButtonWithGap(panel, btnCancelOptions, gap);

        controls.add(btnSaveOptions);
        controls.add(btnCancelOptions);

        windowLayer.add(panel);

        return controls;
    }

    private void readOptionsStateAndSetInGUI(List<JPanel> options) {
        ApplicationConfigReader configReader = ApplicationConfigReader.getLastConfig();

        JPanel logAppPanel = options.get(0);
        JPanel useLafPanel = options.get(1);
        JPanel useDarkPanel = options.get(2);
        JPanel useSoundPanel = options.get(3);

        SupportFunctions.setEntityWindowCheckboxValue(logAppPanel, configReader.getLogApp());
        SupportFunctions.setEntityWindowCheckboxValue(useLafPanel, configReader.getUseLAF());
        SupportFunctions.setEntityWindowCheckboxValue(useDarkPanel, configReader.getUseDark());
        SupportFunctions.setEntityWindowCheckboxValue(useSoundPanel, configReader.getUseSound());
    }

    private void addButtonsActionListeners(List<JButton> buttons, List<JPanel> options) {
        ActionListener listener = e -> {
            String command = e.getActionCommand();
            switch (command) {
                case "Save":
                    SupportFunctions.showMessage("Options saved");
                    setupOptionsFromGUI(options);
                    this.dispose();

                    break;
                case "Cancel":
                    SupportFunctions.showMessage("Cancel");
                    this.dispose();

                    break;
            }
        };

        for (JButton button : buttons) {
            button.addActionListener(listener);
        }
    }

    private void setupOptionsFromGUI(List<JPanel> options) {
        ApplicationConfigReader configReader = ApplicationConfigReader.getLastConfig();

        JPanel logAppPanel = options.get(0);
        JPanel useLafPanel = options.get(1);
        JPanel useDarkPanel = options.get(2);
        JPanel useSoundPanel = options.get(3);

        boolean logApp = SupportFunctions.getEntityWindowCheckboxValue(logAppPanel);
        boolean useLaf = SupportFunctions.getEntityWindowCheckboxValue(useLafPanel);
        boolean useDark = SupportFunctions.getEntityWindowCheckboxValue(useDarkPanel);
        boolean useSound = SupportFunctions.getEntityWindowCheckboxValue(useSoundPanel);

        configReader.setLogApp(logApp);
        configReader.setUseLAF(useLaf);
        configReader.setUseDark(useDark);
        configReader.setUseSound(useSound);

        try {
            configReader.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
