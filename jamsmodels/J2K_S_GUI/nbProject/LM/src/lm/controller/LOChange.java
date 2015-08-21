package lm.controller;


import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lm.model.CropModel;
import lm.model.FertModel;
import lm.model.SaveModel;
import lm.model.TillModel;
import lm.view.ChangeLMArable;
import lm.view.MainContentPanel;
import lm.view.MainView;
import lm.view.changeLMArable.MainContentChange;
import lm.view.miniComponets.Tooltipp;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class LOChange {

    private MainController mainController;
    private SaveModel saveModel;
    private CropModel cropModel;
    private TillModel tillModel;
    private FertModel fertModel;
    private ChangeLMArable changeLMArable;
    private MainContentChange mainContentChange;
    private Conflict_controller conflict_controller;
    private MainView mainView;



    public LOChange(MainController p){
        mainController=p;
        saveModel=mainController.getMainModel().getSaveModel();
        conflict_controller= new Conflict_controller(mainController);
        mainView=mainController.getMainView();
        cropModel=mainController.getMainModel().getCropModel();
        tillModel=mainController.getMainModel().getTillModel();
        fertModel=mainController.getMainModel().getFertModel();
        
        
    }

    public void addListeners(){
        mainController.getMainView().getMainMenuBar().setChangeLMArable(new change());
        mainController.getMainView().getMainMenuBar().setChangeCrop(new changeCrop());
        mainController.getMainView().getMainMenuBar().setChangeTill(new changeTill());
        mainController.getMainView().getMainMenuBar().setChangeFert(new changeFert());
//        mainController.getMainView().getMainChange().getMaincontentchange().addlmArableIDStepListListener(new selectIDStep());
        }
    public void addPanelListener(){
        addLMArablePanelListener();
        addLMArableHeadListener();
    }

    public void addMainContentChangeListener(){
      //ID List Listener
      mainContentChange.addLmArableIDListListener(new ListSelectionListener(){
          public void valueChanged(ListSelectionEvent listSelectionEvent){
                if(mainContentChange.getLmArableIDList().getSelectedIndex()!=-1){
                int index= mainContentChange.getlmArableIDListPosition();
                
                    /*
                     *
                     * Hier Bitte die nächsten 3 Zeilen noch zusammen fassen und nur übergeben was gesetzt werden soll
                     *
                     *
                     */
                    mainContentChange.setContent(index);
//                        mainContentChange.getLmArableHead().setColor(saveModel.getMultiModel().getLMArable().get(index+1).getColor());
//                        mainContentChange.setlmArablePanel(saveModel.getArrayLMArable(index+1),saveModel.getTIDBoxModel(),saveModel.getFIDBoxModel());
//                        mainContentChange.getLmArableHead().setLMArableID(saveModel.getLMArableID(index+1));
//               //         mainContentChange.getLmArableHead().setSelectedID(saveModel.getMultiModel().getLMArable().get(index+1).getCropID());
                    addLMArablePanelListener();
                    addLMArableHeadListener();
                   
                }
           }
      });
      //Add ID Listener
      mainContentChange.addAddIDListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
              saveModel.AddLMArableID(mainContentChange.getlmArableIDListPosition());
          }
      });
      //Delete ID Listener
      mainContentChange.addDeleteIDListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
             // saveModel.DeleteAndReorderLMArableID(mainContentChange.getlmArableIDListPosition());
              conflict_controller.start_delete_LM_ID_conflict(saveModel.before_delete_LMArableID(mainContentChange.getlmArableIDListPosition()));
          }
      });
      //Save Listener
      mainContentChange.addSaveListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
              changeLMArable.setVisible(false);
                try {
                    mainController.getMainModel().saveSaveModel();
                } catch (CloneNotSupportedException ex) {
                }
             }
      });
      //Cancel Listener
      mainContentChange.addCancelListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
              changeLMArable.setVisible(false);
                try {
                    mainController.getMainModel().cancelSaveModel();
                } catch (CloneNotSupportedException ex) {
                }
             }
      });
       

    }
    public void addLMArableHeadListener(){

        class colorButtonListener implements ActionListener{
            public void actionPerformed(ActionEvent event){
                int SearchRow=mainContentChange.getlmArableIDListPosition();
                mainContentChange.getLmArableHead().ShowAndGetColor();
                saveModel.setColor(SearchRow, mainContentChange.getLmArableHead().getColor());
            }
        }
        //Hinzufügen des CIDEdit Listeners
        mainContentChange.getLmArableHead().addChangeIDActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                int SearchRow=mainContentChange.getlmArableIDListPosition();
                mainView.showcropPanel();
            }
        });
        //Hinzufügen des ColorButton Listener
        mainContentChange.getLmArableHead().addColorButtonListener(new colorButtonListener());




    }

    public void addLMArablePanelListener(){
        //Hinzufügen der TIDEDit Listeners
        for(int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
            mainContentChange.getLmArablePanel().getViewList().get(i).addTIDEditListener(new ActionListener(){
                         public void actionPerformed(ActionEvent event){
                        int SearchRow=mainContentChange.getlmArableIDListPosition();
                        int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                        mainView.showtillPanel();



                        if(saveModel.getLMArableVector(SearchRow, SearchStep).getTID()!=0){
                            Object[]arr=saveModel.getTIDListModel().toArray();
                             String sA[] = Arrays.copyOf(arr, arr.length, String[].class);;
                            for( int i=0;i<sA.length;i++){
                                if(sA[i].startsWith(saveModel.getLMArableVector(SearchRow, SearchStep).getTID()+ "")){
                                    mainView.getTillPanel().getTillJList().setSelectedIndex(i);
                                    break;
                                }
                            }
                        }else{
                            mainView.getTillPanel().getTillJList().setSelectedIndex(0);
                        }
                }
            });

        }
        //Hinzufügen der FIDEDIT Listener
         for(int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
            mainContentChange.getLmArablePanel().getViewList().get(i).addFIDEditListener(new ActionListener(){
                public void actionPerformed(ActionEvent event ){
                        int SearchRow=mainContentChange.getlmArableIDListPosition();
                        int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                        mainView.showfertPanel();

                         if(saveModel.getLMArableVector(SearchRow, SearchStep).getFID()!=0){
                            Object[]arr=saveModel.getFIDListModel().toArray();
                             String sA[] = Arrays.copyOf(arr, arr.length, String[].class);
                            for( int i=0;i<sA.length;i++){
                                if(sA[i].startsWith(saveModel.getLMArableVector(SearchRow, SearchStep).getFID()+ "")){
                                    mainView.getFertPanel().getFertJList().setSelectedIndex(i);
                                    break;
                                }
                            }
                        }else{
                            mainView.getFertPanel().getFertJList().setSelectedIndex(0);
                        }
                }
            });
        }
        //Hinzufügen der delete Listeners
        for(int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
                mainContentChange.getLmArablePanel().getViewList().get(i).addDeleteListener(new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                         int SearchRow=mainContentChange.getlmArableIDListPosition();
                         int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                         saveModel.DeleteAndReorderLMArableStep(SearchRow,SearchStep);
                         addLMArablePanelListener();
                         addLMArableHeadListener();
                    }
                });
     }
        //Hinzufügen der Add Listeners
        for(int i=0;i<mainContentChange.getLmArablePanel().getAddSteps().size();i++){
            mainContentChange.getLmArablePanel().getAddSteps().get(i).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int SearchRow=mainContentChange.getlmArableIDListPosition();
                    
                    int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                    saveModel.AddAndReorderLMArableStep(SearchRow, SearchStep);
                    addLMArablePanelListener();
                    addLMArableHeadListener();
                }
            });
        }



    //Hinzufügen der FRACHARVFieldListener
      for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
          mainContentChange.getLmArablePanel().getViewList().get(i).addFRACHARVFieldKeyListener(new KeyListener(){

                public void keyTyped(KeyEvent ke) {
                }
                public void keyPressed(KeyEvent ke) { 
                }
                public void keyReleased(KeyEvent ke) {
                    ((JTextField)ke.getSource()).postActionEvent();
                }   
          });
          mainContentChange.getLmArablePanel().getViewList().get(i).addFRACHARVFieldActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int Step= Integer.parseInt(event.getActionCommand())+1;
                    int ID = mainContentChange.getlmArableIDListPosition();
                    String s=mainContentChange.getLmArablePanel().getViewList().get(Step-1).getFRACHARVField().getText();
                    Double input=null;
                    try{
                        if(s.isEmpty()){
                            input=null;
                        }else{
                                input = Double.valueOf(s);

                        }

                        if(input!=null){
                                saveModel.getLMArableVector(ID, Step).setFRACHARV(input);
                        }else{
                                saveModel.getLMArableVector(ID,Step).setFRACHARV(null);
                        }

                    }catch(NumberFormatException nfe){
                            mainController.getMainView().getDialogs().showNNDialog();
                            mainContentChange.getLmArablePanel().getViewList().get(Step-1).getFRACHARVField().setText(s.substring(0, s.length()-1));
                        }
                 //  Double Min =Double.valueOf(java.util.ResourceBundle.getBundle("lm/properties/MinMax").getString("FRACHARV_MIN"));
                 //   Double Max =Double.valueOf(java.util.ResourceBundle.getBundle("lm/properties/MinMax").getString("FRACHARV_MAX"));

                }

          });
        }
        //Hinzufügen der Date Listener
        for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){

          mainContentChange.getLmArablePanel().getViewList().get(i).addDateFieldActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int step= Integer.parseInt(event.getActionCommand())+1;
                    int iD = mainContentChange.getlmArableIDListPosition();
                    int date=mainContentChange.getLmArablePanel().getViewList().get(step-1).getDate();
                    conflict_controller.start_change_duration_LM_ID_conflict(saveModel.changeDate(iD, step, date));
                    mainContentChange.Reselect();
                    addLMArablePanelListener();
                    addLMArableHeadListener();
                    
                    
                }

          });
        }
        //Hinzufügen Year Listener
        for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
            mainContentChange.getLmArablePanel().getViewList().get(i).addYearActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int step= Integer.parseInt(event.getActionCommand())+1;
                    int iD = mainContentChange.getlmArableIDListPosition();
                    int year=mainContentChange.getLmArablePanel().getViewList().get(step-1).getYear();
                    if(saveModel.checkIfYearChangeOK(iD, step, year)){
                        saveModel.ChangeAYear(iD, step, year);
                        addLMArablePanelListener();
                        addLMArableHeadListener();
                    }else{
                        mainContentChange.Reselect();
                        mainController.getMainView().getDialogs().showWrongYear();
                    }
                }
            });
        }
        //Hinzufügen der FAMount Listener
        for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
                 mainContentChange.getLmArablePanel().getViewList().get(i).addFAMountFieldKeyListener(new KeyListener(){

                public void keyTyped(KeyEvent ke) {
                }
                public void keyPressed(KeyEvent ke) {
                }
                public void keyReleased(KeyEvent ke) {
                    ((JTextField)ke.getSource()).postActionEvent();
                }
          });
          mainContentChange.getLmArablePanel().getViewList().get(i).addFAMountFieldActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int Step= Integer.parseInt(event.getActionCommand())+1;
                    int ID = mainContentChange.getlmArableIDListPosition();
                    String s=mainContentChange.getLmArablePanel().getViewList().get(Step-1).getFAMountField().getText();
                    Double input;
                    if(s.isEmpty()){
                        input=null;
                    }else{
                        input = Double.valueOf(s);
                    }
                    if(input!=null){
                            saveModel.getLMArableVector(ID, Step).setFAmount(input);
                    }else{
                            saveModel.getLMArableVector(ID,Step).setFAmount(null);
                    }
                    //Neues Berechnen des Nitrogen und Phosphorus Gehalts
                    saveModel.calculateNitrogen(null,ID);
                    saveModel.calculatePhosphorus(null,ID);
                }

          });

        }
        // hinzufügen der PLANT And HARVEST Listener
        for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
            mainContentChange.getLmArablePanel().getViewList().get(i).addPLANTBOXActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent event) {
                    int ID = mainContentChange.getlmArableIDListPosition();
                    int step =Integer.parseInt(event.getActionCommand())+1;
                    Boolean b= mainContentChange.getLmArablePanel().getViewList().get(step-1).isPLANT();
                    saveModel.getLMArableVector(ID, step).setPLANT(b);

                    mainContentChange.getLmArablePanel().getViewList().get(step-1).setHARVESTVisible(!b);
                }

            });
            mainContentChange.getLmArablePanel().getViewList().get(i).addHARVESTBOXActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent event) {
                    int ID = mainContentChange.getlmArableIDListPosition();
                    int step =Integer.parseInt(event.getActionCommand())+1;
                    Boolean b= mainContentChange.getLmArablePanel().getViewList().get(step-1).isHARVEST();
                    saveModel.getLMArableVector(ID, step).setHARVEST(b);
                    mainContentChange.getLmArablePanel().getViewList().get(step-1).setPLANTVisible(!b);
                }

            });
        }
        //Hinzufügen der TID und FID Listener
        for( int i=0;i<mainContentChange.getLmArablePanel().getViewList().size();i++){
            mainContentChange.getLmArablePanel().getViewList().get(i).addTIDListener(new ActionListener (){

                public void actionPerformed(ActionEvent ae) {
                    int ID = mainContentChange.getlmArableIDListPosition();
                    int step=Integer.parseInt(ae.getActionCommand())+1;
                    Integer input=mainContentChange.getLmArablePanel().getViewList().get(step-1).getTIDContent();
                    if(input!=null){
                        saveModel.getLMArableVector(ID, step).setTID(input);
                        mainContentChange.getLmArablePanel().getViewList().get(step-1).setFIDVisible(false);
                    }else{
                        saveModel.getLMArableVector(ID, step).setTID(null);
                        mainContentChange.getLmArablePanel().getViewList().get(step-1).setFIDVisible(true);
                    }
                }

        });

            mainContentChange.getLmArablePanel().getViewList().get(i).addFIDListener(new ActionListener (){

                public void actionPerformed(ActionEvent ae) {
                    int ID = mainContentChange.getlmArableIDListPosition();
                    int step=Integer.parseInt(ae.getActionCommand())+1;
                    Integer input=mainContentChange.getLmArablePanel().getViewList().get(step-1).getFIDContent();
                    if(input!=null){
                        saveModel.getLMArableVector(ID, step).setFID(input);
                        mainContentChange.getLmArablePanel().getViewList().get(step-1).setTIDVisible(false);
                    }else{
                        saveModel.getLMArableVector(ID, step).setFID(null);
                        mainContentChange.getLmArablePanel().getViewList().get(step-1).setTIDVisible(true);
                    }
                    saveModel.calculateNitrogen(null, ID);
                    saveModel.calculatePhosphorus(null, ID);
                }

        });
        }








    }


    public void addTillPanelListener(){
        //Add TillPanel the DefaultListModel
        mainView.getTillPanel().getTillJList().setModel(tillModel.getTIDListModel());
        //Save Button Listener
         mainView.getTillPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainView.getTillPanel().getTillJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\t")[0]);
                                ArrayList a= mainView.getTillPanel().getInput();
                               // a.set(0,String.valueOf(i));
                               
                                tillModel.setTillVector(i, mainView.getTillPanel().getInput());
                                tillModel.createTIDListModel();
                                tillModel.getTillVector(i).print();
                            }
                      
//                      mainContentChange.Reselect();
//                    int i =mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition();
//                    mainController.getMainView().getMainChange().
//                    mainController.getMainView().getMainChange().getMaincontentchange().setSelectedListElement();

                    }
                });
        //Cancel Button Listener
        mainView.getTillPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainView.getTillPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                tillModel.AddTillVector();
                mainView.getTillPanel().getTillJList().setModel(tillModel.getTIDListModel());
                int i = mainView.getTillPanel().getTillJList().getModel().getSize()-1;
                mainView.getTillPanel().getTillJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainView.getTillPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainView.getTillPanel().getTillJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\n")[0]);
                    int index=mainView.getTillPanel().getTillJList().getSelectedIndex();
                    tillModel.DeleteTillVector(Key, index);
                    mainView.getTillPanel().getTillJList().setModel(tillModel.getTIDListModel());
                }
                mainView.getTillPanel().getTillJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainView.getTillPanel().getTillJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainView.getTillPanel().getTillJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainView.getTillPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\t')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainView.getTillPanel().setInput(tillModel.getTillVector(element));
                    }
                }
            }
        });

    }
    public void addFertPanelListener(){
        //Add TillPanel the DefaultListModel
        mainView.getFertPanel().getFertJList().setModel(saveModel.getFIDListModel());
        //Save Button Listener
         mainView.getFertPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainView.getFertPanel().getFertJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\n")[0]);
                                ArrayList a= mainView.getFertPanel().getInput();
                               // a.set(0,String.valueOf(i));
                                saveModel.setFertVector(i, mainView.getFertPanel().getInput());
                                saveModel.createFIDListModel();
                            }
                            saveModel.createFIDBoxModel();
                    }
                });
        //Cancel Button Listener
        mainView.getFertPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainView.getFertPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                saveModel.AddFertVector();
                mainView.getFertPanel().getFertJList().setModel(saveModel.getFIDListModel());
                int i = mainView.getFertPanel().getFertJList().getModel().getSize()-1;
                mainView.getFertPanel().getFertJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainView.getFertPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainView.getFertPanel().getFertJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\t")[0]);
                    int index=mainView.getFertPanel().getFertJList().getSelectedIndex();
                    saveModel.DeleteFertVector(Key, index);
                    mainView.getFertPanel().getFertJList().setModel(saveModel.getFIDListModel());
                }
                mainView.getFertPanel().getFertJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainView.getFertPanel().getFertJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainView.getFertPanel().getFertJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainView.getFertPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\t')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainView.getFertPanel().setInput(saveModel.getFertVector(element));
                    }
                }
            }
        });

    }
    public void addCropPanelListener(){
        mainView.getCropPanel().getCropJList().setModel(cropModel.getCIDListModel());

         for (JLabel i :mainView.getCropPanel().addListenerToLabels()){
             i.addMouseListener(new MouseListener(){

                public void mouseClicked(MouseEvent e) {
                    
                }

                public void mousePressed(MouseEvent e) {
                    
                }

                public void mouseReleased(MouseEvent e) {
                    
                }

                public void mouseEntered(MouseEvent e) {
                    System.out.println("Test");
                    mainView.getDialogs().showToolTipp((((JLabel)e.getSource()).getText()));
                    
                }

                public void mouseExited(MouseEvent e) {
                    mainView.getDialogs().closeToolTipp();
                }
                 
             });
         }
        //Save Button Listener
         mainView.getCropPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainView.getCropPanel().getCropJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\t")[0]);
                                ArrayList a= mainView.getCropPanel().getInput();
                               // a.set(0,String.valueOf(i));
                                cropModel.setCropVector(i, mainView.getCropPanel().getInput());
                                cropModel.createCIDListModel();
                            }

                    }
                });
        //Cancel Button Listener
        mainView.getCropPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainView.getCropPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                cropModel.AddCropVector();
                mainView.getCropPanel().getCropJList().setModel(cropModel.getCIDListModel());
                int i = mainView.getCropPanel().getCropJList().getModel().getSize()-1;
                mainView.getCropPanel().getCropJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainView.getCropPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainView.getCropPanel().getCropJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\t")[0]);
                    int index=mainView.getCropPanel().getCropJList().getSelectedIndex();
                    cropModel.DeleteCropVector(Key, index);
                    mainView.getCropPanel().getCropJList().setModel(cropModel.getCIDListModel());
                }
                mainView.getCropPanel().getCropJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainView.getCropPanel().getCropJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainView.getCropPanel().getCropJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainView.getCropPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\t')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainView.getCropPanel().setInput(cropModel.getCropVector(element));
                    }
                }
            }
        });
    }

   
    class change implements ActionListener{
        public void actionPerformed(ActionEvent e){
           if(mainController.getMainModel().isActiv()){
                try {
                    mainController.getMainModel().createSaveModel();
                            saveModel=mainController.getMainModel().getSaveModel();
                    mainController.getMainView().ChangeLMArable(saveModel);
                            mainContentChange=mainController.getMainView().getChangeLMArable().getMaincontentchange();
                            changeLMArable=mainController.getMainView().getChangeLMArable();
                    addMainContentChangeListener();
                    saveModel.addObserver(mainContentChange);
                    saveModel.addObserver(mainContentChange.getLmArableHead());
                    saveModel.addObserver(mainContentChange.getLmArable_fact_head());
                    mainContentChange.getLmArableHead().setCIDModel(saveModel.getCIDBoxModel());
                    mainContentChange.setDefaultListModel(saveModel.getLmArableIDModel());
                    saveModel.setLists();
                    //setzen des ersten ListModel
                    
                    
                    //  mainContentChange.setlmArableHead(saveModel.getCIDBoxModel());
                    
                    
                    
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(LOChange.class.getName()).log(Level.SEVERE, null, ex);
                }
           }       
        }
    }
    
    class changeCrop implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainView.showcropPanel();
            try {
                mainController.getMainModel().createCropModel();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(LOChange.class.getName()).log(Level.SEVERE, null, ex);
            }
            cropModel=mainController.getMainModel().getCropModel();
            addCropPanelListener();
        }
    }
    
    class changeTill implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainView.showtillPanel();
            try {
                mainController.getMainModel().createTillModel();
            } catch (CloneNotSupportedException ex) {
                
            }
            tillModel=mainController.getMainModel().getTillModel();
            addTillPanelListener();
        }
    }
    
    class changeFert implements ActionListener{
        public void actionPerformed(ActionEvent e){
            mainView.showfertPanel();
            addFertPanelListener();
        }
    }
    
}
