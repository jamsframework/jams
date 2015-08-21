package lm.view;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainView extends JFrame {


    private MainContentPanel maincontentpanel = new MainContentPanel();
    private MainMenuBar mainmenubar = new MainMenuBar();
    private Dialogs dialogs=new Dialogs();


    private Import_Panel import_panel;
    private MainChange mainchange=new MainChange();
    

    public MainView (){
            super(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("LANDNUTZUNGSMANGEMENTEDITOR"));
            initForm();
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initForm(){
        this.setSize(new Dimension(500,500));
        this.setJMenuBar(mainmenubar);
        this.getContentPane().add(maincontentpanel);
        this.setPreferredSize(new Dimension(500,500));

    }
    public void  ImportPanel(){
        import_panel=new Import_Panel();
        import_panel.setVisible(true);
    }
    public void MainChange(){
        mainchange.setVisible(true);
    }





    //Getter Methoden
    public MainMenuBar getMainMenuBar(){
        return mainmenubar;
    }
    public MainContentPanel getMainContentPanel(){
        return maincontentpanel;
    }
    public Import_Panel getImport_Panel(){
        return import_panel;
    }
    public Dialogs getDialogs(){
        return this.dialogs;
    }
    public MainChange getMainChange(){
        return this.mainchange;
    }

}
