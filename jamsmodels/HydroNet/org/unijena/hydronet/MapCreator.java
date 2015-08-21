package org.unijena.hydronet;

import jams.data.Attribute.Entity;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import org.geotools.gui.swing.event.SelectionChangeListener;
import org.geotools.gui.swing.event.SelectionChangedEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.opengis.feature.simple.SimpleFeature;


/*
 * @author C. Fischer
 */
public class MapCreator extends jams.components.gui.MapCreator {               	
    private JTextArea idfield;		
    private JFreeChart chart;
    private XYPlot plot;
    private XYSeries dataset1;

    private JLabel InterflowWeight;
    private JLabel percolation_weight;
    private JButton downstreamID;
    private JLabel ist_input;
    private JLabel cur_input;
	
    @SuppressWarnings("unchecked")
    @Override
    public void run() {       
        super.run();
        
        this.addSelectionChangeListener(new SelectionChangeListener(){
            @Override
            public void selectionChanged(SelectionChangedEvent evt){
                try {
		SimpleFeature f = getSelectedFeature();
		if (f != null) {
		    ShowActFunction(new Double(f.getID()).intValue());
		}
	    }
	    catch (Exception e1) {
		getContext().getModel().getRuntime().println(e1.toString());
	    }
            }
        });

        createPanel();
	}
                	
	protected void createPanel() {	    			    
	    //super.createPanel();
	    
	    try {	
		plot = new XYPlot();
		plot.setDomainAxis(new NumberAxis("Nitrogen Input"));
		plot.setRangeAxis(new NumberAxis("Nitrogen Output"));
		chart = new JFreeChart(plot);
        
		dataset1 = new XYSeries("Activation - Function");
                
		plot.setDataset(0, new XYSeriesCollection(dataset1));
		plot.setRenderer(0, new DefaultXYItemRenderer());
	           
		JFrame frame = new JFrame("Activation - Function");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new ChartPanel(chart), BorderLayout.CENTER);
        	    	    
		JPanel panel = new JPanel(new FlowLayout());
	
		idfield = new JTextArea(1,40);
		idfield.setEditable(true);
		panel.add(idfield, BorderLayout.WEST);
	    
		JButton bShow = new JButton("Show");
		
		
		//information panel				
		JLabel label1 = new JLabel("Interflow - Weight: ");
		JLabel label2 = new JLabel("Percolation - Weight: ");
		JLabel label3 = new JLabel("DownstreamID: ");
		JLabel label4 = new JLabel("Input (old): ");
		JLabel label5 = new JLabel("Input (new): ");
		
		InterflowWeight = new JLabel();
		percolation_weight = new JLabel();
		downstreamID = new JButton("-");
		
		downstreamID.addActionListener(new ActionListener() {
                @Override
		    public void actionPerformed(ActionEvent e)
		    {
			try {
			    ShowActFunction(new Integer(downstreamID.getText()).intValue());
			}
			catch(Exception e2) {
			}
		    }});
		
		ist_input = new JLabel();
		cur_input = new JLabel();
			
		JPanel infopanel = new JPanel(new SpringLayout());
		
		
		infopanel.add(label1);
		infopanel.add(InterflowWeight);
		
		infopanel.add(label2);
		infopanel.add(percolation_weight);
		
		infopanel.add(label3);
		infopanel.add(downstreamID);
		
		infopanel.add(label4);
		infopanel.add(ist_input);
		
		infopanel.add(label5);
		infopanel.add(cur_input);
						
		makeCompactGrid(infopanel,
                                5, 2, //rows, cols
                                6, 6,        //initX, initY
                                6, 6);       //xPad, yPad

		bShow.addActionListener(new ActionListener() {
                @Override
		    public void actionPerformed(ActionEvent e)
		    {
			try {
			    ShowActFunction(new Integer(idfield.getText()).intValue());
			}
			catch(Exception e2) {
			}
		    }});
		panel.add(bShow,BorderLayout.EAST);
	    
		frame.getContentPane().add(panel,BorderLayout.SOUTH);        
		frame.getContentPane().add(infopanel,BorderLayout.EAST);        
		frame.setBounds(50, 50, 800, 500);
		frame.setVisible(true);	    	    
	    } catch (Exception e) {
		this.getContext().getModel().getRuntime().println(e.toString());
	}


    }
	    
    void ShowActFunction(int id) throws Exception {	    
	Entity e = null;        
	for (int i = 0;i<hrus.getEntities().size();i++) {
	    e = hrus.getEntities().get(i);
                        
	    if (e.getDouble("ID") == id)
		break;
	}
	if (e == null)
	    return;
	NONeuron nitr_neuron = (NONeuron)e.getObject("NITROGEN_NEURON");
			
	GenericFunction f = nitr_neuron.getFilter(0);
	dataset1.clear();
	if ( f.getFunction().getType() ==  ActivationFunction.LINAPPROX) {
	    Matrix M = ((LinApprox)f.getFunction()).getData();
	    for (int i=0;i<M.rows;i++) {
		dataset1.add(M.element[i][0],M.element[i][1]);
	    }			    			    			    
	}
	
	idfield.setText(new Integer(id).toString());

	InterflowWeight.setText((new Double(((NONeuron)e.getObject("NITROGEN_NEURON")).getDownstreamWeight())).toString());
	percolation_weight.setText((new Double(((NONeuron)e.getObject("NITROGEN_NEURON")).getOutputWeight())).toString());
	ist_input.setText((new Double(((DistNeuron)e.getObject("DIST_NEURON")).getInitalExternInput())).toString());
	cur_input.setText((new Double(((DistNeuron)e.getObject("DIST_NEURON")).getInput())).toString());
        //if (e.getDouble("to_poly")
        Entity dsEntity = (Entity)e.getObject("to_poly");
        if (dsEntity!=null)
            downstreamID.setText(new Integer((new Double(dsEntity.getId())).intValue()).toString());
    }
    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    private static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}


