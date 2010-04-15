/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import jams.JAMS;
import jamsui.juice.optimizer.wizard.Tools.Parameter;

/**
 *
 * @author Christian Fischer
 */
public class step3Pane extends stepPane{
    JPanel step3ParameterPanel = null;
    ArrayList<Parameter> selectedParameters = null;
    
    JTextField lowBound[];            
    JTextField upBound[];
    
    JTextField startValue[];
            
    public void setSelectedParameters(ArrayList<Parameter> selectedParameters){
        this.selectedParameters = selectedParameters;
    }
    
    @Override
    public JPanel build() {        
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(JAMS.resources.getString("step2_desc")), BorderLayout.NORTH);               
        
        step3ParameterPanel = new JPanel(new BorderLayout());
        //step3.add(step3ParameterPanel);
        
        JScrollPane Scroller = new JScrollPane(step3ParameterPanel);        
        Scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        Scroller.setVisible(true);
        panel.add(Scroller,BorderLayout.CENTER);
        
        
        return panel;
    }
    
    @Override    
    public JPanel getPanel(){
        return panel;
    }
    @Override   
    public String init(){
        if (selectedParameters == null){
            return JAMS.resources.getString("error_no_parameter");
        }                
        step3ParameterPanel.removeAll();
        step3ParameterPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;    c.gridy = 0;    c.fill = GridBagConstraints.HORIZONTAL;
        step3ParameterPanel.add(new JLabel(JAMS.resources.getString("parameter")),c);
        c.gridx = 1;    c.gridy = 0;        
        step3ParameterPanel.add(new JLabel(JAMS.resources.getString("component")),c);
        c.gridx = 2;    c.gridy = 0;
        step3ParameterPanel.add(new JLabel(JAMS.resources.getString("lower_bound")),c);
        c.gridx = 3;    c.gridy = 0;
        step3ParameterPanel.add(new JLabel(JAMS.resources.getString("upper_bound")),c);        
        c.gridx = 4;    c.gridy = 0;
        step3ParameterPanel.add(new JLabel(JAMS.resources.getString("start_value")),c);        
        //step3ParameterPanel.setMaximumSize(new Dimension(100,100));
        
        lowBound = new JTextField[selectedParameters.size()];
        upBound = new JTextField[selectedParameters.size()];
        startValue = new JTextField[selectedParameters.size()];
        
        for (int i=0;i<selectedParameters.size();i++){
            JTextField paramName = new JTextField(10);
            paramName.setEditable(false);
            JTextField componentName = new JTextField(20);
            componentName.setEditable(false);
            if (selectedParameters.get(i).variableName != null){
                paramName.setText(selectedParameters.get(i).variableName);
                componentName.setText(selectedParameters.get(i).componentName);            
            }else{
                paramName.setText(selectedParameters.get(i).attributeName);
                componentName.setText(selectedParameters.get(i).contextName);
            }                                                                            
            lowBound[i] = new JTextField(8);            
            upBound[i] = new JTextField(8);
            startValue[i] = new JTextField(10);
            
            c.gridx = 0;    c.gridy = i+1;
            step3ParameterPanel.add(paramName,c);
            c.gridx = 1;    c.gridy = i+1;
            step3ParameterPanel.add(componentName,c);
            c.gridx = 2;    c.gridy = i+1;
            step3ParameterPanel.add(lowBound[i],c);
            c.gridx = 3;    c.gridy = i+1;
            step3ParameterPanel.add(upBound[i],c);
            c.gridx = 4;    c.gridy = i+1;
            step3ParameterPanel.add(startValue[i],c);
        }
        return null;
    }
    
    @Override
    public String finish(){
        for (int i=0;i<this.lowBound.length;i++){
           try{
                this.selectedParameters.get(i).lowerBound = Double.parseDouble(this.lowBound[i].getText());
           }catch(Exception e){
               return JAMS.resources.getString("illegal_number_format_of_lower_bound_in_row") + (i+1) + "!";
           }
           try{
                this.selectedParameters.get(i).upperBound = Double.parseDouble(this.upBound[i].getText());
           }catch(Exception e){
               return JAMS.resources.getString("illegal_number_format_of_upper_bound_in_row") + (i+1) + "!";
           }
           if (this.selectedParameters.get(i).lowerBound >= this.selectedParameters.get(i).upperBound){
               return JAMS.resources.getString("lower_bound_have_to_be_smaller_than_upper_bound");
           }               
           try{
               if (this.startValue[i].getText().length() != 0){
                    this.selectedParameters.get(i).startValue = Double.parseDouble(this.upBound[i].getText());
                    this.selectedParameters.get(i).startValueValid = true;
               }else{
                   this.selectedParameters.get(i).startValueValid = false;
               }
           }catch(Exception e){
               return JAMS.resources.getString("illegal_number_format_of_startvalue_in_row") + (i+1) + "!";
           }
        }
        return null;
    }
}
