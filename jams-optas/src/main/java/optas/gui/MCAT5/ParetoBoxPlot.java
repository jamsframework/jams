package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.table.AbstractTableModel;
import optas.data.DataCollection;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.Statistics;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;

/**
 *
 * @author Nathan Lighthart
 */
public class ParetoBoxPlot extends MCAT5Plot {

    SimpleEnsemble[] parameterEnsembles;
    JTable objectiveTable = new JTable();
    ObjectiveTableModel objTableModel = null;
    BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
    CategoryPlot plot = null;
        
    JCheckBox showMean = new JCheckBox(JAMS.i18n("show_mean"),true);
    JCheckBox showMedian = new JCheckBox(JAMS.i18n("show_median"),true);
    boolean calcParetoFront = true;
    JCheckBox useParetoOptimalSolutionsOnly = new JCheckBox(JAMS.i18n("use_only_pareto_solutions"),calcParetoFront);
    
    
    JLabel alphaLabel = new JLabel("\u03B1: ");
    JTextField alphaField = new JTextField("0.1");
    JButton applyAlpha = new JButton(JAMS.i18n("Apply"));
    
    JLabel numLabel = new JLabel("n: ");
    JTextField numField = new JTextField("0.0");
    
    public ParetoBoxPlot() {
        init();
    }
    JPanel mainPanel = new JPanel();

    private void init() {
        CategoryAxis xAxis = new CategoryAxis("");
        CategoryAxis xAxis2 = new CategoryAxis(JAMS.i18n("Parameter"));
        CategoryAxis xAxis3 = new CategoryAxis("");
        NumberAxis yAxis = new NumberAxis(JAMS.i18n("NORMALISED_RANGE"));

        plot = new CategoryPlot(null, xAxis, yAxis, renderer);
        plot.setDomainAxis(1, xAxis2);
        plot.setDomainAxis(2, xAxis3);

        
        
        xAxis.setFixedDimension(20);
        xAxis2.setFixedDimension(20);
        xAxis3.setFixedDimension(20);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        renderer.setFillBox(true);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesFillPaint(0, Color.BLUE);
        renderer.setSeriesFillPaint(1, Color.BLUE);
        renderer.setSeriesFillPaint(2, Color.BLUE);
        
        renderer.setUseOutlinePaintForWhiskers(false);
        renderer.setMaximumBarWidth(0.33);
        
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setDomainGridlinesVisible(true);
        
        PatchedChartPanel chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(MAXIMUM_WIDTH);
        chartPanel.setMaximumDrawHeight(MAXIMUM_HEIGHT);

        chartPanel.setChart(chart);


        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);

        JScrollPane tableScrollPane = new JScrollPane(objectiveTable);
        /*tableScrollPane.setMinimumSize(new Dimension(400, 350));
        tableScrollPane.setMaximumSize(new Dimension(400, 350));
        tableScrollPane.setPreferredSize(new Dimension(400, 350));*/

        showMedian.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renderer.setMedianVisible(showMedian.isSelected());
                objTableModel.update();
            }
        });
        
        showMean.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renderer.setMeanVisible(showMean.isSelected());
                objTableModel.update();
            }
        });
        
        useParetoOptimalSolutionsOnly.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                calcParetoFront = useParetoOptimalSolutionsOnly.isSelected();
                objTableModel.update();
            }
        });
        
        ActionListener applyAlphaListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                double alpha = 0;
                try{
                    alpha = Double.parseDouble(alphaField.getText());
                }catch(NumberFormatException nfe){
                    alpha = 0;
                }
                
                for (int i=0;i<objTableModel.m;i++){
                    Integer sortedIds[] = objTableModel.objectives[i].sort();
                    int id = sortedIds[(int)(sortedIds.length*alpha)];
                    objTableModel.worstAcceptableValue[i] = objTableModel.objectives[i].getValue(id);
                }
                                
                objTableModel.update();
            }
        };
        
        alphaField.addActionListener(applyAlphaListener);        
        applyAlpha.addActionListener(applyAlphaListener);
        
        numField.setEditable(false);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel)
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING,false)     
                        .addComponent(tableScrollPane,200,425,500)
                        .addGroup(
                            layout.createSequentialGroup()
                            .addComponent(alphaLabel)
                            .addComponent(alphaField,35,50,75)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,5,10)
                            .addComponent(applyAlpha)                     
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,10,20)                            
                        )
                        .addComponent(useParetoOptimalSolutionsOnly) 
                        .addGroup(
                            layout.createSequentialGroup()
                            .addComponent(numLabel)
                            .addComponent(numField,35,50,75)
                        )
                        .addComponent(useParetoOptimalSolutionsOnly) 
                        .addComponent(showMedian)
                        .addComponent(showMean) 
                        
                    )
                );
        
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(chartPanel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tableScrollPane, 200, 350, 400)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 20, 20)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(alphaLabel, 20, 26, 30)
                            .addComponent(alphaField, 20, 26, 30)
                            .addComponent(applyAlpha, 20, 26, 30))
                            .addComponent(useParetoOptimalSolutionsOnly)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(numLabel, 20, 26, 30)
                                .addComponent(numField, 20, 26, 30))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showMedian)
                                .addComponent(showMean)
                                .addContainerGap(100, 2000)));
    }
    
    class ObjectiveTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{JAMS.i18n("selected"), JAMS.i18n("Name"), JAMS.i18n("best_value"), JAMS.i18n("worst_acceptable_value")};
        DataCollection dataSource = null;
        EfficiencyEnsemble objectives[] = null;
        
        private String name[] = null;
        private boolean selected[] = null;
        double bestValue[], worstAcceptableValue[];
        int m;

        ObjectiveTableModel(DataCollection dataSource) {
            this.dataSource = dataSource;

            Set<String> effDataSets = this.dataSource.getDatasets(Efficiency.class);
            m = effDataSets.size();

            name = new String[m];
            effDataSets.toArray(name);
            selected = new boolean[m];
            bestValue = new double[m];
            worstAcceptableValue = new double[m];
            Arrays.fill(worstAcceptableValue, Double.POSITIVE_INFINITY);
            Arrays.fill(selected, true);
            
            objectives = new EfficiencyEnsemble[m];
            for (int i=0;i<m;i++){
                objectives[i] = (EfficiencyEnsemble)dataSource.getDataSet(name[i]);
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return m;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return selected[row];
                case 1:
                    return name[row];
                case 2:
                    return bestValue[row];
                case 3:
                    return worstAcceptableValue[row];
                default:
                    return null;
            }
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return Double.class;
                case 3:
                    return Double.class;
                default:
                    return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;
                case 1:
                    return false;
                case 2:
                    return false;
                case 3:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                selected[row] = (Boolean) value;
                update();
            }
            if (col == 3) {
                worstAcceptableValue[row] = (Double) (value);
                update();
            }
        }

        public void update() {
            SampleFactory factory = new SampleFactory();
            getDataSource().constructSample(factory);
            Statistics stats = factory.getStatistics();

            ArrayList<SampleFactory.Sample> paretoFront = null;
            if (calcParetoFront) {
                paretoFront = stats.getParetoSubset(selected);
            }
            else {
                paretoFront = factory.getSampleList();
            }

            for (int j = 0; j < m; j++) {
                bestValue[j] = Double.POSITIVE_INFINITY;
                for (int i = 0; i < paretoFront.size(); i++) {
                    bestValue[j] = Math.min(paretoFront.get(i).F()[j], bestValue[j]);
                }
                /*if (objectives[j].isPositiveBest()){
                    bestValue[j] = -bestValue[j];
                }*/
            }

            int i = 0;
            while (i < paretoFront.size()) {
                for (int j = 0; j < m; j++) {
                    if (!selected[j])
                        continue;
                    if (paretoFront.get(i).F()[j] > worstAcceptableValue[j]) {
                        paretoFront.set(i, paretoFront.get(paretoFront.size() - 1));
                        paretoFront.remove(paretoFront.size() - 1);
                        i--;
                        break;
                    }
                }
                i++;
            }
            numField.setText(Integer.toString(paretoFront.size()));
            updateBoxPlot(paretoFront);
            fireTableDataChanged();
        }
    }

    private void initDataSet() {
        objTableModel = new ObjectiveTableModel(this.getDataSource());

        objectiveTable.setModel(objTableModel);

        Set<String> parameterSet = this.getDataSource().getDatasets(Parameter.class);
        parameterEnsembles = new SimpleEnsemble[parameterSet.size()];
        int counter = 0;
        for (String name : parameterSet) {
            parameterEnsembles[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        objectiveTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        objectiveTable.getColumnModel().getColumn(1).setPreferredWidth(115);
        objectiveTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        objectiveTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        objectiveTable.getTableHeader().setPreferredSize(new Dimension(objectiveTable.getTableHeader().getPreferredSize().width, 40));
        objectiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        objTableModel.update();
    }

    public void updateBoxPlot(ArrayList<Sample> candidates) {
        CategoryAxis xAxis = plot.getDomainAxis(1);
        xAxis.setLabel("");
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));

        xAxis = plot.getDomainAxis(0);
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));
        xAxis.setLabel("");
                
        xAxis = plot.getDomainAxis(2);
        xAxis.setLabel("");
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));
        plot.getRangeAxis().setRange(0, 1.0);
        
        double min[] = new double[parameterEnsembles.length],
               max[] = new double[parameterEnsembles.length];
        
        // Add tooltip so the actual min and max can be known
        String name;
        SimpleEnsemble ensemble;
        for (int parameterNumber = 0; parameterNumber < parameterEnsembles.length; parameterNumber++) {
            ensemble = parameterEnsembles[parameterNumber];
            name = ensemble.name;
            xAxis.addCategoryLabelToolTip(name, name + " (Min: " + ensemble.getMin() + " Max: " + ensemble.getMax() + ")");            
            
            min[parameterNumber] = (ensemble.getMin());
            max[parameterNumber] = (ensemble.getMax());
        }
                        
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        DefaultBoxAndWhiskerCategoryDataset dataset1 = new DefaultBoxAndWhiskerCategoryDataset();
        DefaultBoxAndWhiskerCategoryDataset dataset2 = new DefaultBoxAndWhiskerCategoryDataset();
        
        List<List<Double>> parameterValues = new ArrayList<List<Double>>(parameterEnsembles.length);
        for (int i = 0; i < parameterEnsembles.length; i++) {
            parameterValues.add(new ArrayList<Double>());
        }
        for (Sample sample : candidates) {
            for (int i = 0; i < sample.x.length; i++) {
                parameterValues.get(i).add(sample.x[i]);
            }
        }

        parameterValues = normalize(parameterValues);

        int parameterNumber = 0;
        for (List<Double> parameter : parameterValues) {
            dataset.add(parameter, "Single Series1", parameterEnsembles[parameterNumber].name);
            
            dataset1.add(parameter, "Single Series2", new DoubleFormatter(min[parameterNumber]));
            dataset2.add(parameter, "Single Series3", new DoubleFormatter(max[parameterNumber]));
            
            double c = 1E-10;
            while (dataset1.getColumnCount()<=parameterNumber){
                dataset1.add(parameter, "Single Series2", new DoubleFormatter(min[parameterNumber]));
                c+=1E-10;
            }
            while (dataset2.getColumnCount()<=parameterNumber){
                dataset2.add(parameter, "Single Series3", new DoubleFormatter(max[parameterNumber]));
                c+=1E-10;
            }
            
            parameterNumber++;
        }

        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToDomainAxis(2, 2);        
        
        plot.setDataset(0,dataset1);
        plot.setDataset(1,dataset);
        plot.setDataset(2,dataset2);
        
        
        plot.setDomainAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
        plot.setDomainAxisLocation(2, AxisLocation.TOP_OR_LEFT);
        
    }

    @Override
    public void refresh() throws NoDataException {
        initDataSet();
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    private List<List<Double>> normalize(List<List<Double>> parameterValues) {
        int parameterNumber = 0;
        for (List<Double> list : parameterValues) {
            double max = parameterEnsembles[parameterNumber].getMax();
            double min = parameterEnsembles[parameterNumber].getMin();
            double difference = max - min;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, (list.get(i) - min) / difference);
            }
            parameterNumber++;
        }
        return parameterValues;
    }

    public static double roundToSignificantFigures(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    private class DoubleFormatter implements Comparable<Double>{
        double value = 0;
        public DoubleFormatter(double value){
            this.value = value;
        }

        @Override
        public int compareTo(Double o) {
            if (o instanceof Double){
                return ((Double)o).compareTo(o);
            }
            return 0;
        }
        
        DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
        
        @Override
        public String toString(){
            
            return df2.format(roundToSignificantFigures(value,3));
        }
    }
    
    public static void main(String[] args) {
        DataCollection dc = DataCollection.createFromFile(new File("E:\\ModelData\\Testgebiete\\J2000\\Gehlberg\\output\\20140424_131138\\new_nsga_sample.cdat"));

        try {
            DataRequestPanel d = new DataRequestPanel(new ParetoBoxPlot(), dc);
            JFrame plotWindow = new JFrame("test");
            plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            plotWindow.setLayout(new BorderLayout());
            plotWindow.setSize(800, 700);
            plotWindow.add(d, BorderLayout.CENTER);
            plotWindow.pack();
            plotWindow.setVisible(true);
        } catch (NoDataException nde) {
        }
    }
}
