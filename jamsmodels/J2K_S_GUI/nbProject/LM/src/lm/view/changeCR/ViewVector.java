package lm.view.changeCR;

import java.awt.Color;
import java.util.ArrayList;
import lm.Componet.Vector.LMCropRotationElement;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ViewVector extends ArrayList<ViewButton> {

    private int year;
    private int lastDay;
    private Boolean hasLast;

    public ViewVector(){
        this.year=0;
        this.lastDay=0;
        this.hasLast=false;
    }



    public void addButton(LMCropRotationElement cropRotationElement, Color color,int zoom){
        if(hasLast){
           this.remove(this.get(this.size()-1));
        }
        this.add(new ViewButton(cropRotationElement,color,zoom));
        addLastButton(cropRotationElement.getActionCommand());
    }
    public void addLastButton(Integer key) {
       ViewButton viewButton=new ViewButton();
       viewButton.setLast();
       viewButton.setActionCommand(String.valueOf(key));
       this.add(viewButton);
       hasLast=true;
    }

    private void addLastButton(String s){
        ViewButton viewButton=new ViewButton();
        viewButton.setLast();
        viewButton.setActionCommand(s.split("/")[0]);
        this.add(viewButton);
        hasLast=true;
    }

    public Boolean isLast(int i){
        return this.size()-1 == i;
    }


    public int getLastDay(){
        return this.lastDay;
    }

}
