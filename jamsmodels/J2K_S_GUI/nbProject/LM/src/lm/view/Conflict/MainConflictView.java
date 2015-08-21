/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.view.Conflict;

import lm.Componet.errors.Ierror_change_duration_LM_ID;
import lm.Componet.errors.Ierror_delete_LM_ID;

/**
 *
 * @author MultiMedia
 */
public class MainConflictView {
    
   private Conflictmanager_deleteID conflict_delete_LM_ID;
   private Conflictmanager_changeduration change_duration_LM_ID;
    
    
    //ConfictManager für DeleteID
    
    public void create_conflict_delete_LM_ID(Ierror_delete_LM_ID error) {
        conflict_delete_LM_ID = new Conflictmanager_deleteID(error);
    }
    public void start_conflict_delete_LM_ID(){
        conflict_delete_LM_ID.start();
    }
    public Ierror_delete_LM_ID stop_conflict_delete_LM_ID(){
        return conflict_delete_LM_ID.stop();
    }
    public Conflictmanager_deleteID getConflict_delete_LM_ID() {
        return conflict_delete_LM_ID;
    }
    
    
    // ConflictManager für Change Duration
    
    
    public void create_change_duration_LM_ID(Ierror_change_duration_LM_ID error) {
        change_duration_LM_ID = new Conflictmanager_changeduration(error);
    }
    public void start_change_duration_LM_ID(){
        change_duration_LM_ID.start();
    }
    public Ierror_change_duration_LM_ID stop_conflict_change_duration_LM_ID(){
        return change_duration_LM_ID.stop();
    }
    public Conflictmanager_changeduration getChange_duration_LM_ID() {
        return change_duration_LM_ID;
    }
    
    
}
