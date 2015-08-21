package lm.controller;

import java.awt.event.*;
import java.io.File;
import java.util.Properties;
import javax.swing.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ListenersAndObserver {

    private MainController mainController;
    private LOChange lOChange;

    

    public ListenersAndObserver(MainController p){
        mainController=p;
        lOChange =new LOChange(mainController);
        lOChange.addListeners();
    }


    public void AddObserver(){
        mainController.getMainModel().getMultiReader().addObserver(mainController.getMainView().getMainContentPanel().getContentPane());

    }
    public void AddMainMenuListener(){
        mainController.getMainView().getMainMenuBar().setCloseListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                    System.exit(0);
            }
        });
        mainController.getMainView().getMainMenuBar().setFromResources(new FromResourcesButton());
        mainController.getMainView().getMainMenuBar().setStartListener(new StartAction());

    }
    public void AddFromResourcesListener(){
        mainController.getMainView().getImport_Panel().path_dir_button_listener(new SelectPath());
        mainController.getMainView().getImport_Panel().Import_button_Listener(new ImportFile());
    }

    public void AddFromResourcesObserver(){
        mainController.getMainModel().getLMConfig().addObserver(mainController.getMainView().getImport_Panel());
    }

    //    Listernes
    class closeListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            
        }
    }

    class SelectPath implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int i =mainController.getMainView().getImport_Panel().getConfigInt();
            String Path =null;
            JFileChooser path_dir=new JFileChooser();
            path_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val = path_dir.showDialog(null,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SELECT PATH"));
            if(val==path_dir.APPROVE_OPTION){
                   File file =path_dir.getSelectedFile();
                   Path=file.getAbsolutePath();
                   mainController.getMainModel().getLMConfig().Check(Path);
                   mainController.getMainView().getImport_Panel().setLMConfig(mainController.getMainModel().getLMConfig());
                   mainController.getMainModel().getLMConfig().tellObserver(i);
                   System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("1.  CONFIG.CHECK(PATH);"));
            }
        }
    };

    class FromResourcesButton implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainController.getMainView().ImportPanel();
            AddFromResourcesListener();
            AddFromResourcesObserver();

        }
    }
    
    class ImportFile implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getLmArable())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getTill())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getCrop())&&mainController.getMainModel().getLMConfig().exsit(mainController.getMainView().getImport_Panel().getFert())){
                mainController.getMainView().getImport_Panel().setVisible(false);
                mainController.getMainModel().getMultiReader().setPaths(mainController.getMainView().getImport_Panel().getLmArable(),mainController.getMainView().getImport_Panel().getCrop(),mainController.getMainView().getImport_Panel().getTill(),mainController.getMainView().getImport_Panel().getFert(),mainController.getMainView().getImport_Panel().getCropRotation());
                mainController.getMainModel().getMultiReader().createDefaultModel();
                
                
            }else{
                mainController.getMainView().getDialogs().showAbortDialog(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("EINS ODER MEHERE FILES WURDEN NICHT GEFUNDEN"));
            }

        }
    }
    class StartAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(mainController.getMainModel().getMultiReader().isEmpty()){
                mainController.getMainView().getDialogs().showAbortDialog(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("ES IST NOCH KEIN MODEL GELADEN"));
            }else{
                mainController.getMainView().getMainContentPanel().getContentPane().setDefaultModel(mainController.getMainModel().getMultiReader());
                mainController.getMainModel().getMultiReader().tellObservers(0);
            }
        }
    }

}
