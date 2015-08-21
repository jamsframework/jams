package lm.view;

import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainMenuBar extends JMenuBar {
    
    //Main Menu
    private JMenu File= new JMenu(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FILE"));
    private JMenu view= new JMenu(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("VIEW"));
    private JMenu Action = new JMenu(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("ACTION"));
    private JMenu Options = new JMenu(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("OPTIONS"));
    //File Menu
        private JMenu New = new JMenu(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NEW"));
        private JMenuItem Load = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("LOAD"));
        private JMenuItem Save = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SAVE"));
        private JMenuItem SaveAs = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SAVE AS.."));
        private JMenuItem close = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CLOSE"));
        //New Menu
            private JMenuItem Empty = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("EMPTY"));
            private JMenuItem FromResources = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FROM RESOURCES"));
    //View Menu

    //Action Menu
        private JMenuItem start = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("START"));

    //Change Menu
        private JMenuItem Change = new JMenuItem(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CHANGE"));
    public MainMenuBar(){

        New.add(Empty);
        New.add(FromResources);

        
        File.add(New);
        File.add(new JSeparator());
        File.add(Load);
        File.add(new JSeparator());
        File.add(Save);
        File.add(SaveAs);
        File.add(new JSeparator());
        File.add(close);

        Action.add(start);

        Options.add(Change);

        this.add(File);
        this.add(view);
        this.add(Action);
        this.add(Options);

    }



    //Setting the Listeners
    //File Menu
    public void setEmpty(ActionListener l){
        this.Empty.addActionListener(l);
    }
    public void setFromResources(ActionListener l){
        this.FromResources.addActionListener(l);
    }
    public void setLoad(ActionListener l){
        this.Load.addActionListener(l);
    }
    public void setSave(ActionListener l){
        this.Save.addActionListener(l);
    }
    public void setSaveAs(ActionListener l){
        this.SaveAs.addActionListener(l);
    }
    public void setCloseListener(ActionListener l){
        this.close.addActionListener(l);
    }

    //Action Menu
    
    public void setStartListener(ActionListener l){
        this.start.addActionListener(l);
    }

    //Change Menu

    public void setChange(ActionListener l){
        this.Change.addActionListener(l);
    }


}
