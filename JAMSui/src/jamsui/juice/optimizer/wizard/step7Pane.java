/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jamsui.juice.optimizer.wizard.step6Pane.OptimizerDescription;
import jams.model.JAMSModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.w3c.dom.Document;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class step7Pane extends stepPane {
    final JTextField selectedOutputFile = new JTextField(25);
    JScrollPane logScroller = null;
    JDialog parent = null;
    JTextArea infoLogField = new JTextArea();
            
    Document doc = null;        
    JAMSModel model = null;
    
    
    boolean removeNotUsedComponents = true;
    boolean removeGUIComponents = true;
    boolean optimizeModelStructure = true;    
    String infoLog = null;
    OptimizerDescription desc;
    
    public String getOutputPath(){
        return this.selectedOutputFile.getText();
    }
        
    public void setDialog(JDialog parent){
        this.parent = parent;
    }
    
    
    public void setModel(Document doc,JAMSModel model){
        this.doc = doc;
        this.model = model;        
    }
    
    public void setInfoLog(String log){
        this.infoLog = log;
    }
    
    @Override
    public String init(){                   
        infoLogField.setPreferredSize(new Dimension(300,200));        
        infoLogField.setAutoscrolls(true);
        infoLogField.setBorder(new LineBorder(Color.BLACK));
        infoLogField.setEditable(true);
        
        infoLogField.append(infoLog);
        infoLogField.invalidate();
        panel.invalidate();        
        return null;
    }
    
    @Override 
    public JPanel getPanel(){
        return panel;
    }
    
    
    
    @Override
    public JPanel build(){
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        /*JButton chooseModelFile = new JButton(JAMS.resources.getString("Save"));        
        JPanel saveModelFilePanel = new JPanel(new GridBagLayout());    
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        saveModelFilePanel.add(selectedOutputFile,c);
        c.gridx = 1;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        saveModelFilePanel.add(chooseModelFile,c);*/
                
        /*JPanel modelFilePanel = new JPanel(new BorderLayout());
        modelFilePanel.add(new JLabel(JAMS.resources.getString("output_file_path")), BorderLayout.NORTH);
        modelFilePanel.add(saveModelFilePanel,BorderLayout.CENTER);

        c.gridx = 0;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        panel.add(modelFilePanel,c);   */
        this.panel.add(new JLabel(JAMS.resources.getString("successfully_finished")));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel("infoLog"),BorderLayout.NORTH);
        logScroller = new JScrollPane();
        logScroller.setViewportView(infoLogField);       
        logScroller.setVisible(true);
        infoPanel.add(logScroller,BorderLayout.SOUTH);
        c.gridx = 0;    c.gridy = 1;    c.fill = GridBagConstraints.NONE;
        panel.add(infoPanel,c);                
                        
/*        chooseModelFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle(JAMS.resources.getString("Choose_a_model_file"));
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".jam") || f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
                    }
                    @Override
                    public String getDescription() {
                        return "model file filter";
                    }
                });

                if (fc.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File fileFromDialog = fc.getSelectedFile();
                selectedOutputFile.setText(fileFromDialog.getAbsolutePath());
            }
        });   */   
        
        infoLogField.setCaretPosition(infoLogField.getDocument().getLength());
        
        return panel;    
    }                
}
