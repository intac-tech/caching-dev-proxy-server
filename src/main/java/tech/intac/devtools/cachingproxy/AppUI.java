package tech.intac.devtools.cachingproxy;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

public class AppUI {
    private JPanel pnlContent;
    private JTextField txtBaseUrl;
    private JCheckBox chkCacheGet;
    private JCheckBox chkCachePost;
    private JButton btnSave;
    private JButton btnResetCache;
    private JButton btnMapLocal;

    public AppUI() {
        var config = Config.getInstance();

        btnSave.addActionListener(e -> {
            config.setBaseUrl(txtBaseUrl.getText());
            config.setCacheGetRequests(chkCacheGet.isSelected());
            config.setCachePostRequests(chkCachePost.isSelected());
            config.save();

            MsgBox.info(pnlContent, "Config updated.");
        });

        btnResetCache.addActionListener(e -> {
            try {
                // delegate to the OS for faster processing
                Desktop.getDesktop().moveToTrash(config.getLocalOverridesPath().toFile());
                MsgBox.info(pnlContent, "Local cache moved trash.");
            } catch (Exception ex) {
                ex.printStackTrace();
                MsgBox.err(pnlContent, "Failed to reset cache. Please check the logs for details");
            }
        });

        btnMapLocal.addActionListener(e -> {
            MsgBox.info(pnlContent, "Not yet implemented.");
        });

        txtBaseUrl.setText(config.getBaseUrl());
        chkCacheGet.setSelected(config.isCacheGetRequests());
        chkCachePost.setSelected(config.isCachePostRequests());

        var frame = new JFrame("Dev Proxy Server v1.0");
        frame.setContentPane(pnlContent);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);

        EventQueue.invokeLater(() -> {
            frame.setVisible(true);
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        pnlContent = new JPanel();
        pnlContent.setLayout(new BorderLayout(0, 0));
        pnlContent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlContent.add(panel1, BorderLayout.CENTER);
        final JLabel label1 = new JLabel();
        label1.setText("Base URL:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtBaseUrl = new JTextField();
        panel1.add(txtBaseUrl, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(500, -1), null, 0, false));
        chkCacheGet = new JCheckBox();
        chkCacheGet.setText("Cache GET Requests?");
        panel1.add(chkCacheGet, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chkCachePost = new JCheckBox();
        chkCachePost.setText("Cache POST Requests?");
        panel1.add(chkCachePost, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(10, 0, 0, 0), -1, -1));
        pnlContent.add(panel2, BorderLayout.SOUTH);
        btnSave = new JButton();
        btnSave.setText("Save");
        panel2.add(btnSave, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnResetCache = new JButton();
        btnResetCache.setText("Reset Cache");
        panel2.add(btnResetCache, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnMapLocal = new JButton();
        btnMapLocal.setText("Map Local Path");
        panel2.add(btnMapLocal, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlContent;
    }

}
