/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import reg.gui.MCAT5Dialog.MonteCarloData;

/**
 *
 * @author Christian Fischer
 */
public abstract class DataRequestDlg extends JDialog{
    public static final int OBSERVATED_TIMESERIE    = 1;
    public static final int SIMULATATED_TIMESERIE   = 2;
    public static final int ENSEMBLE_EFFICIENCY     = 3;
    public static final int ENSEMBLE_VARIABLE       = 4;
    
    public class DataHandler extends TransferHandler {  
        DialogRow request;
        
        public DataHandler(DialogRow t) {
            this.request = t;
            this.request.info.setTransferHandler(this);            
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;
        }
        
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                /*Object obj = t.getTransferData(JAMSSpreadSheet.FLAVOR);
                JAMSSpreadSheet.TableData value = (JAMSSpreadSheet.TableData) obj;
                
                if (this.request.request.type == OBSERVATED_TIMESERIE){
                    if (value.values.length != 1){
                        request.info.setText("only one column allowed for observation data");
                        return false;
                    }
                                        
                    ObservationDataSet obs = new ObservationDataSet();
                    obs.name = "obsdata";
                    obs.set = value.values[0];
                    obs.timeLength = value.values[0].length;
                    String result = request.target.addObservationDataSet(obs);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(obs.toString());
                        request.setAccepted();
                    }
                }       
                else if (this.request.request.type == ENSEMBLE_EFFICIENCY){
                    if (value.values.length != 1){
                        request.info.setText("only one column allowed for efficiency data");
                        return false;
                    }
                                        
                    EfficiencyDataSet eff = new EfficiencyDataSet("effdata",value.values[0],null,null,true);                    
                    String result = request.target.addEfficiencyDataSet(eff);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(eff.toString());
                        request.setAccepted();
                    }
                }        */
                return true;
            } catch (Exception e) {
            }

            return super.importData(comp, t);
        }
        
    }
    
    public static class DataRequest{
        int         type;
        String      description;
        boolean     multipleAllowed;
        
        public DataRequest(int type, String desc, boolean multiple){
            this.type = type;
            this.description = desc;
            this.multipleAllowed = multiple;
        }
    }
    
    static final ImageIcon icon_ok = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/ok_button.png"));
    static final ImageIcon icon_required = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/not_ok_button.png"));
    static final ImageIcon icon_add = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/add_button.png"));
    static final ImageIcon icon_del = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/remove_button.png"));
    
    class DialogRow{
        JButton     sign = new JButton("");
        JTextArea  desc = new JTextArea("description");
        JTextArea  info = new JTextArea("drop here!");
        JButton     del = new JButton("");
        
        DataRequest request = null;
        MonteCarloData target = null;
        
        JPanel row = new JPanel();
        
        DialogRow(DataRequest request,MonteCarloData mc,boolean add){
            this.request = request;            
            target = mc;
            JPanel subrow = new JPanel();
            subrow.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            if (add)
                sign.setIcon(icon_add);
            else
                sign.setIcon(icon_required);
            
            sign.setBorderPainted(false);
            sign.setMargin(new Insets(0,0,0,0));
            sign.setContentAreaFilled(false);
            sign.setFocusPainted(false);
            
            del.setIcon(icon_del);           
            del.setMargin(new Insets(0,0,0,0));
            
            
            if (add)
                info.setBackground(new Color(255,255,64));
            else
                info.setBackground(new Color(255,128,128));
            
            info.setLineWrap(true);
            info.setEnabled(false);
            desc.setMinimumSize(new Dimension(300,70));
            desc.setEnabled(false);
            desc.setOpaque(false);

            info.setMinimumSize(new Dimension(100,50));

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 10;
            c.gridheight = 10;
            
            JPanel dummy1 = new JPanel();
            dummy1.add(desc);
            dummy1.setBorder(BorderFactory.createLineBorder(Color.black));
            dummy1.setMinimumSize(new Dimension(100,80));
           // dummy1.
            subrow.add(dummy1,c);            
            
            c.gridx = 10;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 10;
            
            subrow.add(Box.createRigidArea(new Dimension(20,5)),c);
            
            c.gridx = 11;
            c.gridy = 0;
            c.weightx = 1.5;
            c.gridheight = 10;
            
            subrow.add(info,c);
            
            c.gridx = 15;
            c.gridy = 0;
            c.gridwidth = 4;
            c.gridheight = 10;
            
            subrow.add(Box.createRigidArea(new Dimension(20,5)),c);
            
            c.gridx = 19;
            c.gridy = 0;
            c.gridwidth = 4;
            c.gridheight = 10;
            
            subrow.add(sign,c);  
            
            c.gridx = 23;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 10;
            
            subrow.add(del,c);  
            
            c.gridx = 24;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            
            subrow.add(Box.createRigidArea(new Dimension(5,5)),c);
            subrow.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
            subrow.setMinimumSize(new Dimension(500,50));
            
            row.setLayout(new BorderLayout());
            row.add(subrow,BorderLayout.NORTH);
            new DataHandler(this);                        
        }
        
        void setAccepted(){
            sign.setIcon(icon_ok);
            info.setBackground(new Color(128,255,128));
            
            if (this.request.multipleAllowed)
                row.add((new DialogRow(request,target,true)).getComponent());
            row.invalidate();
        }
        
        DataRequest getRequest(){
            return request;
        }
        
        MonteCarloData getTarget(){
            return target;
        }
        
        JPanel getComponent(){
            return row;
        }
    };
    
    ArrayList<DialogRow> dataRows = new ArrayList<DialogRow>();
    
    MonteCarloData mcData = new MonteCarloData();
    
    DataRequestDlg(DataRequest[]req){
        this.setTitle("Data Request");
        
        JPanel dialogPanel = new JPanel(new BorderLayout());
        
        JTextArea descarea = new JTextArea("This Dialog list all data required to perfom the requested operation. Load the data you want to use into" +
                " the JAMS Explorer and Drag and Drop it to this dialog!\n\n");        
        descarea.setEnabled(false);
        descarea.setBackground(dialogPanel.getBackground());
        descarea.setForeground(new Color(0,0,0));
        descarea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descarea.setLineWrap(true);
        descarea.setWrapStyleWord(true);
        descarea.setMinimumSize(new Dimension(500, 100));
        
        dialogPanel.add(descarea,BorderLayout.NORTH);
        dialogPanel.add(Box.createRigidArea(new Dimension(50,50)),BorderLayout.WEST);
        dialogPanel.add(Box.createRigidArea(new Dimension(50,50)),BorderLayout.EAST);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
        mainPanel.setMinimumSize(new Dimension(500, Math.min(req.length*50+150,500)));
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        
        for (int i=0;i<req.length;i++){
            dataRows.add(new DialogRow(req[i],mcData,false));
            mainPanel.add(dataRows.get(dataRows.size()-1).getComponent());
        }
        JScrollPane scroller = new JScrollPane(mainPanel);        
        scroller.setMinimumSize(new Dimension(500, Math.min(req.length*50+150,500)));        
        dialogPanel.add(scroller,BorderLayout.CENTER);
        dialogPanel.add(new JButton("OK"){
            public void actionPerformed(ActionEvent e){                
                dataCollectAction();
            }        
        },BorderLayout.SOUTH);
        this.add(dialogPanel);
        this.setMinimumSize(new Dimension(500, Math.min(req.length*50+150,500)));        
        this.setResizable(false);
        this.invalidate();
        this.setVisible(true);
    }
    
    abstract public void dataCollectAction();
}
