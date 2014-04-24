/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.tools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class TableRowHeader extends JViewport implements AdjustmentListener{
    private JTable  table;
    private Cell cell;
    private Object names[];

    public TableRowHeader( JTable table, JScrollPane parent ) {
        this.table = table;
        cell = new Cell();
        setView( new Dummy() );
        setPreferredSize( new Dimension( 100, 0 ) );

        parent.getVerticalScrollBar().addAdjustmentListener( this );
        parent.getHorizontalScrollBar().addAdjustmentListener( this );
    }

    public void adjustmentValueChanged( AdjustmentEvent e ) {
        repaint();
    }

    public void setRowHeaderName(Object[] names){
        this.names = names;
        int width = 0;
        for (int i=0;i<names.length;i++){
            JLabel test = new JLabel(names[i].toString());
            width = Math.max(test.getPreferredSize().width,width);                    
        }
        setPreferredSize( new Dimension( width+20, 0 ) );
        invalidate();
        repaint();
    }
    protected void paintComponent(Graphics g){
        super.paintComponent( g );
        Rectangle rec = TableRowHeader.this.getViewRect();

        int y = 0;
        int rows = table.getRowCount();
        int index = 0;

        if( rows == 0 )
            return;

        if( y + table.getRowHeight( 0 ) < rec.y ){
            while( index < rows ){
                int height = table.getRowHeight( index );

                if( y + height < rec.y ){
                    y += height;
                    index++;
                }
                else
                    break;
            }
        }

        int max = rec.y + rec.height;
        int width = getWidth();

        while( y < max && index < rows ){
            int height = table.getRowHeight( index );
            cell.set( index );
            SwingUtilities.paintComponent( g, cell, this, 0, y-rec.y, width, height );

            y += height;
            index++;
        }
    }

    // Nur eine leere Hülle, damit eine "Simulation" aufgebaut werden kann.
    private static class Dummy extends JComponent{
        public void paint( Graphics g ){
            // do nothing
        }

        public void update( Graphics g ) {
            // do nothing
        }
    }

    // Stellt den Knopf für eine Reihe dar.
    private class Cell extends JLabel {
        public void set( int row ) {
            setHorizontalAlignment( CENTER );
            setForeground( TableRowHeader.this.getForeground() );
            setBackground( TableRowHeader.this.getBackground() );
            setFont( TableRowHeader.this.getFont() );
            if (names!=null && names.length>row)
                setText( names[row].toString());
            else
                setText( "Row " + row );
            setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
        }
    }
}