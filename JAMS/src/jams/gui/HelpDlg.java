/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jams.tools.JAMSTools;
import jams.JAMS;

/**
 *
 * @author Heiko Busch
 */
public class HelpDlg extends JDialog {

    public final static int OK_RESULT = 0;
    public final static int CANCEL_RESULT = -1;
    private HelpComponent helpComponent;
    /**
     * the base url coming from outside 
     */
    private String baseUrl = "";
    /**
     *  the pane to be filled with content
     */
    private JTextPane webPagePane;

    public HelpDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        init();
    }

    public HelpComponent getHelpComponent() {
        return helpComponent;
    }

    public void setHelpComponent(HelpComponent helpComponent) {
        this.helpComponent = helpComponent;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * init the help dialog without filling content
     */
    public void init() {
        setModal(false);
        this.setTitle(JAMS.resources.getString("Help"));

        this.setLayout(new BorderLayout());
        GridBagLayout gbl = new GridBagLayout();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(gbl);

        GUIHelper.addGBComponent(contentPanel, gbl, new JPanel(), 0, 0, 1, 1, 0, 0);

        JButton okButton = new JButton(JAMS.resources.getString("OK"));
        ActionListener okListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        okButton.addActionListener(okListener);
        getRootPane().setDefaultButton(okButton);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        webPagePane = new JTextPane();
        webPagePane.setEditable(false); // Start read-only
        JScrollPane scrollPane = new JScrollPane(webPagePane);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * load the content of helpComponent into webPagePane
     * content could be text or url-page
     * 
     * @param helpComponent
     */
    public void load(HelpComponent helpComponent) {

        setHelpComponent(helpComponent);

        if (this.helpComponent.hasHelpText()) {
            webPagePane.setContentType("text/html");
            webPagePane.setText(this.helpComponent.getHelpText());
            this.setVisible(true);
        }
        if (this.helpComponent.hasHelpURL()) {
            String url = this.baseUrl;
            if (!JAMSTools.isEmptyString(url)) {
                url += "/";
            }
            url += this.helpComponent.getHelpURL();

            try {
//                webPagePane.setContentType("text/html");
//                webPagePane.setPage(url);
                GUIHelper.openURL(url);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(webPagePane, new String[]{
                            JAMS.resources.getString("Unable_to_open_file"), url
                        }, JAMS.resources.getString("File_Open_Error"),
                        JOptionPane.ERROR_MESSAGE);
                setCursor(Cursor.getDefaultCursor());
            }
        }

        pack();
    }
}
