package lm.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Observable;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import lm.Componet.LMDefaultModel;
import lm.Componet.LMSaveModel;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropRotationVector;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.Componet.errors.Ierror_change_duration_LM_ID;
import lm.Componet.errors.Ierror_delete_LM_ID;
import lm.model.DefaultVector.ArableID;
import lm.model.DefaultVector.ArableVector;
import lm.model.DefaultVector.CropVector;
import lm.model.DefaultVector.FertVector;
import lm.model.DefaultVector.TillVector;
import lm.model.errors.Error_change_duration_LM_ID;
import lm.model.errors.Error_delete_LM_ID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class SaveModel extends Observable implements LMSaveModel {

    private MultiModel defaultModel;

    private DefaultListModel lmArableIDModel=new DefaultListModel();
    private DefaultListModel lmArableIDStepModel=new DefaultListModel();

    private ArrayList<String> CIDBoxModel=new ArrayList();
    private ArrayList<String> TIDBoxModel=new ArrayList();
    private ArrayList<String> FIDBoxModel=new ArrayList();

    private ArrayList<String> yearBoxModel=new ArrayList();
    private ArrayList<String> dateBoxModel=new ArrayList();

    private DefaultListModel TIDListModel=new DefaultListModel();
    private DefaultListModel FIDListModel=new DefaultListModel();
//    private DefaultListModel CIDListModel=new DefaultListModel();

    private ArrayList<LMArableVector> ArrayLMArable =new ArrayList();
    
    
    //Errors
    private Ierror_delete_LM_ID error;
    private Ierror_change_duration_LM_ID change_error;

    public SaveModel(){
            
    }
    
    public void createSaveModel(MultiModel DefaultModel){
            this.defaultModel=DefaultModel;
            this.createFactsinLMArable();
               }

    public void setLists(){
      this.createlmArableIDModel();
      this.createTIDBoxModel();
      this.createTIDListModel();
      this.createFIDListModel();
//      this.createCIDListModel();
      this.createCIDBoxModel();
      this.createFIDBoxModel();
      this.createYearBoxModel();
      createDateBoxModel();
      this.setChanged();
      this.notifyObservers(0);
    }


    public void createlmArableIDModel(){
        lmArableIDModel.clear();
        if(!defaultModel.getLMArable().isEmpty()){
            Integer key=defaultModel.getLMArable().firstKey();
            while(key!=null){
                if(defaultModel.getLMArable().get(key).getCropID()==0){
                    lmArableIDModel.addElement(key +"<"+ "-"+">   ");
                }else{
                    lmArableIDModel.addElement(key +"<"+ defaultModel.getLMArable().get(key).getCropID()+">   "+ defaultModel.getLMCrop().get(defaultModel.getLMArable().get(key).getCropID()).getCpnm());
                }
                key=defaultModel.getLMArable().higherKey(key);
            }
        }
    }

    public void createCIDBoxModel(){
        CIDBoxModel.clear();
        CIDBoxModel.add("-");
        if(!defaultModel.getLMCrop().isEmpty()){
            Integer i=defaultModel.getLMCrop().firstKey();
            while(i!=null){
                CIDBoxModel.add(i + "\n<" + defaultModel.getLMCrop().get(i).getCropname() + ">" );
                i=defaultModel.getLMCrop().higherKey(i);
            }
        }
    }
    public void createTIDBoxModel(){
        TIDBoxModel.clear();
        TIDBoxModel.add("-");
        if(!defaultModel.getLMTill().isEmpty()){
            Integer i=defaultModel.getLMTill().firstKey();
            while(i!=null){
                TIDBoxModel.add(i+"\n<" + defaultModel.getLMTill().get(i).gettillnm()+">");
                i=defaultModel.getLMTill().higherKey(i);
            }
        }
    }
    public void createFIDBoxModel(){
        FIDBoxModel.clear();
        FIDBoxModel.add("-");
        if(!defaultModel.getLMFert().isEmpty()){
            Integer i=defaultModel.getLMFert().firstKey();
            while(i!=null){
                FIDBoxModel.add(i+"\n<"+defaultModel.getLMFert().get(i).getfertnm()+">");
                i=defaultModel.getLMFert().higherKey(i);
            }
        }
    }
    public void createYearBoxModel(){
        yearBoxModel.clear();
        yearBoxModel.add("1");
        yearBoxModel.add("2");
        yearBoxModel.add("3");
        yearBoxModel.add("4");
        yearBoxModel.add("5");
        yearBoxModel.add("6");
        yearBoxModel.add("7");
        yearBoxModel.add("8");
        yearBoxModel.add("9");
        yearBoxModel.add("10");
    }
    public void createDateBoxModel(){
        dateBoxModel.clear();
        for(int i=1;i<=365;i++){
            dateBoxModel.add(i+"");
        }
    }
    public void createTIDListModel(){
        TIDListModel.clear();
        TIDListModel.addElement("-");
        if(!defaultModel.getLMTill().isEmpty()){
            Integer i =defaultModel.getLMTill().firstKey();
            while(i!=null){
                TIDListModel.addElement(i + "\n<" + defaultModel.getLMTill().get(i).gettillnm() + ">" );
                i=defaultModel.getLMTill().higherKey(i);
            }
        }
    }
    public void createFIDListModel(){
        FIDListModel.clear();
        FIDListModel.addElement("-");
        if(!defaultModel.getLMFert().isEmpty()){
            Integer i =defaultModel.getLMFert().firstKey();
            while(i!=null){
                FIDListModel.addElement(i+"\t<" + defaultModel.getLMFert().get(i).getfertnm()+">");
                i=defaultModel.getLMFert().higherKey(i);
            }
        }
    }
//    public void createCIDListModel(){
//        CIDListModel.clear();
//        CIDListModel.addElement("-");
//        if(!defaultModel.getLMCrop().isEmpty()){
//            Integer i =defaultModel.getLMCrop().firstKey();
//            while(i!=null){
//                CIDListModel.addElement(i+"\t<"+defaultModel.getLMCrop().get(i).getCpnm()+">");
//                i=defaultModel.getLMCrop().higherKey(i);
//            }
//        }
//    }

    public void AddTillVector(){
        defaultModel.getLMTill().put(defaultModel.getLMTill().lastKey()+1, new TillVector());
        defaultModel.getLMTill().get(defaultModel.getLMTill().lastKey()).setTID(defaultModel.getLMTill().lastKey());
        TIDListModel.addElement(defaultModel.getLMTill().lastKey()+"\n<>");
    }
    public void DeleteTillVector(Integer key,int i){
        TIDListModel.removeElementAt(i);
        defaultModel.getLMTill().remove(key);
    }

    public void AddFertVector(){
        defaultModel.getLMFert().put(defaultModel.getLMFert().lastKey()+1,new FertVector());
        defaultModel.getLMFert().get(defaultModel.getLMFert().lastKey()).setID(defaultModel.getLMFert().lastKey());
        FIDListModel.addElement(defaultModel.getLMFert().lastKey()+"\t<>");
    }
    public void DeleteFertVector(Integer key,int i){
        FIDListModel.removeElementAt(i);
        defaultModel.getLMFert().remove(key);
    }
    

    public void DeleteAndReorderLMArableStep(Integer ID, Integer Step){
        defaultModel.getLMArable().get(ID).DeleteVector(Step);
        this.setChanged();
        this.notifyObservers();
    }
    public void AddAndReorderLMArableStep(Integer ID,Integer Step){
        defaultModel.getLMArable().get(ID).AddVector(Step);
        this.setChanged();
        this.notifyObservers();
    }

    public void AddLMArableID(Integer ID){        
        defaultModel.getLMArable().put(defaultModel.getLMArable().lastKey()+1, new ArableID(defaultModel.getLMArable().lastKey()+1,0));
        this.createlmArableIDModel();
        this.setChanged();
        this.notifyObservers(0);
    }
    public Error_delete_LM_ID before_delete_LMArableID(Integer ID){
        Error_delete_LM_ID errorzwi = new Error_delete_LM_ID();
        errorzwi.setID(ID);
        if(!defaultModel.getLMCropRotation().isEmpty()){
            Integer key =defaultModel.getLMCropRotation().firstKey();
            while(key!=null){
                if(!defaultModel.getLMCropRotation().get(key).getCRE().isEmpty()){
                    Integer key2=defaultModel.getLMCropRotation().get(key).getCRE().firstKey();
                    while(key2!=null){
                    if(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getArableID()==ID&&!defaultModel.getLMCropRotation().get(key).getCRE().get(key2).isFallow()){
                        errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                        errorzwi.getDecision().add(1);
                        }    
                    key2=defaultModel.getLMCropRotation().get(key).getCRE().higherKey(key2);   
                    }
                }
               key=defaultModel.getLMCropRotation().higherKey(key);    
            }
       }
       this.error=errorzwi;
       return errorzwi;
    }
    
    
    
    
    
    
    

    public void DeleteAndReorderLMArableID(Integer ID){
        Integer key=ID;
//        while(true){
//            if(defaultModel.getLMArable().higherKey(key)!=null){
//                defaultModel.getLMArable().put(key,defaultModel.getLMArable().get(key+1));
//                defaultModel.getLMArable().get(key).setID(key);
//            }else{
//                defaultModel.getLMArable().remove(defaultModel.getLMArable().lastKey());
//                break;
//            }
//            key=defaultModel.getLMArable().higherKey(key);
//        }
        defaultModel.getLMArable().remove(key);
        this.createlmArableIDModel();
        this.setChanged();
        if(!defaultModel.getLMArable().isEmpty()){
              this.notifyObservers(ID-1);
        }else{
              this.notifyObservers();
        }
        
    }
    
    public Error_change_duration_LM_ID before_change_duration(LMArableID new_ , Integer ID){
        Error_change_duration_LM_ID errorzwi = new Error_change_duration_LM_ID();
        errorzwi.setID(new_.getID());
        errorzwi.setNewAID(new_);
        LMArableID old =defaultModel.getLMArable().get(ID);
        if(old.getDuration()!=new_.getDuration()){
            errorzwi.setMinus(old.getRelativeBegin()-new_.getRelativeBegin());
            errorzwi.setPlus(new_.getDuration()-old.getDuration()-errorzwi.getMinus());
            if(!defaultModel.getLMCropRotation().isEmpty()){
            Integer key =defaultModel.getLMCropRotation().firstKey();
            while(key!=null){
                if(!defaultModel.getLMCropRotation().get(key).getCRE().isEmpty()){
                    Integer key2=defaultModel.getLMCropRotation().get(key).getCRE().firstKey();
                    while(key2!=null){
                    if(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getArableID()==ID){
                            if(errorzwi.getMinus()<=0&&errorzwi.getPlus()<=0){
                                errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                                errorzwi.getDecision().add(-1);
                                errorzwi.getPossible().add(true);
                            }else{
                                    if(defaultModel.getLMCropRotation().get(key).getCRE().get(key2-1).getDuration()>=errorzwi.getMinus()){
                                       if(defaultModel.getLMCropRotation().get(key).getCRE().lastKey()==key2){
                                            errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                                            errorzwi.getDecision().add(2);
                                            errorzwi.getPossible().add(true);
                                        }else{
                                            if(defaultModel.getLMCropRotation().get(key).getCRE().get(key2+1).getDuration()>=errorzwi.getPlus()){
                                                errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                                                errorzwi.getDecision().add(2);
                                                errorzwi.getPossible().add(true);
                                            }else{
                                                errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                                                errorzwi.getDecision().add(1);
                                                errorzwi.getPossible().add(false);
                                            }
                                        }
                                    }else{
                                        errorzwi.setArrayListElement(defaultModel.getLMCropRotation().get(key).getCRE().get(key2).getActionCommand());
                                                errorzwi.getDecision().add(1);
                                                errorzwi.getPossible().add(false);
                                    }
                            }
                        }    
                    key2=defaultModel.getLMCropRotation().get(key).getCRE().higherKey(key2);   
                    }
                }
               key=defaultModel.getLMCropRotation().higherKey(key);    
            }
       }
        }
        for(int i=0;i<errorzwi.getDecision().size();i++){
           }
        this.change_error=errorzwi;
        return errorzwi;
    }
    //Berechnet die Stickstoff Menge einer Arable, wird bei jeder Änderung neu berechnet
    //übergeben wird entweder direkt eine LMArable oder der key für diese. Das jeweilige andere ist null
    public void calculateNitrogen(LMArableID aid, Integer id){
        if(aid == null){
            aid = defaultModel.getLMArable().get(id);
        }
        double zwi = 0;
        if(!aid.getTreeMap().isEmpty()){
            Integer key = aid.getTreeMap().firstKey();
            while(key!=null){
                int fid = aid.getTreeMap().get(key).getFID();
                if(fid!=0){
                    double fminn = defaultModel.getLMFert().get(fid).getfminn();
                    double forgn = defaultModel.getLMFert().get(fid).getforgn();      
                    double amount = 0;
                    if(aid.getTreeMap().get(key).getFAmount()!=null){
                        amount = aid.getTreeMap().get(key).getFAmount();
                    }
                    zwi = zwi + (amount * (fminn + forgn));
                }
                key=aid.getTreeMap().higherKey(key);
            }
        }
        zwi = zwi * 100;
        zwi = Math.round(zwi);
        zwi = zwi / 100;
        aid.set_Nitrogen(zwi);
        this.setChanged();
        this.notifyObservers(1);
    }
    //Berechnet die Stickstoff Menge einer Arable, wird bei jeder Änderung neu berechnet
    //übergeben wird entweder direkt eine LMArable oder der key für diese. Das jeweilige andere ist null
    public void calculatePhosphorus(LMArableID aid, Integer id){
        if(aid == null){
            aid = defaultModel.getLMArable().get(id);
        }
        double zwi = 0;
        if(!aid.getTreeMap().isEmpty()){
            Integer key = aid.getTreeMap().firstKey();
            while(key!=null){
                int fid = aid.getTreeMap().get(key).getFID();
                if(fid!=0){
                    double fminp =defaultModel.getLMFert().get(fid).getfminp();
                    double forgp =defaultModel.getLMFert().get(fid).getforgp();
                    double amount = 0;
                    if(aid.getTreeMap().get(key).getFAmount()!=null){
                        amount = aid.getTreeMap().get(key).getFAmount();
                    }
                    zwi= zwi + (amount * (fminp + forgp));
                }
                key=aid.getTreeMap().higherKey(key);
            }
        }
        zwi = zwi * 100;
        zwi = Math.round(zwi);
        zwi = zwi / 100;
        aid.set_Phosphorus(zwi);
        this.setChanged();
        this.notifyObservers(1);
    }
    
    
    public void createFactsinLMArable(){
        if(!defaultModel.getLMArable().isEmpty()){
            Integer key = defaultModel.getLMArable().firstKey();
            while(key!=null){
                calculatePhosphorus(defaultModel.getLMArable().get(key),null);
                calculateNitrogen(defaultModel.getLMArable().get(key),null);
                key=defaultModel.getLMArable().higherKey(key);
            }
        }
    }
    
    public void setALMArableID(LMArableID aid){
        this.defaultModel.getLMArable().remove(aid.getID());
        this.defaultModel.getLMArable().put(aid.getID(), aid);
    }
    
    public void acceptChange_duration (){
        setALMArableID(change_error.getNewAID());
    }
    
    public Error_change_duration_LM_ID changeDate(int id, int step, int date){
        LMArableID aid = defaultModel.getLMArable().get(id).clone();
        if(step==1){
        aid.getTreeMap().get(step).setDate(date);
        
        }else{
            if(aid.getTreeMap().get(step-1).getDate()>date){
                ChangeAYear(aid,step,aid.getTreeMap().get(step).getYear()+1);
                aid.getTreeMap().get(step).setDate(date);
            }else{
                aid.getTreeMap().get(step).setDate(date);
                
            }
        }
        aid.setADate();
        return before_change_duration(aid,id);
        
    }
    
    
    
    public Boolean checkIfYearChangeOK(int id, int step, int year){
        if(step==0||defaultModel.getLMArable().get(id).getTreeMap().get(step).getYear()<=year){
            return true;
        }
        return false;
        
    }

    public void ChangeAYear(int id,int step,int year){
        
        defaultModel.getLMArable().get(id).ChangeAYearAndCheckTheRest(step, year);
        this.setChanged();
        this.notifyObservers();

    }
    public void ChangeAYear(LMArableID aid,int step,int year){
        
        aid.ChangeAYearAndCheckTheRest(step, year);

    }


    public ArrayList<LMArableVector> getArrayLMArable(int searchRow){

        ArrayLMArable.clear();
        LMArableID arableID=defaultModel.getLMArable().get(searchRow);
        if(!arableID.isEmpty()){
            Integer key=(Integer)arableID.getTreeMap().firstKey();
            while(key!=null){
                ArrayLMArable.add((ArableVector)arableID.getTreeMap().get(key));
                key=(Integer)arableID.getTreeMap().higherKey(key);
            }
        }

    return ArrayLMArable;

    }

    public void setColor(Integer ID,Color color){
        defaultModel.getLMArable().get(ID).setColor(color);
    }

    public LMArableVector getLMArableVector (int SearchRow, int SearchStep){
        return (LMArableVector)defaultModel.getLMArable().get(SearchRow).getTreeMap().get(SearchStep);
    }

    public LMTillVector getTillVector(int ID){
        return (LMTillVector)defaultModel.getLMTill().get(ID);
    }

    public LMFertVector getFertVector(int ID){
        return (LMFertVector)defaultModel.getLMFert().get(ID);
    }
    

    /**
     * @return the lmArableIDModel
     */
    public DefaultListModel getLmArableIDModel() {
        return lmArableIDModel;
    }

    /**
     * @return the lmArableIDStepModel
     */
    public DefaultListModel getLmArableIDStepModel() {
        return lmArableIDStepModel;
    }

    /**
     * @return the CIDBoxModel
     */
    public ArrayList getCIDBoxModel() {
        return CIDBoxModel;
    }

    /**
     * @return the TIDBoxModel
     */
    public ArrayList getTIDBoxModel() {
        return TIDBoxModel;
    }

    /**
     * @return the FIDBoxModel
     */
    public ArrayList getFIDBoxModel() {
        return FIDBoxModel;
    }

    public ArrayList getYearBoxModel(){
        return yearBoxModel;
    }
    public ArrayList getDateBoxModel(){
        return dateBoxModel;
    }

    public MultiModel getMultiModel(){
        return defaultModel;
    }
    public void setMultiModel(MultiModel defaultModel){
        this.defaultModel=defaultModel;
    }

    public LMDefaultModel getDefaultModel(){
        return defaultModel;
    }

    public void setTillVector(Integer index,ArrayList a){
            defaultModel.getLMTill().get(index).setAll(a);
    }

    public void setFertVector(Integer index,ArrayList a){
            defaultModel.getLMFert().get(index).setAll(a);
    }

    

    /**
     * @return the TIDListModel
     */
    public DefaultListModel getTIDListModel() {
        return TIDListModel;
    }

    public DefaultListModel getFIDListModel(){
        return FIDListModel;
    }

    

    public LMArableID getLMArableID(int i){
        return defaultModel.getLMArable().get(i);
    }
    public Ierror_delete_LM_ID getError_delete_LM_ID(){
        return error;
    }

    /**
     * @return the change_error
     */
    public Ierror_change_duration_LM_ID getChange_error() {
        return change_error;
    }
}