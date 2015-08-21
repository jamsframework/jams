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

    private JMenu file= new JMenu("File");
    private JMenu view= new JMenu("View");
    private JMenu import_ = new JMenu("Import");
    private JMenu Action = new JMenu("Action");
    private JMenuItem close = new JMenuItem("close");
    private JMenuItem new_ = new JMenuItem("New");
    private JMenuItem lmArable = new JMenuItem("lmArable");
    private JMenuItem import2 = new JMenuItem("Import2");
    private JMenuItem start = new JMenuItem("Start");

    public MainMenuBar(){

        import_.add(lmArable);
        import_.add(import2);

        file.add(new_);
        file.add(new JSeparator());
        file.add(import_);
        file.add(new JSeparator());
        file.add(close);

        Action.add(start);

        this.add(file);
        this.add(view);
        this.add(Action);

    }
    public void setCloseListener(ActionListener l){
        this.close.addActionListener(l);
    }
    public void setImport_lmArableListener(ActionListener l){
        this.lmArable.addActionListener(l);
    }
    public void setImport2(ActionListener l){
        this.import2.addActionListener(l);
    }
    public void setStartListener(ActionListener l){
        this.start.addActionListener(l);
    }

}
