/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import optas.gui.wizard.HydrographChart;
import optas.SA.TemporalSensitivityAnalysis;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.hydro.gui.WeightChart;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author chris
 */
public class TemporalSensitivityAnalysisGUI extends MCAT5Plot {

    int MAX_WEIGHTS = 40;

    public static Color[] getDifferentColors(int n) {
        Color[] cols = new Color[n];

        for (int i = 0; i < n; i++) {
            cols[i] = Color.getHSBColor((float) i / n, 1.0f, 1.0f);
        }
        return cols;
    }
    Color standardColorList[] = getDifferentColors(MAX_WEIGHTS);
    HydrographChart chart;
    WeightChart weightChart;   
    XYLineAndShapeRenderer weightRenderer[] = new XYLineAndShapeRenderer[MAX_WEIGHTS];    
    JTable parameterTable = new JTable(new Object[][]{{Boolean.TRUE,Boolean.TRUE,"test",Color.black}},new String[]{"x","y","z","a"});
    JLabel infoLabel = new JLabel("Dominance:?");
    
    SimpleEnsemble p[] = null;
    EfficiencyEnsemble e = null;
    TimeSerieEnsemble ts = null;
    Measurement obs = null;
    TemporalSensitivityAnalysis temporalAnalysis = null;
    JPanel mainPanel = null;

    public TemporalSensitivityAnalysisGUI() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    public class ColorRenderer extends JLabel
            implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            return this;
        }
    }

    public class ColorEditor extends AbstractCellEditor
            implements TableCellEditor,
            ActionListener {

        Color currentColor;
        JButton button;
        JColorChooser colorChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            //Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button,
                    "Pick a Color",
                    true, //modal
                    colorChooser,
                    this, //OK button handler
                    null); //no CANCEL button handler
        }

        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                fireEditingStopped(); //Make the renderer reappear.

            } else { //User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentColor;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            currentColor = (Color) value;
            return button;
        }
    }

    class ParameterTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"show", "name", "enabled", "color"};
        private Object[][] data = null;

        ParameterTableModel(SimpleEnsemble p[]) {
            data = new Object[p.length][3];
            for (int i=0;i<p.length;i++){
                data[i][0] = Boolean.TRUE;
                data[i][1] = p[i];
                data[i][2] = Boolean.TRUE;
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            if (col == 3)
                return TemporalSensitivityAnalysisGUI.this.standardColorList[row];
            else
                return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return Color.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;
                case 1:
                    return false;
                case 2:
                    return true;
                case 3:
                    return true;
            }
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col==2 && ((Boolean)value).booleanValue()==false)
                setValueAt(Boolean.FALSE, row, 0);
            if (col==0 && ((Boolean)value).booleanValue()==true)
                setValueAt(Boolean.TRUE, row, 2);
            if (col==3)
                TemporalSensitivityAnalysisGUI.this.standardColorList[row] = (Color)value;
            else
                data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

    private void init() {
        weightChart = new WeightChart();
        chart = new HydrographChart();

        chart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                weightChart.getXYPlot().setDomainAxis(chart.getXYPlot().getDomainAxis());
            }
        });

        weightChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                chart.getXYPlot().setDomainAxis(weightChart.getXYPlot().getDomainAxis());
            }
        });

        for (int i = 0; i < MAX_WEIGHTS; i++) {
            weightRenderer[i] = new XYLineAndShapeRenderer();
            weightRenderer[i].setBaseFillPaint(this.standardColorList[i]);
            weightRenderer[i].setBaseLinesVisible(true);
            weightRenderer[i].setBaseShapesVisible(false);
            weightRenderer[i].setBaseSeriesVisible(true);
            weightRenderer[i].setDrawSeriesLineAsPath(true);
            weightRenderer[i].setStroke(new BasicStroke(1.0f));
        }

        ChartPanel weightChartPanel = new ChartPanel(weightChart.getChart(), true);

        ChartPanel chartPanel = new ChartPanel(chart.getChart(), true);
        chartPanel.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity e = event.getEntity();
                if (e != null && e instanceof XYItemEntity) {
                    XYItemEntity xy = (XYItemEntity) e;
                    int index = xy.getSeriesIndex();
                    int data = xy.getItem();

                    System.out.println("index:" + index);
                    System.out.println("data:" + data);

                    DataCollection dc = new DataCollection();
                    dc.addEnsemble(ts.get(data));
                    for (int i = 0; i < p.length; i++) {
                        dc.addEnsemble(p[i]);
                    }
                    try {
                        DataRequestPanel d = new DataRequestPanel(new DottyPlot(), dc);
                        JFrame plotWindow = MCAT5Toolbar.getDefaultPlotWindow("test");
                        plotWindow.add(d, BorderLayout.CENTER);
                        plotWindow.setVisible(true);
                    } catch (NoDataException nde) {
                        System.out.println(nde.toString());
                    }

                    TimeSeriesCollection collection = (TimeSeriesCollection) chart.getXYPlot().getDataset(index);
                    System.out.println(collection.getSeries(0).getDataItem(data).getPeriod());
                    System.out.println(collection.getSeries(0).getDataItem(data).getValue());
                }

            }

            public void chartMouseMoved(ChartMouseEvent event) {
            }
        });

        ChartPanel hydrographChartPanel = new ChartPanel(chart.getChart(), true);
        
        JScrollPane parameterTablePane = new JScrollPane(parameterTable);


        JPanel sideBar = new JPanel();
        GroupLayout sideLayout = new GroupLayout(sideBar);
        sideLayout.setAutoCreateGaps(true);
        sideLayout.setAutoCreateContainerGaps(true);
        sideBar.setLayout(sideLayout);
        sideLayout.setHorizontalGroup(
                sideLayout.createParallelGroup()
                .addComponent(parameterTablePane)
                .addComponent(infoLabel)            
                );
        
        sideLayout.setVerticalGroup(
                sideLayout.createSequentialGroup()
                    .addComponent(parameterTablePane)
                    .addComponent(infoLabel)
                );

        mainPanel = new JPanel();
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainLayout.setAutoCreateGaps(true);
        mainLayout.setAutoCreateContainerGaps(true);
        mainPanel.setLayout(mainLayout);

        mainLayout.setHorizontalGroup(
                mainLayout.createSequentialGroup()
                .addGroup(mainLayout.createParallelGroup()
                    .addComponent(weightChartPanel)
                    .addComponent(hydrographChartPanel))
                    .addComponent(sideBar));
        
        mainLayout.setVerticalGroup(
                mainLayout.createParallelGroup()
                .addGroup(mainLayout.createSequentialGroup()
                    .addComponent(weightChartPanel)
                    .addComponent(hydrographChartPanel))
                    .addComponent(sideBar));
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> data[] = getData(new int[]{0, 1, 2});
        ts = (TimeSerieEnsemble) data[0].get(0);
        obs = (Measurement) data[1].get(0);
        e = (EfficiencyEnsemble) data[2].get(0);

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        p = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            p[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        parameterTable.setModel(new ParameterTableModel(p));
        parameterTable.setDefaultEditor(Color.class, new ColorEditor());
        parameterTable.getColumnModel().getColumn(3).setCellRenderer(new ColorRenderer(true));
        parameterTable.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                weightChart.update(temporalAnalysis.calculate(), null, p, obs, getEnableList(), getShowList(), standardColorList);
            }
        });
        this.temporalAnalysis = new TemporalSensitivityAnalysis(p, e, ts, obs);
        this.temporalAnalysis.calculate();

        this.chart.setHydrograph(obs);
        this.weightChart.update(temporalAnalysis.calculate(), null, p, obs, getEnableList(), getShowList(), standardColorList);
    }

    private boolean[] getEnableList() {
        ParameterTableModel model = (ParameterTableModel)this.parameterTable.getModel();

        boolean parameterActive[] = new boolean[model.getRowCount()];
        for (int i = 0; i < parameterActive.length; i++) {
            if (((Boolean) model.getValueAt(i, 2)) == true) {
                parameterActive[i] = true;
            } else {
                parameterActive[i] = false;
            }
        }
        return parameterActive;
    }

    private boolean[] getShowList() {
        ParameterTableModel model = (ParameterTableModel)this.parameterTable.getModel();

        boolean parameterActive[] = new boolean[model.getRowCount()];
        for (int i = 0; i < parameterActive.length; i++) {
            if (((Boolean) model.getValueAt(i, 0)) == true) {
                parameterActive[i] = true;
            } else {
                parameterActive[i] = false;
            }
        }
        return parameterActive;
    }
}
