/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import lm.Componet.errors.Ierror_change_duration_LM_ID;
import lm.Componet.errors.Ierror_delete_LM_ID;
import lm.model.CRSaveModel;
import lm.model.MainModel;
import lm.model.MultiModel;
import lm.view.Conflict.MainConflictView;
import lm.view.MainView;

/**
 *
 * @author MultiMedia
 */
public class Conflict_controller {
    
    MainModel mainModel;
    MainView mainView;
    MainController mainController;
    MainConflictView mainConflictView;
   
    //zwischenspeicher der Errors
    

    Conflict_controller(MainController mainController) {
        this.mainController=mainController;
        mainConflictView=mainController.getMainView().getMainConflictView();
        mainModel=mainController.getMainModel();
        mainView=mainController.getMainView();
    }
    
    // ~~~ Methoden & Listener für Error_Delelete_LM_ID ~~~ //
    
    public void start_delete_LM_ID_conflict(Ierror_delete_LM_ID error){
        mainConflictView.create_conflict_delete_LM_ID(error);
        
        
        //Save Listener
        mainConflictView.getConflict_delete_LM_ID().addSaveListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                mainConflictView.stop_conflict_delete_LM_ID();
                mainModel.createCRFromSaveModel();
                mainModel.getCRSaveModel().completeDecisionDeleteLMID(mainModel.getSaveModel().getError_delete_LM_ID());
                
                mainModel.getSaveModel().DeleteAndReorderLMArableID(mainController.getMainView().getChangeLMArable().getMaincontentchange().getlmArableIDListPosition());
                }
            
        });
        //Cancel Listener
        mainConflictView.getConflict_delete_LM_ID().addCancelListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                mainConflictView.stop_conflict_delete_LM_ID();
                }
            
        });
        //Radio Listener
        mainConflictView.getConflict_delete_LM_ID().addRadioListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                mainModel.getSaveModel().getError_delete_LM_ID().getDecision().set(Integer.parseInt(e.getActionCommand().split("/")[0]),Integer.parseInt(e.getActionCommand().split("/")[1]));
            }
            
        });
        
        
        mainConflictView.start_conflict_delete_LM_ID();
    }
    
    
    // ~~~ Methoden & Listener für Error_Change_Duration_LM_ID ~~~ //
    
     
    public void start_change_duration_LM_ID_conflict(Ierror_change_duration_LM_ID error){
        if(error.completeSmaller()){
              mainModel.createCRFromSaveModel();
              mainModel.getCRSaveModel().completeDecisionChangeDuration(error);
             
              mainModel.getSaveModel().setALMArableID(error.getNewAID());
        }else{    
                mainConflictView.create_change_duration_LM_ID(error);

                mainConflictView.getChange_duration_LM_ID().addDeleteListener(new ActionListener(){
                    public void actionPerformed (ActionEvent e){
                            mainConflictView.stop_conflict_change_duration_LM_ID();
                             mainModel.createCRFromSaveModel();
                             mainModel.getCRSaveModel().completeDecisionChangeDuration(mainModel.getSaveModel().getChange_error());
                             mainModel.getSaveModel().acceptChange_duration();
                    }
                });

                mainConflictView.getChange_duration_LM_ID().addCancelListener(new ActionListener(){
                    public void actionPerformed (ActionEvent e){

                    }
                });

                mainConflictView.getChange_duration_LM_ID().addRadioListener(new ActionListener(){
                    public void actionPerformed (ActionEvent e){

                    }
                });

                mainConflictView.start_change_duration_LM_ID();
        }
     }
     
}
