/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

import jams.tools.JAMSTools;
import javax.swing.JFrame;
/**
 *
 * @author Developement
 */
public class Spreadsheet_standalone extends JFrame{
    
    JAMSSpreadSheet spreadsheet;
    
    public Spreadsheet_standalone(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("JAMS JTS Viewer");
        setIconImages(JAMSTools.getJAMSIcons());
        
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
