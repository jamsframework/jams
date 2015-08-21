package lm.view.changeCR;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import lm.Componet.Vector.LMCropRotationElement;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ViewButton extends JButton {

    //private JButton jButton;
    private int duration;
    private int begin;
    private int end;
    private Color color;

    public ViewButton(LMCropRotationElement cropRotationElement,Color color,int zoom){
        this.setActionCommand(cropRotationElement.getActionCommand());
        this.duration=cropRotationElement.getDuration();
        //this.begin=cropRotationElement.getAbsolutBegin();
        //this.end=cropRotationElement.getAbsolutEnd();
        if(!cropRotationElement.isFallow()){
            this.setText(cropRotationElement.getArableID()+"");
        }
        this.setBackground(color);
        this.setPreferredSize(new Dimension(duration/zoom,20));
        this.setMinimumSize(this.getPreferredSize());
        
    }
    public ViewButton(){
        this.begin=0;
        this.end=0;
        this.duration=0;
        this.setLast();

    }

//    public ViewButton(int duration, int begin , int end ){
//        this.duration=duration;
//        this.begin=begin;
//        this.end=end;
//        this.color=Color.WHITE;
//        this.setBackground(color);
//        this.setPreferredSize(new Dimension(duration/4,20));
//        this.setMinimumSize(this.getPreferredSize());
//        //ActionCommandString hinzufügen
//    }

//    public ViewButton(int duration , int begin , int end , Color color,String cid){
//        this.duration=duration;
//        this.begin=begin;
//        this.end=end;
//        this.color=color;
//        this.cid=cid;
//        this.setBackground(color);
//        System.out.println("Dieser Button hat eine Länge von ----->" +duration);
//        this.setPreferredSize(new Dimension(duration/4,20));
//        this.setMinimumSize(this.getPreferredSize());
//
//        //ActionCommandString hinzufügen
//    }

    public void setLast(){
        this.setIcon(new ImageIcon("src/lm/resources/images/Plus.gif"));
        this.setPreferredSize(new Dimension(20,20));
        this.setMinimumSize(this.getPreferredSize());
    }
    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return the begin
     */
    public int getBegin() {
        return begin;
    }

    /**
     * @param begin the begin to set
     */
    public void setBegin(int begin) {
        this.begin = begin;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
