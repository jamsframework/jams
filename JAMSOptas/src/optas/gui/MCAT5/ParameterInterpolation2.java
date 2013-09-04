/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.regression.SimpleNeuralNetwork;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ParameterInterpolation2 extends MCAT5Plot {

    protected XYPlot plot = new XYPlot();
    protected PatchedChartPanel chartPanel = null;
    XYBlockRenderer bg_renderer = new XYBlockRenderer();

    JPanel panel = new JPanel(new BorderLayout());
    JSlider slider = new JSlider();

    EfficiencyEnsemble y;
    SimpleEnsemble[] params;

    SimpleEnsemble param1, param2;
    double p1Min,p1Max;
    double p2Min,p2Max;
    int n=0;

    SimpleNeuralNetwork knn;    
    final int RESOLUTION = 100;

    int currentIndex = -1;
    double paramMin, paramMax;
    double point[];

    final JList list = new JList(new String[]{"test"});

    JLabel blueLabel = new JLabel(" ");
    JLabel minLabel = new JLabel(" ");
    JLabel redLabel = new JLabel(" ");
    JLabel maxLabel = new JLabel(" ");

    public ParameterInterpolation2() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER")+"1",Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER")+"2",Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"),Efficiency.class));

        init();
    }

    private void init(){
        LookupPaintScale paintScale = new LookupPaintScale(0,1,new Color(0,0,0));
        for (int i=0;i<=255;i++){
            Color interpolated = new Color(i,0,255-i);
            paintScale.add((double)i / 255.0, interpolated);
        }
        //setup renderer
        bg_renderer.setPaintScale(paintScale);
        bg_renderer.setBlockWidth(1.00);
        bg_renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        //setup plot
        plot.setRenderer(bg_renderer);
        //setup chart
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("Interpolation");
        chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        panel.add(chartPanel,BorderLayout.CENTER);

        JPanel adjustmentPanel = new JPanel(new BorderLayout());

        slider.setBorder(BorderFactory.createTitledBorder("Parameter Space"));
        slider.setMaximum(99);
        slider.setMinimum(0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener(){
           public void stateChanged(ChangeEvent evt){
               point[currentIndex] = (((paramMax-paramMin)*(double)slider.getValue())/(double)RESOLUTION)+paramMin;
               updatePlot();
            }
        });
        adjustmentPanel.add(slider, BorderLayout.CENTER);

        JPanel colorPanel = new JPanel();
        GroupLayout layout = new GroupLayout(colorPanel);
        colorPanel.setLayout(layout);
        layout.setVerticalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addComponent(redLabel)
                    .addComponent(blueLabel)
                    )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(this.minLabel)
                    .addComponent(this.maxLabel)
                    )
                );

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(redLabel)
                    .addComponent(blueLabel)
                    )
                .addGroup(layout.createParallelGroup()
                    .addComponent(this.minLabel)
                    .addComponent(this.maxLabel)
                    )
                );

        redLabel.setOpaque(true);
        blueLabel.setOpaque(true);
        redLabel.setBackground(Color.red);
        blueLabel.setBackground(Color.blue);



        adjustmentPanel.add(colorPanel, BorderLayout.SOUTH);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        list.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e){
                currentIndex = list.getSelectedIndex();
                NumberFormat f = NumberFormat.getInstance();

                if (currentIndex != -1){
                    paramMin = params[currentIndex].getMin();
                    paramMax = params[currentIndex].getMax();
                    Dictionary labels = new Hashtable<Integer,JLabel>();
                    for (int i=0;i<=100;i+=20){
                        labels.put(i, new JLabel(f.format( (double)i*((paramMax - paramMin)/100.0)+paramMin )));
                    }
                    slider.setLabelTable(labels);
                    slider.setValue( (int)Math.round((point[currentIndex]-paramMin)/(paramMax-paramMin)*(double)RESOLUTION));
                    slider.setEnabled(true);
                    updatePlot();
                }else{
                    slider.setEnabled(false);
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 200));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("Parameter"),BorderLayout.NORTH);
        listPanel.add(listScroller,BorderLayout.SOUTH);
        adjustmentPanel.add(listPanel,BorderLayout.NORTH);

        panel.add(adjustmentPanel,BorderLayout.EAST);

        redraw();
    }

    public void updatePlot(){
        int K = RESOLUTION;
        DefaultXYZDataset xyz_dataset = new DefaultXYZDataset();

        int index1 = 0;
        int index2 = 0;

        for (int i=0;i<n;i++){
            if (this.params[i].name.equals(this.param1.name))
                index1 = i;
            if (this.params[i].name.equals(this.param2.name))
                index2 = i;
        }

        double dotMap[][] = new double[3][K*K];
        double x[] = Arrays.copyOf(point, point.length);

        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                x[index1] = p1Min+i*(p1Max-p1Min)/100.0;
                x[index2] = p2Min+j*(p2Max-p2Min)/100.0;

                dotMap[0][i*K+j] = x[index1];
                dotMap[1][i*K+j] = x[index2];
                double v[] = this.knn.getInterpolatedValue(x);
                dotMap[2][i*K+j] = v[0];
                yMax = Math.max(yMax,v[0]);
                yMin = Math.min(yMin,v[0]);               
            }
        }
        minLabel.setText(Double.toString(yMin));
        maxLabel.setText(Double.toString(yMax));

        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                dotMap[2][i*K+j] = (dotMap[2][i*K+j] - yMin)/(yMax-yMin);
            }
        }
        xyz_dataset.addSeries(0, dotMap);

        bg_renderer.setBlockHeight((p2Max - p2Min) / 100.0);
        bg_renderer.setBlockWidth((p1Max - p1Min) / 100.0);

        plot.setDataset(0, xyz_dataset);        
    }

    @Override
    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled())
            return;

        ArrayList<DataSet> p[] = getData(new int[]{0,1,2});

        param1 = (SimpleEnsemble)p[0].get(0);
        param2 = (SimpleEnsemble)p[1].get(0);

        p1Min = param1.getMin();
        p2Min = param2.getMin();
        
        p1Max = param1.getMax();
        p2Max = param2.getMax();

        y = (EfficiencyEnsemble)p[2].get(0);

        plot.setDomainAxis(new NumberAxis(param1.getName()));
        plot.setRangeAxis(new NumberAxis(param2.getName()));

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        params = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            params[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }
        n = params.length;

                Object[] listItem = new Object[params.length];
        for (int i=0;i<params.length;i++){
            listItem[i] = params[i].name;
        }

        DefaultListModel model = new DefaultListModel();
        for (int i=0;i<n;i++){
            model.addElement(listItem[i]);
        }
        this.list.setModel(model);
        
        knn = new SimpleNeuralNetwork();
        knn.setComplexityAdjustmentFactor(2);
        knn.setData(params, y);
        knn.init();
                                
        

        point = new double[n];
        for (int i=0;i<n;i++){
            point[i] = (this.params[i].getMax()+this.params[i].getMin())*0.5;
        }

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setRange(p1Min, p1Max);
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setRange(p2Min, p2Max);
        }
        
        updatePlot();
    }

    
    
    @Override
    public JPanel getPanel() {
        return panel;
    }
}
