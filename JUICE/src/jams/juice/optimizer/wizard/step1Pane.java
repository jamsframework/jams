/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.juice.optimizer.wizard;

import jams.JAMSProperties;
import jams.data.JAMSDataFactory;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.w3c.dom.Document;

/**
 *
 * @author Christian Fischer
 */
public class step1Pane extends stepPane {

    final JTextField selectedModelFile = new JTextField(25);
    final JTextField selectedConfigurationFile = new JTextField(25);
    Document loadedModel = null;
    JAMSProperties properties = null;

    @Override
    public String finish() {
        //load model
        String fileName = this.selectedModelFile.getText();
        File file = new File(fileName);

        DocumentLoader loader = new DocumentLoader();
        loader.modelFile = JAMSDataFactory.createString();
        loader.modelFile.setValue(file.getName());
        loader.workspaceDir = JAMSDataFactory.createString();
        loader.workspaceDir.setValue(file.getParent());
        loader.modelDoc = JAMSDataFactory.createDocument();

        String errorString = loader.init_withResponse();
        loadedModel = loader.modelDoc.getValue();
        if (loadedModel == null) {
            return errorString;
        }

        //try to load property values from file        
        properties = JAMSProperties.createProperties();
        try {
            properties.load(selectedConfigurationFile.getText());
        } catch (IOException e) {
            return "Cant find property file, because:" + e.toString();
        } catch (Exception e2) {
            return "Error while loading property file, because: " + e2.toString();
        }
        return null;
    }

    @Override
    public String init() {
        return null;
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public JPanel build() {
        JButton chooseModelFile = new JButton("Open");
        JPanel loadModelFilePanel = new JPanel();
        loadModelFilePanel.add(selectedModelFile);
        loadModelFilePanel.add(chooseModelFile);

        JPanel modelFilePanel = new JPanel(new BorderLayout());
        modelFilePanel.add(new JLabel("Choose a model file:"), BorderLayout.NORTH);
        modelFilePanel.add(loadModelFilePanel, BorderLayout.CENTER);

        JButton chooseConfigurationFile = new JButton("Open");
        JPanel loadConfigurationFilePanel = new JPanel();
        loadConfigurationFilePanel.add(selectedConfigurationFile);
        loadConfigurationFilePanel.add(chooseConfigurationFile);

        JPanel configurationFilePanel = new JPanel(new BorderLayout());
        configurationFilePanel.add(new JLabel("Choose a configuration file:"), BorderLayout.NORTH);
        configurationFilePanel.add(loadConfigurationFilePanel, BorderLayout.CENTER);

        panel.add(modelFilePanel);
        panel.add(configurationFilePanel);

        chooseModelFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose a model file");
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
                selectedModelFile.setText(fileFromDialog.getAbsolutePath());
            }
        });

        chooseConfigurationFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose a model file");
                fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".jap") || f.isDirectory();
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
                selectedConfigurationFile.setText(fileFromDialog.getAbsolutePath());
            }
        });
        return panel;
    }

    public Document getModelDocument() {
        return this.loadedModel;
    }

    public JAMSProperties getSelectedProperties() {
        return this.properties;
    }
}
