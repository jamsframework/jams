/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsspreadsheet;
/**
 *
 * @author Developement
 */
import javax.swing.JFrame;

public class Main extends JFrame{
    
    
    
    private SpreadSheet sheet = new SpreadSheet(this);
    
    
    public Main(){
        
        super("JAMS Spread Sheet");
        setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        sheet.init();
        add(sheet.getPanel());
        setVisible(true);
        pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
    }

}
