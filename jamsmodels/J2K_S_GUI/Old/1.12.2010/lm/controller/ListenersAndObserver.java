/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.controller;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ListenersAndObserver {

    private MainController mainController;

    public ListenersAndObserver(MainController p){
        mainController=p;
    }

    public void AddObserver(){
        mainController.getMainModel().getMultiReader().addObserver(mainController.getMainView().getMainContentPanel().getContentPane());
        mainController.getMainModel().getLMConfig().addObserver(mainController.getMainView().getImport_Panel());

    }
    public void AddListener(){
        mainController.getMainView().getMainMenuBar().setCloseListener(new closeListener());
        mainController.getMainView().getMainMenuBar().setImport_lmArableListener(new import_lmarabel_Listener());
        mainController.getMainView().getMainMenuBar().setImport2(new import2button());
        mainController.getMainView().getImport_Panel().path_dir_button_listener(new SelectPath());
        mainController.getMainView().getImport_Panel().Import_button_Listener(new ImportFile());
        mainController.getMainView().getMainMenuBar().setStartListener(new StartAction());

    }

    //    Listernes
    class closeListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            System.exit(0);
        }
    }
    class import_lmarabel_Listener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            String dir ="";
            JFileChooser chooser = new JFileChooser();
            int n = chooser.showOpenDialog(mainController.getMainView());
                if(n ==JFileChooser.APPROVE_OPTION){
                    File file=chooser.getSelectedFile();
                    dir = file.getAbsolutePath();
                    mainController.getMainModel().getBufferedFileReader().init(dir);
                }

        }
    }

    class SelectPath implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int i =mainController.getMainView().getImport_Panel().getConfigInt();
            String Path =null;
            JFileChooser path_dir=new JFileChooser();
            path_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val = path_dir.showDialog(null,"Select Path");
            if(val==path_dir.APPROVE_OPTION){
                   File file =path_dir.getSelectedFile();
                   Path=file.getAbsolutePath();
                   mainController.getMainModel().getLMConfig().Check(Path);
                   mainController.getMainView().getImport_Panel().setLMConfig(mainController.getMainModel().getLMConfig());
                   mainController.getMainModel().getLMConfig().tellObserver(i);
                   System.out.println("1.  config.Check(Path);");
            }
        }
    };

    class import2button implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainController.getMainView().ImportPanel();

        }
    }

    class ImportFile implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getLmArable())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getTill())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getCrop())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getFert())){
                mainController.getMainView().getImport_Panel().setVisible(false);
                mainController.getMainModel().getMultiReader().setPaths(mainController.getMainView().getImport_Panel().getLmArable(),mainController.getMainView().getImport_Panel().getCrop(),mainController.getMainView().getImport_Panel().getTill(),mainController.getMainView().getImport_Panel().getFert());
                mainController.getMainModel().getMultiReader().createDefaultModel();
                
                
            }else{
                mainController.getMainView().getDialogs().showAbortDialog("Eins oder mehere Files wurden nicht gefunden");
            }

        }
    }
    class StartAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(mainController.getMainModel().getMultiReader().isEmpty()){
                mainController.getMainView().getDialogs().showAbortDialog("Es ist noch kein Model geladen");
            }else{
                mainController.getMainView().getMainContentPanel().getContentPane().setDefaultModel(mainController.getMainModel().getMultiReader());
                mainController.getMainModel().getMultiReader().tellObservers(0);
            }
        }
    }

}
