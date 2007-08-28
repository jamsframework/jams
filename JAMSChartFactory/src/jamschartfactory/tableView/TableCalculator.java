/*
 * TableCalculator.java
 *
 * Created on 14. November 2005, 15:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jamschartfactory.tableView;

import java.util.StringTokenizer;
import javax.swing.JTable;

/**
 *
 * @author c0krpe
 */
public class TableCalculator {
    final static String[] OPERATORS = {"=", "+", "-", "*", "/"};
    /**
     * Creates a new instance of TableCalculator 
     */
    public TableCalculator() {
    }
    
    public static void calcEntries(JTable table, String calcString){
        //getting the selection
        // Get the min and max ranges of selected cells
        int rowIndexStart = table.getSelectedRow();
        int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
        int colIndexStart = table.getSelectedColumn();
        int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();
        
        
        System.out.println("from row: " + rowIndexStart);
        System.out.println("to row: " + rowIndexEnd);
        System.out.println("from col: " + colIndexStart);
        System.out.println("to col: " + colIndexEnd);
        
        for(int x = colIndexStart; x <= colIndexEnd; x++){
            for(int y = rowIndexStart; y <= rowIndexEnd; y++){
                if(((SortableTableModel)table.getModel()).getDataTypes(x) == "i"){
                    int actVal = ((Integer)table.getModel().getValueAt(y, x)).intValue();
                    double newVal = calcValue((double)actVal, calcString);
                    if(newVal != -9999){
                        int newInt = (int)newVal;
                        table.getModel().setValueAt(newInt, y, x);
                        table.repaint();
                    }
                }
                else if(((SortableTableModel)table.getModel()).getDataTypes(x) == "d"){
                    double actVal = ((Double)table.getModel().getValueAt(y, x)).doubleValue();
                    double newVal = calcValue(actVal, calcString);
                    if(newVal != -9999){
                        table.getModel().setValueAt(newVal, y, x);
                        table.repaint();
                    }
                }
            }
        }
        table.changeSelection(rowIndexStart, colIndexStart, false, false);
        table.changeSelection(rowIndexEnd, colIndexEnd, false, true);
    }
    
    private static double calcValue(double value, String calcString){
        //parsing the calcString
        StringTokenizer tok = new StringTokenizer(calcString, " ");
        String operator = tok.nextToken();
        boolean valid = false;
        for(int i = 0; i < OPERATORS.length; i++){
            if(operator.equals(OPERATORS[i])){
                valid = true;
                break;
            }
        }
        if(!valid){
            System.err.println("Formula is not valid!");
            return -9999;
        }
        double opValue;
        try{
            opValue = Double.parseDouble(tok.nextToken());
        }catch(NumberFormatException nfe){
            System.err.println("Formula is not valid!");
            return -9999;
        }
        
        double returnVal;
        
        if(operator.equals("+")){
            return (value + opValue);
        }
        else if(operator.equals("-")){
            return(value - opValue);
        }
        else if(operator.equals("*")){
            return(value * opValue);
        }
        else if(operator.equals("/")){
            return(value / opValue);
        }
        else if(operator.equals("=")){
            return(opValue);
        }
        else{
            System.out.println("No valid calc string!");
            return -9999;
        }
        
    }
    
}
