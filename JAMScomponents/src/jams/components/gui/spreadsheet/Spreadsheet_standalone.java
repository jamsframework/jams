/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
/**
 *
 * @author Developement
 */
public class Spreadsheet_standalone extends JFrame{
    
    JAMSSpreadSheet spreadsheet;
    
    public Spreadsheet_standalone(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("JAMS JTS Viewer");
        URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        
        setSize(680,480);
        
        initSpreadSheet();
        
        
        //setMinimumSize(new Dimension(680,480));
        createPanel();
        //timePlot();
        pack();
        setVisible(true);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Spreadsheet_standalone main = new Spreadsheet_standalone();
    }
    
    private void initSpreadSheet(){
        
        String[] default_headers = {""};
        
        spreadsheet = new jams.components.gui.spreadsheet.JAMSSpreadSheet(this, default_headers);
        spreadsheet.init();
    }
    
    private void createPanel(){
        add(spreadsheet.getPanel());
    }


}
