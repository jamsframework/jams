package lm.controller;

import java.awt.event.*;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ListenersAndObserver {

    private MainController mainController;
    private LOChange lOChange;
    private CRChange crChange;

    

    public ListenersAndObserver(MainController p){
        mainController=p;
        lOChange =new LOChange(mainController);
        crChange =new CRChange(mainController);
        lOChange.addListeners();
        crChange.addMenubarListener();
    }


//    public void AddObserver(){
//        mainController.getMainModel().getMultiReader().addObserver(mainController.getMainView().getMainContentPanel().getContentPane());
//
//    }
    public void AddMainMenuListener(){
        mainController.getMainView().getMainMenuBar().setCloseListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                    System.exit(0);
            }
        });
        mainController.getMainView().getMainMenuBar().setFromResources(new FromResourcesButton());
        mainController.getMainView().getMainMenuBar().setStartListener(new StartAction());
        mainController.getMainView().getMainMenuBar().setSave(new Save());
        mainController.getMainView().getMainMenuBar().setSaveAs(new SaveAs());
        mainController.getMainView().getMainMenuBar().setEmpty(new Empty());

    }
    public void AddFromResourcesListener(){
    }

    public void AddFromResourcesObserver(){
        mainController.getMainModel().getLMConfig().addObserver(mainController.getMainView().getImport_panel2());
    }

    //    Listernes
    class closeListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            
        }
    }

    public class Empty implements ActionListener{
            public void actionPerformed (ActionEvent event){
            try {
                mainController.getMainModel().createSaveModel();
                mainController.getMainModel().createCRSaveModel();
                mainController.getMainModel().setActiv();
            } catch (CloneNotSupportedException ex) {
                
            }
        }
    }

    class FromResourcesButton implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainController.getMainModel().startNewConfig();
            mainController.getMainView().startImportPanel(mainController.getMainModel().getLMConfig());
            addImport_Panel2Listener();
            AddFromResourcesObserver();

        }
    }
    
    public void addImport_Panel2Listener(){
        //Import_Panel added new File Chooser then set the Path in the Model
       mainController.getMainView().getImport_panel2().setActionListener_choose(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                    JFileChooser path_dir=new JFileChooser();
                    path_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int val = path_dir.showDialog(null,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SELECT PATH"));
                    if(val==path_dir.APPROVE_OPTION){
                            File file =path_dir.getSelectedFile();
                            String path=file.getAbsolutePath();
                            mainController.getMainModel().getLMConfig().setNewPath(path);
                        
                    }
            }
       });
       mainController.getMainView().getImport_panel2().setActionListerToChooseSpecial(new ActionListener(){
           public void actionPerformed(ActionEvent event ){
               String way = event.getActionCommand();
               JFileChooser path_dir=new JFileChooser();
               path_dir.setFileSelectionMode(JFileChooser.FILES_ONLY);
               int val = path_dir.showDialog(null,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SELECT File"));
               if(val==path_dir.APPROVE_OPTION){
                   File file =path_dir.getSelectedFile();
                   String path=file.getAbsolutePath();
                   if(way == "crop"){
                       mainController.getMainModel().getLMConfig().checkCropPath(path,false);
                   }
                   if(way == "croprotation"){
                        mainController.getMainModel().getLMConfig().checkCropRotationPath(path,false);
                   }
                   if(way == "till"){
                        mainController.getMainModel().getLMConfig().checkTillPath(path,false);
                   }
                   if(way == "fert"){
                        mainController.getMainModel().getLMConfig().checkFertPath(path,false);
                   }
                   if(way == "lmarable"){
                        mainController.getMainModel().getLMConfig().checkLmArablePath(path,false);
                   }
                   if(way == "landuse"){
                        mainController.getMainModel().getLMConfig().checkLandusePath(path,false);
                   }
                   if(way == "hru_rot_acker"){
                        mainController.getMainModel().getLMConfig().checkHru_rot_ackerPath(path,false);
                   }
               }
           }
       });
       
    mainController.getMainView().getImport_panel2().setKeyListenerToTextfields(new KeyListener(){

            public void keyTyped(KeyEvent e) {
                
            }

            public void keyPressed(KeyEvent e) {
                
            }

            public void keyReleased(KeyEvent e) {
                ((JTextField)e.getSource()).postActionEvent();
            }
        
    });
    
    mainController.getMainView().getImport_panel2().setActionListener_textfield(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                String way = e.getActionCommand();
                String path= ((JTextField)e.getSource()).getText();
                
                if(way == "crop"){
                       mainController.getMainModel().getLMConfig().checkCropFromKey(path);
                   }
                   if(way == "croprotation"){
                        mainController.getMainModel().getLMConfig().checkCropRotationFromKey(path);
                   }
                   if(way == "till"){
                        mainController.getMainModel().getLMConfig().checkTillFromKey(path);
                   }
                   if(way == "fert"){
                        mainController.getMainModel().getLMConfig().checkFertFromKey(path);
                   }
                   if(way == "lmarable"){
                        mainController.getMainModel().getLMConfig().checkLmArableFromKey(path);
                   }
                   if(way == "landuse"){
                        mainController.getMainModel().getLMConfig().checkLanduseFromKey(path);
                   }
                   if(way == "hru_rot_acker"){
                        mainController.getMainModel().getLMConfig().checkHru_rot_ackerFromKey(path);
                   }
            }
        
    });
    mainController.getMainView().getImport_panel2().setKeyListenerToPathField(new KeyListener(){

            public void keyTyped(KeyEvent e) {
                
            }

            public void keyPressed(KeyEvent e) {
                
            }

            public void keyReleased(KeyEvent e) {
                ((JTextField)e.getSource()).postActionEvent();
            }
        
    });
    
    mainController.getMainView().getImport_panel2().setActionListenerToPathField(new ActionListener (){

            public void actionPerformed(ActionEvent e) {
                String s=((JTextField)e.getSource()).getText();
                mainController.getMainModel().getLMConfig().setNewPath(s);
            }
        
    });
    
    mainController.getMainView().getImport_panel2().setActionListener_import(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                    if(mainController.getMainModel().getLMConfig().checkIfAllRight()){
                        try{
                            mainController.getMainModel().startNewModel();
                            mainController.getMainModel().getMultiReader().setPaths(mainController.getMainModel().getLMConfig().getAllPaths());
                            mainController.getMainModel().getMultiReader().createDefaultModel();
                            mainController.getMainModel().createSaveModel();
                            mainController.getMainModel().cancelCRSaveModel();
                            mainController.getMainModel().getLMConfig().saveHeader();
                        } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(ListenersAndObserver.class.getName()).log(Level.SEVERE, null, ex);
                }
                            mainController.getMainView().getImport_panel2().close();
                    }else{
                            mainController.getMainView().getDialogs().showNotAllPathsKorrekt();
                    }
            }
        
    });
    
    }
    
    class StartAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            if(mainController.getMainModel().getMultiReader().isEmpty()){
                mainController.getMainView().getDialogs().showAbortDialog(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("ES IST NOCH KEIN MODEL GELADEN"));
            }else{
                mainController.getMainView().getMainContentPanel().getContentPane().setDefaultModel(mainController.getMainModel().getMultiReader());
                mainController.getMainView().getMainContentPanel().getContentPane().setTextArea();
            }
        }
    }

    public class Save implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainController.getMainModel().save();
        }
    }
    public class SaveAs implements ActionListener{
        public void actionPerformed(ActionEvent e){
            String Path =null;
            JFileChooser path_dir=new JFileChooser();
            path_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val = path_dir.showDialog(null,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SELECT PATH"));
            if(val==path_dir.APPROVE_OPTION){
                   File file =path_dir.getSelectedFile();
                   Path=file.getAbsolutePath();
            }
            mainController.getMainModel().save(Path);
        }
    }
    
    public LOChange getLOChange(){
        return lOChange;
    }

}
