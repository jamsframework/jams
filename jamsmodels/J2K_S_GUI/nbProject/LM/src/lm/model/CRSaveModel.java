package lm.model;

import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMCropRotationElement;
import lm.Componet.Vector.LMCropRotationVector;
import lm.Componet.errors.Ierror_change_duration_LM_ID;
import lm.Componet.errors.Ierror_delete_LM_ID;
import lm.model.DefaultVector.ArableID;
import lm.model.DefaultVector.CropRotationElement;
import lm.model.DefaultVector.CropRotationVector;
import lm.model.errors.Error_delete_LM_ID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRSaveModel {

    private MultiModel defaultModel;
    private TreeMap<Integer, LMCropRotationVector> treeMap;
    private DefaultListModel arableIDListModel = new DefaultListModel();
    private DefaultListModel cropIDListModel = new DefaultListModel();
    private DefaultComboBoxModel zoomModel =new DefaultComboBoxModel();

    public CRSaveModel(MultiModel mm) {
        defaultModel=mm;
        this.treeMap = mm.getLMCropRotation();
        createArableIDListModel();
        createCropIDListModel();
        createZoomModel();
    }

    public void createArableIDListModel() {
        getArableIDListModel().clear();
        if(!defaultModel.getLMArable().isEmpty()){
            Integer key = getDefaultModel().getLMArable().firstKey();
            getArableIDListModel().addElement("-");
            while (key != null) {
                if (getDefaultModel().getLMArable().get(key).getCropID() == 0) {
                    getArableIDListModel().addElement(key + "\t<" + "-" + ">   ");
                } else {
                    getArableIDListModel().addElement(key + "\t <" + getDefaultModel().getLMArable().get(key).getCropID() + ">   " + getDefaultModel().getLMCrop().get(getDefaultModel().getLMArable().get(key).getCropID()).getCpnm());
                }
                key = getDefaultModel().getLMArable().higherKey(key);
            }
        }
    }

    public void createCropIDListModel() {
        getCropIDListModel().clear();
        if(!treeMap.isEmpty()){
            Integer key = treeMap.firstKey();
            getCropIDListModel().addElement("-");
            while (key != null) {
                getCropIDListModel().addElement(key);
                key = treeMap.higherKey(key);
            }
    }
    }

    public void createZoomModel(){
        zoomModel.addElement("100%");
        zoomModel.addElement("50%");
        zoomModel.addElement("33%");
        zoomModel.addElement("25%");
        zoomModel.addElement("20%");
    }

    public void addElement(Integer key, LMArableID lmArableID,int multiplikator) {
        for(int i=1;i<=multiplikator;i++){
            treeMap.get(key).addElement(lmArableID);
        }
    }


    public void addElementAtPostion(Integer key, int position ,Integer keyForArableID){

       treeMap.get(key).insertElementAtPosition(position, defaultModel.getLMArable().get(keyForArableID));
    }

    public void addVector(Integer key, LMCropRotationVector cropRotationVector,int multiplikator) {

        TreeMap<Integer,LMCropRotationElement>abc=new TreeMap(cropRotationVector.getCRE());
        treeMap.get(key).addVector(abc,multiplikator);
    }

    public void deleteElement(Integer key,Integer element){
        treeMap.get(key).setElementAsFallow(element);
        treeMap.get(key).reduceFallow(element-1);
    }

    public void reduceFallow (Integer key, Integer element){
        treeMap.get(key).reduceFallow(element);
    }

    public void maximizeFallow(Integer key, Integer position, Integer years){
        treeMap.get(key).maximizeFallow(position, years);
    }

    public void insertArableIDLeft(Integer key,Integer position,String ArableID){
        Integer keyForArableID =Integer.parseInt(ArableID.split(" ")[0]);
        treeMap.get(key).insertElementLeftFromPosition(position, defaultModel.getLMArable().get(keyForArableID));
    }

    public void insertArableIDRight(Integer key,Integer position,String ArableID){
        Integer keyForArableID =Integer.parseInt(ArableID.split(" ")[0]);
        treeMap.get(key).insertElementRightFromPosition(position, defaultModel.getLMArable().get(keyForArableID));
    }

    public DefaultListModel getCompleteArableIDModel(){
        DefaultListModel dlm = new DefaultListModel();
        if(!defaultModel.getLMArable().isEmpty()){
            Integer key=defaultModel.getLMArable().firstKey();
            while(key!=null){
                LMArableID aid=defaultModel.getLMArable().get(key);
                dlm.addElement(key +"   <"+ aid.getCropID()+">   "+ getDefaultModel().getLMCrop().get(aid.getCropID()).getCpnm());
                key=defaultModel.getLMArable().higherKey(key);
            }
        }
        return dlm;
    }

    public DefaultListModel createAndGetInsertArableIDModel(Integer crID, Integer element){
  
        LMCropRotationElement cre=treeMap.get(crID).getLMCropRotationElement(element);
        int begin= cre.getBegin();
        int end= cre.getBegin()+cre.getDuration()-1;
        DefaultListModel dlm = new DefaultListModel();
        Integer key = getDefaultModel().getLMArable().firstKey();
            while(key!=null){
                LMArableID aid=defaultModel.getLMArable().get(key);
                int beginFromArableID=aid.getRelativeBegin();
                int endFromArableID=aid.getRelativeBegin()+aid.getDuration()-1;
                
                if(beginFromArableID<begin){
                   beginFromArableID=beginFromArableID+365;
                   endFromArableID=endFromArableID+365; 
                }
                if(endFromArableID<=end && beginFromArableID>=begin){
                    dlm.addElement(key +"   <"+ aid.getCropID()+">   "+ getDefaultModel().getLMCrop().get(aid.getCropID()).getCpnm());
                }
                key=getDefaultModel().getLMArable().higherKey(key);
            }
        return dlm;
    }

    public void deleteRow(Integer key){
        treeMap.remove(key);
    }
    public void addRow(){
        Integer key;
        if(!treeMap.isEmpty()){
            key=treeMap.lastKey()+1;
        }else{
            key=1;
        }
        treeMap.put(key, new CropRotationVector(key));
    }

    public void setAsFallow (Integer crID,Integer id){
       treeMap.get(crID).setElementAsFallow(id);
    }
    
    private void expandACRE (int crID,int position,int minus,int plus){
        if(treeMap.get(crID).getCRE().lastKey()==position){
            treeMap.get(crID).getCRE().get(position-1).setRelativeBeginByCRSaveModel(minus);
        }else{
            treeMap.get(crID).getCRE().get(position-1).setRelativeEndByCRSaveModel(minus);
            treeMap.get(crID).getCRE().get(position+1).setRelativeBeginByCRSaveModel(plus);
        }
    }
    
    public void completeDecisionChangeDuration(Ierror_change_duration_LM_ID error){
        for(int i=0;i<error.getDecision().size();i++){
        }
        for(int i=error.getArrayList().size()-1;i>=0;i--){
            int crID=Integer.parseInt(error.getArrayListElement(i).split("/")[0]);
            int position=Integer.parseInt(error.getArrayListElement(i).split("/")[1]);
            if(error.getDecision().get(i)==-1){
                this.expandACRE(crID, position, error.getMinus(), error.getPlus());
            }
            
            if(error.getDecision().get(i)==0){
                
                 deleteElement(crID,position);
            }
            if(error.getDecision().get(i)==1){
                setAsFallow(crID,position);
            } 
            if(error.getDecision().get(i)==2){
                this.expandACRE(crID, position, error.getMinus(), error.getPlus());
            }   
            
                
                
        }
    }
    
    public void completeDecisionDeleteLMID(Ierror_delete_LM_ID error){
        for(int i=error.getArrayList().size()-1;i>=0;i--){
            int crID=Integer.parseInt(error.getArrayListElement(i).split("/")[0]);
            int position=Integer.parseInt(error.getArrayListElement(i).split("/")[1]);
            if(error.getDecision().get(i).equals(0)){
                deleteElement(crID,position);
            }else{
                setAsFallow(crID,position);
            }
        }
    }

    /**
     * @return the arableIDListModel
     */
    public DefaultListModel getArableIDListModel() {
        return arableIDListModel;
    }

    /**
     * @return the cropIDListModel
     */
    public DefaultListModel getCropIDListModel() {
        return cropIDListModel;
    }

    /**
     * @return the zoomModel
     */
    public DefaultComboBoxModel getZoomModel() {
        return zoomModel;
    }

    /**
     * @return the defaultModel
     */
    public MultiModel getDefaultModel() {
        return defaultModel;
    }
    
    public void setMultiModel(MultiModel defaultModel){
        this.defaultModel=defaultModel;
    }
}
