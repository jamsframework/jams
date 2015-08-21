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
        private JMenu file= new JMenu("File");
        private JMenu view= new JMenu("View");
        private JMenu action = new JMenu("Action");
        private JMenu options = new JMenu("Options");
    //File Menu
        private JMenu new_ = new JMenu("New");
        private JMenuItem load = new JMenuItem("Load");
        private JMenuItem save = new JMenuItem("Save");
        private JMenuItem saveAs = new JMenuItem("Save As");
        private JMenuItem close = new JMenuItem("Close");
        //New Menu
        private JMenuItem empty = new JMenuItem("Empty");
        private JMenuItem fromResources = new JMenuItem("From Resources");
    //View Menu

    //Action Menu
        private JMenuItem start = new JMenuItem("Start");

    //Change Menu
        private JMenuItem changelmArable = new JMenuItem("ChangeLMArable");
        private JMenuItem changeCR =new JMenuItem("ChangeCR");
        private JMenuItem changeCrop =new JMenuItem("ChangeCrop");
        private JMenuItem changeTill =new JMenuItem("ChangeTill");
        private JMenuItem changeFert =new JMenuItem("ChangeFert");       

        public MainMenuBar(){

        new_.add(empty);
        new_.add(fromResources);

        
        file.add(new_);
        file.add(new JSeparator());
        file.add(load);
        file.add(new JSeparator());
        file.add(save);
        file.add(saveAs);
        file.add(new JSeparator());
        file.add(close);

        action.add(start);

        options.add(changelmArable);
        options.add(changeCR);
        options.add(changeCrop);
        options.add(changeTill);
        options.add(changeFert);
        
        this.add(file);
        this.add(view);
        this.add(action);
        this.add(options);

    }



    //Setting the Listeners
    //File Menu
    public void setEmpty(ActionListener l){
        this.empty.addActionListener(l);
    }
    public void setFromResources(ActionListener l){
        this.fromResources.addActionListener(l);
    }
    public void setLoad(ActionListener l){
        this.load.addActionListener(l);
    }
    public void setSave(ActionListener l){
        this.save.addActionListener(l);
    }
    public void setSaveAs(ActionListener l){
        this.saveAs.addActionListener(l);
    }
    public void setCloseListener(ActionListener l){
        this.close.addActionListener(l);
    }

    //Action Menu
    
    public void setStartListener(ActionListener l){
        this.start.addActionListener(l);
    }

    //Change Menu

    public void setChangeLMArable(ActionListener l){
        this.changelmArable.addActionListener(l);
    }

    public void setChangeCR(ActionListener l){
        this.changeCR.addActionListener(l);

    }
    
    public void setChangeCrop(ActionListener l){
        this.changeCrop.addActionListener(l);
    }
    
    public void setChangeTill(ActionListener l){
        this.changeTill.addActionListener(l);
    }
    
    public void setChangeFert(ActionListener l){
        this.changeFert.addActionListener(l);
    }


}
