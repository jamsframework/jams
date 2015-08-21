package lm.model.DefaultVector;

import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMCropRotationElement;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CropRotationElement implements LMCropRotationElement {

    private LMArableID aid;
    private String actionCommand;
    private int absolutBegin;
    private int absolutEnd;

    public CropRotationElement(LMArableID aid, String actionCommand,int absolutBegin,int absolutEnd){
        this.aid=aid;
        this.actionCommand=actionCommand;
        this.absolutBegin=absolutBegin;
        this.absolutEnd=absolutEnd;
    }


    public CropRotationElement(String actionCommand,LMArableID aid){
        this.actionCommand=actionCommand;
        this.aid=aid;
    }
//    public CropRotationElement(LMCropRotationElement cre){
//        this.actionCommand=cre.getActionCommand();
//        this.aid=cre.getLMArableID();
//    }

    public LMCropRotationElement clone(LMArableID aid){
        return new CropRotationElement(aid,actionCommand,absolutBegin,absolutEnd);

    }

    public Boolean isFallow() {
        return aid.isFallow();
    }

    public Boolean wasArableID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Boolean checkActionCommand(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setArableID(Integer i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Integer getArableID() {
        if(aid.isFallow()){
            return 0;
        }else{
            return aid.getID();
        }
    }
    
//    public void changeDurationByCRSaveModel(int minus, int plus, int duration){
//        aid.setDuration(duration);
//        this.absolutBegin=absolutBegin-minus;
//        this.absolutEnd=absolutEnd+plus;
//        aid.setRelativeBegin(aid.getRelativeBegin()-minus);
//        if(aid.getRelativeEnd()+plus>365){
//            while(aid.getRelativeEnd()+plus>365){
//                plus=plus-365;
//            }    
//        }
//        if(aid.getRelativeEnd()+plus<1){
//            while(aid.getRelativeEnd()+plus<1){
//                plus=plus+365;
//            }
//        }
//        aid.setRelativeEnd(aid.getRelativeEnd()+plus);
//            
//        
//        
//    }
    
    public void setRelativeEndByCRSaveModel(int minus){
        this.absolutEnd=absolutEnd-minus;
         if(aid.getRelativeEnd()-minus>365){
            while(aid.getRelativeEnd()-minus>365){
                minus=minus-365;
            }    
        }
        if(aid.getRelativeEnd()-minus<1){
            while(aid.getRelativeEnd()-minus<1){
                minus=minus+365;
            }
        }
        aid.setRelativeEnd(aid.getRelativeEnd()-minus);
        aid.setDuration(aid.getDuration()-minus);
        
    }
    public void setRelativeBeginByCRSaveModel(int plus){
        this.absolutBegin=absolutBegin+plus;
        aid.setRelativeBegin(aid.getRelativeBegin()+plus);
        aid.setDuration(aid.getDuration()-plus);
    }
    
    
    

    public void setBegin(int i) {
        aid.setRelativeBegin(i);
    }

    public int getBegin() {
        return aid.getRelativeBegin();
    }

    public void setEnd(int i) {
        aid.setRelativeEnd(i);
    }

    public int getEnd() {
        return aid.getRelativeEnd();
    }

    public void setDuration(int i) {
        aid.setDuration(i);
    }

    public int getDuration() {
        return aid.getDuration();
    }

    public void setAbsolutBegin(int i) {
       this.absolutBegin=i;
    }

    public int getAbsolutBegin() {
        return this.absolutBegin;
    }

    public void setAbsolutEnd(int i) {
        this.absolutEnd=i;
    }

    public int getAbsolutEnd() {
        return absolutEnd;
    }

    public void setActionCommand(String s) {
        this.actionCommand=s;
    }

    public String getActionCommand() {
        return this.actionCommand;
    }

    public void setWasArableID(Boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Boolean getWasArableID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFallow(Boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LMArableID getLMArableID(){
        return aid;
    }

    public void setLMArableID(LMArableID aid) {
        this.aid=aid;
    }
}
