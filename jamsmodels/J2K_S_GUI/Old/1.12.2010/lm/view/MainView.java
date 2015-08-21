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
    private Import_Panel import_panel = new Import_Panel();
    private Dialogs dialogs=new Dialogs();

    public MainView (){
            super("LandnutzungsMangementEditor");
            initForm();
    }

    private void initForm(){
        this.setSize(new Dimension(500,500));
        this.setJMenuBar(mainmenubar);
        this.getContentPane().add(maincontentpanel);
        this.setPreferredSize(new Dimension(500,500));

    }
    public void  ImportPanel(){
        import_panel.setVisible(true);

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

}
