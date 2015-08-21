/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMCropRotationElement {

    public Boolean isFallow();
    public Boolean wasArableID();

     public LMArableID getLMArableID();
     public void setLMArableID (LMArableID aid);
    public Boolean checkActionCommand(String s);

    public LMCropRotationElement clone(LMArableID aid);

    public void setArableID(Integer i);
    public Integer getArableID();

    public void setBegin(int i);
    public int getBegin();

    public void setEnd(int i);
    public int getEnd();

    public void setDuration(int i);
    public int getDuration();

    public void setAbsolutBegin(int i);
    public int getAbsolutBegin();

    public void setAbsolutEnd(int i);
    public int getAbsolutEnd();

    public void setActionCommand(String s);
    public String getActionCommand();

    public void setWasArableID(Boolean b);
    public Boolean getWasArableID();

    public void setFallow(Boolean b);
    
    public void setRelativeEndByCRSaveModel(int plus);
    public void setRelativeBeginByCRSaveModel(int minus);

}
