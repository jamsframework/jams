/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jams.tools.XMLTools;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class step8Pane extends stepPane{
    Document doc;
    String outputFilePath = null;
    
    @Override
    public JPanel build(){
        this.panel.add(new JLabel(JAMS.resources.getString("successfully_finished")));
        return panel;
    }
          
    public void setOutputProperties(Document doc, String path){
        this.doc = doc;
        this.outputFilePath = path;
    }
    
    public String init(){
        try{
            XMLTools.writeXmlFile(doc, this.outputFilePath);
        }catch(Exception e){
            return e.toString();
        }   
        return null;
    }
}
