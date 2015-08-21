package lm.view;

import lm.Componet.*;
import lm.Componet.Vector.*;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ContentPanel extends JPanel implements Observer {

    private JTextArea textarea = new JTextArea();
    private LMDefaultModel model;

    public ContentPanel(){
        this.setPreferredSize(new Dimension(400,400));
        this.setLayout(new GridLayout(1,1));
        this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JScrollPane t = new JScrollPane(textarea);
        this.add(t);
        
    }

    void setContentPanel(){

    }
//    public void setTextArea(){
//        textarea.append("DIES IST DIE CROP.PAR!!!" + "\r\n");
//        for(int i=0;i<model.getCrop().size();i++){
//            ArrayList t =(ArrayList)model.getCrop().get(i);
//            for(int p=0;p<t.size();p++){
//                textarea.append(t.get(p) + "\t" + "\t");
//            }
//            textarea.append( "\r\n");
//        }
//        textarea.append("DIES IST DIE Fert.PAR!!!" + "\r\n");
//        for(int i=0;i<model.getFert().size();i++){
//            ArrayList t =(ArrayList)model.getFert().get(i);
//            for(int p=0;p<t.size();p++){
//                textarea.append(t.get(p) + "\t"+ "\t");
//            }
//            textarea.append( "\r\n");
//        }
//        textarea.append("DIES IST DIE Till.PAR!!!"+"\r\n");
//        for(int i=0;i<model.getTill().size();i++){
//            ArrayList t =(ArrayList)model.getTill().get(i);
//            for(int p=0;p<t.size();p++){
//                textarea.append(t.get(p) + "\t"+ "\t");
//            }
//            textarea.append( "\r\n");
//        }
//        repaint();
//    }
    public void setTextArea(){
            textarea.append("DIES IST DIE Fert.PAR!!!" + "\r\n");
            for(int i=0;i<model.getFert().size();i++){
                LMFertVector t =(LMFertVector) model.getFert().get(i);
                textarea.append(t.getID() + "\t"+ "\t");
                textarea.append(t.getfertnm() + "\t"+ "\t");
                textarea.append(t.getfminn() + "\t"+ "\t");
                textarea.append(t.getfminp() + "\t"+ "\t");
                textarea.append(t.getforgn() + "\t"+ "\t");
                textarea.append(t.getforgp() + "\t"+ "\t");
                textarea.append(t.getfnh3n() + "\t"+ "\t");
                textarea.append(t.getbactpdb() + "\t"+ "\t");
                textarea.append(t.getbactldb() + "\t"+ "\t");
                textarea.append(t.getbactddb() + "\t"+ "\t");
                textarea.append(t.getdesc() +"\r\n");

            }
            
    }


    public void update(Observable FileReader,Object arg){

        setTextArea();
    }


    public void setDefaultModel(LMDefaultModel model){
        this.model=model;
    }
    public LMDefaultModel getDefaultModel(){
        return this.model;
    }
}
