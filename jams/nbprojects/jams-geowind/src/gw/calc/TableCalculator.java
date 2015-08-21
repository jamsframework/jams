/*
 * TableCalculator.java
 *
 * Created on April 25, 2007, 7:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gw.calc;

import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.Evaluator;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 */
public class TableCalculator {
    
    String usage() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/gw/calc/Functions.txt");
            if (is == null)
                return "";
            int c;
            StringBuffer b = new StringBuffer();
            while ((c = is.read()) != -1) {

                b.append((char) c);
            }
            is.close();
            return b.toString();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
    
    String apply(JelLibrarySupport jel, JTable table, String expr) {
        
        AbstractTableModel model = (AbstractTableModel) table.getModel();

        table.clearSelection();

        String lhs = null;
        String rhs = expr;
        
        if (expr.equals("?")) {
            return usage();
        }

        int idx = expr.indexOf('=');
        if ((idx > 0) &&
                (expr.charAt(idx + 1) != '=') && 
                (expr.charAt(idx - 1) != '!') && 
                (expr.charAt(idx - 1) != '<') && 
                (expr.charAt(idx - 1) != '>')) {
            lhs = expr.substring(0, expr.indexOf('=')).trim();
            rhs = expr.substring(expr.indexOf('=') + 1).trim();
        }

        CompiledExpression expr_c = null;
        Object result = null;

        try {
            expr_c = Evaluator.compile(rhs, jel.getLibrary());

            // test if implicite looping
            try {
                if (expr.indexOf('$') == -1) {
                    // single expression
                    if (lhs == null) {
                        result = expr_c.evaluate(jel.getContext());
                        if (result != null) {
                            return java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_RESULT") + rhs + "': " + result;
                        }
                    }
                } else if (lhs != null) {
                    if (lhs.startsWith("$")) {
                        lhs = lhs.substring(1);
                        int col = Util.findColumn(model, lhs);
                        if (col == -1) {
                            return java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_NO_SUCH_COLUMN") + lhs;
                        }
                        for (int row = 0; row < model.getRowCount(); row++) {
                            jel.getResolver().setRow(row);
                            result = expr_c.evaluate(jel.getContext());
                            if (result != null) {
                                model.setValueAt(result, row, col);
                               // model.fireTableCellUpdated(row, col);
                            }
                        }
                    } else {
                        return "lhs array not supported yet";
                    }
                } else {
                    // row selection
                    StringBuffer b = new StringBuffer();
                    for (int row = 0; row < model.getRowCount(); row++) {
                        jel.getResolver().setRow(row);
                        result = expr_c.evaluate(jel.getContext());
                        if (result != null) {
                            if (result.getClass() == Boolean.class) {
                                boolean r = (Boolean) result;
                                if (r) {
                                    table.addRowSelectionInterval(row, row);
                                }
                            } else {
                                b.append(result);
                            }
                        }
                    }
                    return b.toString();
                }
            } catch (Throwable e) {
                return "Exception emerged from JEL compiled code (IT'S OK) :" + e.getMessage();
            }
        } catch (CompilationException ce) {
            StringBuffer b = new StringBuffer();
            b.append("  ERROR: ");
            b.append(ce.getMessage() + "\n");
            b.append("                       ");
            b.append(rhs + "\n");
            int column = ce.getColumn(); // Column, where error was found
            for (int i = 0; i < column + 23 - 1; i++) {
                b.append(' ');
            }
            b.append("^\n");
            return b.toString();
        }
        return "";
    }
}
