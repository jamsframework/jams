
package jams.logging;

import jams.gui.tools.GUIHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */


public class MessageBoxWithDetails extends JDialog{
    String message, title,details;
    
    JTextArea messageLabel = new JTextArea();
    
    JButton detailsButton = new JButton("View Details >>");
    JButton okButton = new JButton("OK");
    
    JTextArea detailsArea = new JTextArea(20, 40);
    JScrollPane detailsPane = new JScrollPane(detailsArea);
    
    int icon = -1;
    
    private MessageBoxWithDetails(Component parent, String message, String title, String details, int icon){
        super(GUIHelper.findWindow(parent), title);
        
        this.message = message;
        this.title = title;
        this.details = details;
        this.icon = icon;
        
        detailsButton.putClientProperty("state", false);
        detailsPane.setVisible(false);
        
        detailsArea.setText(this.details);
        messageLabel.setText(message);
        
        initGUI();
        initActions();
        
        setModal(true);
                
        //revalidate();
        //repaint();
        pack();
    }

    private void initGUI(){
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel eastPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        outerPanel.add(mainPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(eastPanel, BorderLayout.WEST);
        centerPanel.add(southPanel, BorderLayout.SOUTH);

        centerPanel.add(messageLabel, BorderLayout.CENTER);

        southPanel.add(okButton);
        if (details!=null)
            southPanel.add(detailsButton);
        
        outerPanel.add(detailsPane, BorderLayout.SOUTH);
        detailsPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 6, 40, 6), 
                BorderFactory.createEtchedBorder()));
        
        messageLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
              
        JLabel iconLabel = new JLabel();
        eastPanel.add(iconLabel);
        switch(icon){
            case JOptionPane.INFORMATION_MESSAGE : iconLabel.setIcon(javax.swing.UIManager.getIcon("OptionPane.informationIcon")); break;
            case JOptionPane.WARNING_MESSAGE : iconLabel.setIcon(javax.swing.UIManager.getIcon("OptionPane.warningIcon")); break;
            case JOptionPane.ERROR_MESSAGE : iconLabel.setIcon(javax.swing.UIManager.getIcon("OptionPane.errorIcon")); break;
            case JOptionPane.QUESTION_MESSAGE : iconLabel.setIcon(javax.swing.UIManager.getIcon("OptionPane.questionIcon")); break;
            default: break; //do nothing
        }
        if (iconLabel.getIcon() != null)
            iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 4));
        
        iconLabel.setVerticalAlignment(JLabel.NORTH);
        add(outerPanel);
        
        Dimension dMin = this.getMinimumSize();
        dMin.width = Math.min(Math.max(dMin.width, 460),1000);
        this.setMinimumSize(dMin);
        
        /*Dimension dmax = this.getMaximumSize();
        dmax.width = Math.min(dmax.width, 1000);
        this.setMaximumSize(dmax);
        
         dmax = this.getPreferredSize();
        dmax.width = Math.min(dmax.width, 1000);
        this.setPreferredSize(dmax);*/
        
        messageLabel.setLineWrap(true);
        messageLabel.setEnabled(false);    
        messageLabel.setOpaque(false);
        
        messageLabel.setRows(messageLabel.getLineCount());                
    }
    
    private void initActions(){
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = (Boolean)detailsButton.getClientProperty("state");
                detailsPane.setVisible(!state);
                detailsButton.putClientProperty("state", !state);
                pack();
            }
        });
    }
    
    static public MessageBoxWithDetails showMessageBoxWithDetails(Component parent, String message, String title, String details, int icon){
        MessageBoxWithDetails msgBox = new MessageBoxWithDetails(parent, message, title, details, icon);
        GUIHelper.centerOnParent(msgBox, true);
        msgBox.setVisible(true);
        return msgBox;
    }
    
    public static void main(String[] args) {
        MessageBoxWithDetails.showMessageBoxWithDetails(null, 
                "test-test-test-test-test-test-test-test-test test-test-test-test-test-test-test-test-test test-test-test-test-testest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testtest-test test-test-test-test-test-test-test-test-test test-test-test-test-test-testt-test-test-test-test test-test-test-test-test-test-test-test-test", 
                "my title", 
                "some details-some detailssome details-blabla-blabla-blabla",
                JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}
