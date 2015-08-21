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
        this.mainview=new MainView();
        this.ListAndObs=new ListenersAndObserver(this);
        CreateConnections();
    }
    
    public void CreateConnections(){
        ListAndObs.AddMainMenuListener();
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
    public ListenersAndObserver getListenerAndOberserver(){
        return ListAndObs;
    }
}
