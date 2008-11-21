/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package juice.optimizer.wizard;

import jams.io.XMLIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;

/**
 *
 * @author Christian Fischer
 */
public class step8Pane extends stepPane{
    Document doc;
    String outputFilePath = null;
    
    @Override
    public JPanel build(){
        this.panel.add(new JLabel(java.util.ResourceBundle.getBundle("resources/Bundle").getString("successfully_finished")));
        return panel;
    }
          
    public void setOutputProperties(Document doc, String path){
        this.doc = doc;
        this.outputFilePath = path;
    }
    
    public String init(){
        try{
            XMLIO.writeXmlFile(doc, this.outputFilePath);
        }catch(Exception e){
            return e.toString();
        }   
        return null;
    }
}
