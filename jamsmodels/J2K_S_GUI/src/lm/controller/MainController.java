package lm.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import lm.view.*;
import lm.model.*;
import lm.Componet.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainController {
    private static MainModel mainmodel;
    private static MainView mainview;
    private ListenersAndObserver ListAndObs;

    public MainController(){
        this.mainmodel=new MainModel();
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("MAINMODEL ERSTELLT!"));
        this.mainview=new MainView();
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("MAINVIEW ERSTELLT!"));
        this.ListAndObs=new ListenersAndObserver(this);
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("LISTANDOBS ERSTELLT!"));
        CreateConnections();
    }
    
    public void CreateConnections(){
        ListAndObs.AddMainMenuListener();
        ListAndObs.AddObserver();
    }

    public void CreateAndShowGUI(){
        this.mainview.setVisible(true);
    }



    //GetterMethoden

    public MainView getMainView(){
        return mainview;
    }

    public MainModel getMainModel(){
        return mainmodel;
    }
}
