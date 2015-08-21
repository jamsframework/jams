package lm.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import lm.Componet.LMDefaultModel;

import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;

import javax.swing.JFrame;
import lm.Componet.LMSaveModel;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;

import lm.view.changeLMArable.*;
//import lm.view.change.fertPanel;


/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ChangeLMArable extends JFrame implements Observer {

    private MainContentChange maincontentchange;
     
    
    public void createChangeLMArable(LMSaveModel saveModel){
        maincontentchange=new MainContentChange(saveModel);
        maincontentchange.createGUI();
        
        this.setContentPane(maincontentchange);
        this.setResizable(false);
        this.setVisible(false);
        pack();
    }
    public void update(Observable o, Object o1) {
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("HEYHO"));
    }

    /**
     * @return the maincontentchange
     */
    public MainContentChange getMaincontentchange() {
        return maincontentchange;
    }

    

}
