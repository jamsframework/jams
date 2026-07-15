/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.server.client.gui;

import jams.JAMS;
import jams.server.entities.File;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.StringTools;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author christian
 */
public class FilePropertiesDlg extends JDialog{
    JLabel fileIconLabel = new JLabel("");
    JLabel fileNameLabel = new JLabel("                 ");
    
    JLabel fileTypeLabel = new JLabel("                 ");
    JLabel fileLocationLabel = new JLabel("                  ");
    JLabel fileSizeLabel = new JLabel("                 ");
    
    JLabel fileCreatedLabel = new JLabel("                ");
    
    JLabel typeLabel = new JLabel(JAMS.i18n("Type:"), JLabel.LEFT);
    JLabel locationLabel = new JLabel(JAMS.i18n("Location:"), JLabel.LEFT);
    JLabel sizeLabel = new JLabel(JAMS.i18n("Size:"), JLabel.LEFT);
    JLabel creationLabel = new JLabel(JAMS.i18n("Created:"), JLabel.LEFT);
    JLabel roleLabel = new JLabel(JAMS.i18n("Role:"), JLabel.LEFT);
    
    final String INPUT_FILE = JAMS.i18n("input_file");
    final String OUTPUT_FILE = JAMS.i18n("output_file");
    final String MODEL_FILE = JAMS.i18n("model_file");
    final String CONF_FILE = JAMS.i18n("configuration_file");
    final String OTHER_FILE = JAMS.i18n("other/unknown");
    final String COMP_LIB = JAMS.i18n("component_library");
    final String RUNTIME_LIB = JAMS.i18n("runtime_library");
    final String EXEC_FILE = JAMS.i18n("executable_file");
    final String JAP_FILE = JAMS.i18n("JAP_file");
            
    JComboBox fileRoleCombo = new JComboBox(new String[]{
                INPUT_FILE, 
                OUTPUT_FILE, 
                MODEL_FILE, 
                CONF_FILE, 
                OTHER_FILE, 
                COMP_LIB,
                RUNTIME_LIB,
                EXEC_FILE,
                JAP_FILE});
    
    JButton okButton = new JButton(JAMS.i18n("Ok"));    
    JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
    JButton applyButton = new JButton(JAMS.i18n("Apply"));
            
    WorkspaceFileAssociation wfa;
    SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd, yyyy hh:mm:ss X");
    
    /**
     *
     * @param w
     */
    public FilePropertiesDlg(Window w){
        super(w);
        
        createGUI();
        setupActions();
        
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
            
    /**
     *
     * @return
     */
    public WorkspaceFileAssociation getFile(){
        return wfa;
    }
    
    /**
     *
     * @param wfa
     */
    public void setFile(WorkspaceFileAssociation wfa){
        this.wfa = wfa;
        
        fileTypeLabel.setText(wfa.getFileExtension());                
        fileNameLabel.setText(wfa.getFileName());
        fileLocationLabel.setText(wfa.getFileDirectory());       
        fileSizeLabel.setText(StringTools.humanReadableByteCount(
                wfa.getFile().getFileSize(), false));
        fileCreatedLabel.setText(sdf.format(wfa.getFile().getCreationDate()));
        
        applyButton.setEnabled(false);
        
        fileRoleCombo.setSelectedItem(roleToString(wfa.getRole()));
        
        this.setTitle("Properties of " + wfa.getFileName());
        this.invalidate();
        this.pack();
    }
    
    private String roleToString(int role){
        switch(role){
            case WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY: return COMP_LIB;
            case WorkspaceFileAssociation.ROLE_CONFIG: return CONF_FILE;
            case WorkspaceFileAssociation.ROLE_EXECUTABLE: return EXEC_FILE;
            case WorkspaceFileAssociation.ROLE_INPUT: return INPUT_FILE;
            case WorkspaceFileAssociation.ROLE_JAPFILE: return JAP_FILE;
            case WorkspaceFileAssociation.ROLE_MODEL: return MODEL_FILE;
            case WorkspaceFileAssociation.ROLE_OTHER: return OTHER_FILE;
            case WorkspaceFileAssociation.ROLE_OUTPUT: return OUTPUT_FILE;        
            case WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY: return RUNTIME_LIB;        
            default: return OUTPUT_FILE;
        }       
    }
    
    private int stringToRole(String s) {               
        if (s.compareTo(COMP_LIB)==0){
            return WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY;
        }else if (s.compareTo(CONF_FILE)==0){
            return WorkspaceFileAssociation.ROLE_CONFIG;
        }else if (s.compareTo(EXEC_FILE)==0){
            return WorkspaceFileAssociation.ROLE_EXECUTABLE;
        }else if (s.compareTo(INPUT_FILE)==0){
            return WorkspaceFileAssociation.ROLE_INPUT;
        }else if (s.compareTo(JAP_FILE)==0){
            return WorkspaceFileAssociation.ROLE_JAPFILE;
        }else if (s.compareTo(MODEL_FILE)==0){
            return WorkspaceFileAssociation.ROLE_MODEL;
        }else if (s.compareTo(OTHER_FILE)==0){
            return WorkspaceFileAssociation.ROLE_OTHER;
        }else if (s.compareTo(OUTPUT_FILE)==0){
            return WorkspaceFileAssociation.ROLE_OUTPUT;
        }else if (s.compareTo(RUNTIME_LIB)==0){
            return WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY;
        }        
        return WorkspaceFileAssociation.ROLE_OTHER;
    }
    
    private void setupActions(){
        this.fileRoleCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    int oldValue = wfa.getRole();
                    String item = fileRoleCombo.getSelectedItem().toString();
                    int newValue = stringToRole(item);
                    if (newValue != oldValue) {
                        applyButton.setEnabled(true);
                    }else{
                        applyButton.setEnabled(false);
                    }
                }
            }
        });
        
        this.applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                                                        
                int oldValue = wfa.getRole();
                String item = fileRoleCombo.getSelectedItem().toString();
                int newValue = stringToRole(item);
                                     
                if (newValue != oldValue) {
                    PropertyChangeListener listeners[] = FilePropertiesDlg.this.getPropertyChangeListeners("ROLE");
                    for (PropertyChangeListener pcl : listeners) {
                        pcl.propertyChange(new PropertyChangeEvent(wfa, "ROLE", oldValue, newValue));                        
                    }
                }              
                wfa.setRole(newValue);
                applyButton.setEnabled(false);
            }
        });
        
        this.okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                int oldValue = wfa.getRole();
                String item = fileRoleCombo.getSelectedItem().toString();
                int newValue = stringToRole(item);
                                
                if (newValue != oldValue) {
                    PropertyChangeListener listeners[] = FilePropertiesDlg.this.getPropertyChangeListeners("ROLE");
                    for (PropertyChangeListener pcl : listeners) {
                        pcl.propertyChange(new PropertyChangeEvent(wfa, "ROLE", oldValue, newValue));
                    }
                }   
                wfa.setRole(newValue);
                setVisible(false);
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }
    
    private GridBagConstraints modifyConstrain(GridBagConstraints c, int gridx, int gridy, double weightx, double weighty){
        c.gridx = gridx;
        c.gridy = gridy;
        c.weightx = weightx;
        c.weighty = weighty;
        
        return c;
    }
    
    private void createGUI(){        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JTabbedPane tabPane = new JTabbedPane();        
        mainPanel.add(tabPane, BorderLayout.CENTER);
        this.add(mainPanel);
        
        JPanel generalPanel = new JPanel();
        tabPane.addTab(JAMS.i18n("General"), generalPanel);
        
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
                                
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        
        GridBagLayout layout1 = new GridBagLayout();
        GridBagLayout layout2 = new GridBagLayout();
        GridBagLayout layout3 = new GridBagLayout();
        
        panel1.setLayout(layout1);
        panel2.setLayout(layout2);
        panel3.setLayout(layout3);
        
        generalPanel.add(panel1);
        generalPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        generalPanel.add(panel2);
        generalPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        generalPanel.add(panel3);
                
        fileIconLabel.setIcon(UIManager.getIcon("Tree.leafIcon"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 50;
        c.ipady = 10;

        panel1.add(fileIconLabel, modifyConstrain(c,0,0,1,1));
        panel1.add(fileNameLabel, modifyConstrain(c,1,0,3,1));
        
        panel2.add(typeLabel, modifyConstrain(c,0,0,1,1));
        panel2.add(fileTypeLabel, modifyConstrain(c,1,0,3,1));
        panel2.add(locationLabel, modifyConstrain(c,0,1,1,1));
        panel2.add(fileLocationLabel, modifyConstrain(c,1,1,5,1));
        
        panel2.add(sizeLabel, modifyConstrain(c,0,2,1,1));
        panel2.add(fileSizeLabel, modifyConstrain(c,1,2,3,1));
        panel2.add(creationLabel, modifyConstrain(c,0,3,1,1));
        panel2.add(fileCreatedLabel, modifyConstrain(c,1,3,3,1));
                
        panel3.add(roleLabel, modifyConstrain(c,0,0,1,1));
        c.insets = new Insets(10, 0, 10, 25);
        panel3.add(fileRoleCombo, modifyConstrain(c,1,0,3,1));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        this.setResizable(false);
        this.pack();        
    }
            
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        WorkspaceFileAssociation wfa = new WorkspaceFileAssociation();
        File f = new File();
        f.setCreationDate(new Date());
        f.setFileSize(10000434325L);
        f.setId(1);
        f.setLocation("C:/testdrive/test/xxxy.dat");
        wfa.setFile(f);
        wfa.setPath("C:/testdrive/test/xxxy.dat");
        wfa.setRole(WorkspaceFileAssociation.ROLE_EXECUTABLE);
        
        FilePropertiesDlg fpd = new FilePropertiesDlg((Window)null);
        fpd.setFile(wfa);
        fpd.setVisible(true);
    }
}
