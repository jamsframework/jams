package lm.model;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import lm.Componet.LMDefaultModel;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.model.DefaultVector.ArableID;
import lm.model.DefaultVector.ArableVector;
import lm.model.DefaultVector.CropVector;
import lm.model.DefaultVector.FertVector;
import lm.model.DefaultVector.TillVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class SaveModel {

    private MultiModel DefaultModel;

    private DefaultListModel lmArableIDModel=new DefaultListModel();
    private DefaultListModel lmArableIDStepModel=new DefaultListModel();

    private ArrayList<String> CIDBoxModel=new ArrayList();
    private ArrayList<String> TIDBoxModel=new ArrayList();
    private ArrayList<String> FIDBoxModel=new ArrayList();

    private DefaultListModel TIDListModel=new DefaultListModel();
    private DefaultListModel FIDListModel=new DefaultListModel();
    private DefaultListModel CIDListModel=new DefaultListModel();

    private ArrayList<LMArableVector> ArrayLMArable =new ArrayList();


    public SaveModel(MultiModel DefaultModel){
        try {
            this.DefaultModel = (MultiModel) DefaultModel.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SaveModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }

    public void setLists(){
      this.createlmArableIDModel();
      this.createTIDBoxModel();
      this.createTIDListModel();
      this.createFIDListModel();
      this.createCIDListModel();
//    this.createCIDBoxModel();
    this.createFIDBoxModel();
    }


    public void createlmArableIDModel(){
        lmArableIDModel.clear();
        int save=0;
        ArableID s=DefaultModel.getLMArable().firstKey();
        while(s!=null){
            LMArableVector Vector =(LMArableVector)DefaultModel.getLMArable().get(s);
            LMArableID ID = (LMArableID)Vector.getAID();
            if(ID.getID()>save){
                lmArableIDModel.addElement(ID.getID() +"   <"+ Vector.getCID().getID()+">   "+ Vector.getCID().getCpnm());
                save=ID.getID();
            }
            s=DefaultModel.getLMArable().higherKey(s);
        }


    }

    public void createCIDBoxModel(){
        CIDBoxModel.add("-");
        Integer i=DefaultModel.getLMCrop().firstKey();
        while(i!=null){
            CIDBoxModel.add(i + "\n<" + DefaultModel.getLMCrop().get(i).getCropname() + ">" );
            i=DefaultModel.getLMCrop().higherKey(i);
        }
    }
    public void createTIDBoxModel(){
        TIDBoxModel.add("-");
        Integer i=DefaultModel.getLMTill().firstKey();
        while(i!=null){
            TIDBoxModel.add(i+"");
            i=DefaultModel.getLMTill().higherKey(i);
        }
    }
    public void createFIDBoxModel(){
        FIDBoxModel.add("-");
        Integer i=DefaultModel.getLMFert().firstKey();
        while(i!=null){
            FIDBoxModel.add(i+"");
            i=DefaultModel.getLMFert().higherKey(i);
        }
    }
    public void createTIDListModel(){
        TIDListModel.clear();
        TIDListModel.addElement("-");
        Integer i =DefaultModel.getLMTill().firstKey();
        while(i!=null){
            TIDListModel.addElement(i + "\n<" + DefaultModel.getLMTill().get(i).gettillnm() + ">" );
            i=DefaultModel.getLMTill().higherKey(i);
        }
    }
    public void createFIDListModel(){
        FIDListModel.clear();
        FIDListModel.addElement("-");
        Integer i =DefaultModel.getLMFert().firstKey();
        while(i!=null){
            FIDListModel.addElement(i+"\t<" + DefaultModel.getLMFert().get(i).getfertnm()+">");
            i=DefaultModel.getLMFert().higherKey(i);
        }
    }
    public void createCIDListModel(){
        CIDListModel.clear();
        CIDListModel.addElement("-");
        Integer i =DefaultModel.getLMCrop().firstKey();
        while(i!=null){
            CIDListModel.addElement(i+"\t<"+DefaultModel.getLMCrop().get(i).getCpnm()+">");
            i=DefaultModel.getLMCrop().higherKey(i);
        }
    }

    public void AddTillVector(){
        DefaultModel.getLMTill().put(DefaultModel.getLMTill().lastKey()+1, new TillVector());
        DefaultModel.getLMTill().get(DefaultModel.getLMTill().lastKey()).setTID(DefaultModel.getLMTill().lastKey());
        TIDListModel.addElement(DefaultModel.getLMTill().lastKey()+"\n<>");
    }
    public void DeleteTillVector(Integer key,int i){
        TIDListModel.removeElementAt(i);
        DefaultModel.getLMTill().remove(key);
    }

    public void AddFertVector(){
        DefaultModel.getLMFert().put(DefaultModel.getLMFert().lastKey()+1,new FertVector());
        DefaultModel.getLMFert().get(DefaultModel.getLMFert().lastKey()).setID(DefaultModel.getLMFert().lastKey());
        FIDListModel.addElement(DefaultModel.getLMFert().lastKey()+"\t<>");
    }
    public void DeleteFertVector(Integer key,int i){
        FIDListModel.removeElementAt(i);
        DefaultModel.getLMFert().remove(key);
    }
    public void AddCropVector(){
        DefaultModel.getLMCrop().put(DefaultModel.getLMCrop().lastKey()+1,new CropVector());
        DefaultModel.getLMCrop().get(DefaultModel.getLMCrop().lastKey()).setID(DefaultModel.getLMCrop().lastKey());
        CIDListModel.addElement(DefaultModel.getLMCrop().lastKey()+"\t<>");
    }
    public void DeleteCropVector(Integer key, int i){
        CIDListModel.removeElementAt(i);
        DefaultModel.getLMCrop().remove(key);
    }

    public void DeleteAndReorderLMArableStep(Integer ID, Integer Step){
        int newMaxStufe=DefaultModel.getLMArable().get(new ArableID(ID,Step)).getAID().getMaxStufe()-1;
        Boolean OneAndLast;
        if(Step!=1||!DefaultModel.getLMArable().get(new ArableID(ID,Step)).getAID().isLast()){
            while(DefaultModel.getLMArable().get(new ArableID(ID,Step+1))!=null){
                DefaultModel.getLMArable().put(new ArableID(ID,Step), DefaultModel.getLMArable().get(new ArableID(ID,Step+1)));
                DefaultModel.getLMArable().get(new ArableID(ID,Step)).setAID(new ArableID(ID,Step));
                Step++;
            }
            Integer i=1;
            while(i<=newMaxStufe){
                DefaultModel.getLMArable().get(new ArableID(ID,i)).getAID().setMaxStufe(newMaxStufe);
                i++;
            }
        }
        //set the new Max Stufe
       
        DefaultModel.getLMArable().remove(new ArableID(ID,Step));
    }
    public void AddAndReorderLMArableStep(Integer ID,Integer Step){
        int newMaxStufe;
        ArableID AID=new ArableID(ID,Step);
        if(DefaultModel.getLMArable().get(new ArableID(ID,1))==null){
            newMaxStufe=1;
        }else{
            newMaxStufe=DefaultModel.getLMArable().get(new ArableID(ID,1)).getAID().getMaxStufe()+1;   
        }
        Step=newMaxStufe;
        while(Step!=AID.getStufe()){
            DefaultModel.getLMArable().put(new ArableID(ID,Step), DefaultModel.getLMArable().get(new ArableID(ID,Step-1)));
            DefaultModel.getLMArable().get(new ArableID(ID,Step)).setAID((new ArableID(ID,Step)));
            Step--;
        }
        DefaultModel.getLMArable().put(AID,new ArableVector());
        DefaultModel.getLMArable().get(AID).setAID(AID);
        Integer i=1;
        while(i<=newMaxStufe){
            DefaultModel.getLMArable().get(new ArableID(ID,i)).getAID().setMaxStufe(newMaxStufe);
            i++;
        }
    }

    public void AddLMArableID(Integer ID){
            ArableID AID=DefaultModel.getLMArable().lastKey();
            //Create New Last Key
            AID=new ArableID(AID.getID()+1,1);
            DefaultModel.getLMArable().put(AID, new ArableVector());
            DefaultModel.getLMArable().get(AID).setAID(new ArableID(AID.getID(),AID.getStufe(),1));
            this.createlmArableIDModel();

    }

    public void DeleteAndReorderLMArableID(Integer ID){
        
       ArableID AID=new ArableID(ID,1);

        //löschen der alten Einträge auf der ID
        for(int i=0;i<=DefaultModel.getLMArable().get(AID).getAID().getMaxStufe();i++){
            DefaultModel.getLMArable().remove(AID);
            AID=DefaultModel.getLMArable().higherKey(AID);
            if(AID.getID()!=ID){
                break;
            }
        }
        //Umsetzen aller anderen ID's
        while(AID!=null){
            DefaultModel.getLMArable().put(new ArableID(AID.getID()-1,AID.getStufe()), DefaultModel.getLMArable().get(AID));
            DefaultModel.getLMArable().get(new ArableID(AID.getID()-1,AID.getStufe())).setAID(new ArableID(AID.getID()-1,AID.getStufe(),DefaultModel.getLMArable().get(AID).getAID().getMaxStufe()));
            System.out.println("alter Key " + AID.toString() + "   neuer KEy" + (AID.getID()-1) +"/" + AID.getStufe());
            DefaultModel.getLMArable().remove(AID);
            AID=DefaultModel.getLMArable().higherKey(AID);
        }
        this.createlmArableIDModel();
    }


    public ArrayList<LMArableVector> getArrayLMArable(int searchRow){

        ArrayLMArable.clear();
        Integer step=1;
        ArableID AID=new ArableID(searchRow,step);
        if(DefaultModel.getLMArable().get(AID)!=null){
            if(DefaultModel.getLMArable().get(AID).getAID().isLast()){
                    ArrayLMArable.add(DefaultModel.getLMArable().get(AID));
                }
            while(!DefaultModel.getLMArable().get(AID).getAID().isLast()){
                ArrayLMArable.add(DefaultModel.getLMArable().get(AID));
                AID=DefaultModel.getLMArable().higherKey(AID);
                if(DefaultModel.getLMArable().get(AID).getAID().isLast()){
                    ArrayLMArable.add(DefaultModel.getLMArable().get(AID));
                }
            }
        }

    return ArrayLMArable;

    }

    public LMArableVector getLMArableVector (int SearchRow, int SearchStep){
        return DefaultModel.getLMArable().get(new ArableID(SearchRow,SearchStep));
    }

    public LMTillVector getTillVector(int ID){
        return (LMTillVector)DefaultModel.getLMTill().get(ID);
    }

    public LMFertVector getFertVector(int ID){
        return (LMFertVector)DefaultModel.getLMFert().get(ID);
    }
    public LMCropVector getCropVector(int ID){
        return DefaultModel.getLMCrop().get(ID);
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

    public MultiModel getMultiModel(){
        return DefaultModel;
    }

    public void setTillVector(Integer index,ArrayList a){
            System.out.println(index  +" " + a);
            DefaultModel.getLMTill().get(index).setAll(a);
    }

    public void setFertVector(Integer index,ArrayList a){
            DefaultModel.getLMFert().get(index).setAll(a);
    }

    public void setCropVector(Integer index,ArrayList a){
            DefaultModel.getLMCrop().get(index).setAll(a);
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

    public DefaultListModel getCIDListModel(){
        return CIDListModel;
    }

}