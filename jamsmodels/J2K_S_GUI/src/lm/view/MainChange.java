package lm.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import lm.Componet.LMDefaultModel;

import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;

import javax.swing.JFrame;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;

import lm.view.change.*;
//import lm.view.change.fertPanel;


/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainChange extends JFrame implements Observer {

    private MainContentChange maincontentchange=new MainContentChange();
     private tillPanel tillPanel=new tillPanel();
     private fertPanel fertPanel=new fertPanel();
     private cropPanel cropPanel=new cropPanel();

    public MainChange(){
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

    public void showtillPanel(){
        tillPanel.setVisible(true);
    }

    public void showfertPanel(){
        fertPanel.setVisible(true);
    }

    public void showcropPanel(){
        cropPanel.setVisible(true);
    }


    public tillPanel getTillPanel(){
        return tillPanel;
    }
    
    public fertPanel getFertPanel(){
        return fertPanel;
    }

    public cropPanel getCropPanel(){
        return cropPanel;
    }

}
