package jams.tspaces;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.unijena.jams.data.JAMSString;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.StringTokenizer;
import javax.swing.*;

public class Table extends JPanel {

    public Table(InitSpace initspace, JTextArea t) {       
		super(new GridLayout(1,0)); 
        try{
			TupleSpace space = new TupleSpace(initspace.name,initspace.host);			
			Tuple template=new Tuple(new Field(new JAMSString("header")), new Field(JAMSString.class));
			String header=((JAMSString)space.read(template).getField(1).getValue()).getValue();
			
			StringTokenizer headertokenizer = new StringTokenizer(header);
			int anzspalten=headertokenizer.countTokens();
			String[] columnNames=new String[anzspalten];
					
			for(int i=0;i<anzspalten;i++){
				columnNames[i]=headertokenizer.nextToken();
			}
			
			template = new Tuple(new Field(new JAMSString("result")), new Field(JAMSString.class));
			Tuple scan = space.scan(template);
			int size=scan.numberOfFields();	
			Object[][] data=new Object[size][anzspalten];
			for (int i=0; i<size; i++){
		    	String result=((JAMSString) (((Tuple) scan.getField(i).getValue()).getField(1).getValue())).toString();
				StringTokenizer restokenizer = new StringTokenizer(result);
				for(int j=0;j<anzspalten;j++){
					data[i][j]=restokenizer.nextToken();
				}    	
		    }
			
			for (int i=0; i< data.length; i++){
				int min=i;
				for (int j=i; j<data.length; j++){
					if (Integer.parseInt((String)data[j][0])<Integer.parseInt((String)data[min][0])){
						min=j;
					}
				}
				if(i!=min){
					Object[] h=data[min];
					data[min]=data[i];
					data[i]=h;
				}			
			}
	        final JTable table = new JTable(data, columnNames);
	        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
	       // table.setFillsViewportHeight(true);
	   
	        //Create the scroll pane and add the table to it.
	        JScrollPane scrollPane = new JScrollPane(table);

	        //Add the scroll pane to this panel.
	        add(scrollPane);					
		} catch (Exception exc) {
	        t.append("Der Space existiert nicht, ist leer oder es fehlen Daten im Space. \n\n");
		}            
    } 
}

