package lm.view;

import java.awt.Dimension;
import javax.swing.JFrame;
import lm.Componet.LMConfig;
import lm.Componet.LMSaveModel;
import lm.view.Conflict.MainConflictView;
import lm.view.changeCrop.cropPanel;
import lm.view.changeLMArable.fertPanel;
import lm.view.changeLMArable.tillPanel;
import lm.view.miniComponets.Tooltipp;
/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainView extends JFrame {

    private MainConflictView mainConflictView =new MainConflictView();
    private MainContentPanel maincontentpanel = new MainContentPanel();
    private MainMenuBar mainmenubar = new MainMenuBar();
    private Dialogs dialogs=new Dialogs();


    private Import_Panel import_panel;
    private Import_Panel2 import_panel2;
    private ChangeLMArable changeLMArable;
    private ChangeCRView changeCRView;
    
    private tillPanel tillPanel;
    private fertPanel fertPanel;
    private cropPanel cropPanel;
    
    private Tooltipp tt;


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
    public void startImportPanel(LMConfig l){
        import_panel2=new Import_Panel2(l);
        getImport_panel2().start();
    }
    
    public void ChangeLMArable(LMSaveModel saveModel){
        changeLMArable=new ChangeLMArable();
        changeLMArable.createChangeLMArable(saveModel);
        changeLMArable.setVisible(true);
    }

    public void initCRChange(){
        changeCRView=new ChangeCRView();
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
    public ChangeLMArable getChangeLMArable(){
        return this.changeLMArable;
    }
    public ChangeCRView getChangeCR(){
        return this.changeCRView;
    }

    /**
     * @return the mainConflictView
     */
    public MainConflictView getMainConflictView() {
        return mainConflictView;
    }

    /**
     * @param mainConflictView the mainConflictView to set
     */
    public void setMainConflictView(MainConflictView mainConflictView) {
        this.mainConflictView = mainConflictView;
    }

    /**
     * @return the import_panel2
     */
    public Import_Panel2 getImport_panel2() {
        return import_panel2;
    }
    
    public void showtillPanel(){
        tillPanel = new tillPanel();
        tillPanel.setVisible(true);
    }

        
        
    public void showfertPanel(){
        fertPanel=new fertPanel();
        fertPanel.setVisible(true);
    }

    public void showcropPanel(){
        cropPanel=new cropPanel();
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
