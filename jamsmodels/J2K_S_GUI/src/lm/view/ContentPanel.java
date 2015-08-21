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
import lm.model.DefaultVector.ArableID;


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
            textarea.append(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DIES IST DIE FERT.PAR!!!") + java.util.ResourceBundle.getBundle("ressources/Ressources").getString("RN"));
            Integer i =(Integer)model.getLMFert().firstKey();
            while(i!=null){
                LMFertVector t =(LMFertVector) model.getLMFert().get(i);
                textarea.append(t.getID() + "\t");
                textarea.append(t.getfertnm() +"\t" );
                textarea.append(t.getfminn() + "\t");
                textarea.append(t.getfminp() + "\t");
                textarea.append(t.getforgn() + "\t");
                textarea.append(t.getforgp() +"\t" );
                textarea.append(t.getfnh3n() + "\t");
                textarea.append(t.getbactpdb() + "\t");
                textarea.append(t.getbactldb() +"\t");
                textarea.append(t.getbactddb() + "\t");
                textarea.append(t.getdesc() +"\r\n");
                i=(Integer)model.getLMFert().higherKey(i);
            }
            textarea.append(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DIES IST DIE TILL.PAR!!!")+ java.util.ResourceBundle.getBundle("ressources/Ressources").getString("RN"));
            i=(Integer)model.getLMTill().firstKey();
            while(i!=null){
                LMTillVector t=(LMTillVector) model.getLMTill().get(i);
                textarea.append(t.getTID() + "\t");
                textarea.append(t.gettillnm() + "\t");
                textarea.append(t.getdesc() + "\t");
                textarea.append(t.geteffmix() + "\t");
                textarea.append(t.getdeptil() + "\r\n");
                i=(Integer)model.getLMTill().higherKey(i);
            }
             textarea.append(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DIES IST DIE LMARABLE.PAR!!!")+ java.util.ResourceBundle.getBundle("ressources/Ressources").getString("RN"));
            ArableID AID=(ArableID)model.getLMArable().firstKey();
             while(AID!=null){
                LMArableVector t=(LMArableVector) model.getLMArable().get(AID);
                textarea.append(t.getAID().toString()+ "\t");
                textarea.append(t.getCID().getID()+ "\t");
                textarea.append(t.getDate()+  "\t");
                textarea.append(t.getTID().getTID()+"\t");
                textarea.append(t.getFID().getID()+ "\t");
                textarea.append(t.getFAmount()+ "\t");
                textarea.append(t.getPLANT()+ "\t");
                textarea.append(t.getHARVEST()+ "\t");
                textarea.append(t.getFRACHARV()+ "\r\n");
                AID=(ArableID)model.getLMArable().higherKey(AID);
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
