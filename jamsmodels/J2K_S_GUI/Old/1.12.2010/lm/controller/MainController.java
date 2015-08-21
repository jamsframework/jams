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
        System.out.println("MainModel erstellt!");
        this.mainview=new MainView();
        System.out.println("MainView erstellt!");
        this.ListAndObs=new ListenersAndObserver(this);
        System.out.println("ListAndObs erstellt!");
        CreateConnections();
    }
    
    public void CreateConnections(){
        ListAndObs.AddListener();
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
