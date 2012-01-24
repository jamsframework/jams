/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import jams.data.Attribute.TimeInterval;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.hydro.data.DataCollection;
import optas.hydro.data.Measurement;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeFilterFactory;
import optas.hydro.data.TimeFilterFactory.BaseFlowTimeFilter;
import optas.hydro.data.TimeFilterFactory.EventFilter;
import optas.hydro.data.TimeFilterFactory.MonthlyTimeFilter;
import optas.hydro.data.TimeFilterFactory.YearlyTimeFilter;
import optas.hydro.data.TimeSerie;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author chris
 */
public class TimeFilterDialog extends JDialog{
    TimeSerie timeserie;

    TimeFilter filter;

    JList yearList = new JList();
    JCheckBox hydrologicYearBox = new JCheckBox("Hydrologic Year");

    JList monthList = new JList(new String[]{"January", "Febuary", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
    final static String springString = "Spring",
                        summerString = "Summer",
                        autumnString = "Autumn",
                        winterString = "Winter",
                        winterHalfYearString = "Winter halfyear",
                        summerHalfYearString = "Summer halfyear";

    JComboBox seasonBox = new JComboBox(new String[]{springString, summerString, autumnString, winterString,winterHalfYearString,summerHalfYearString });

    JTextField baseFlowRunoffQuantity = new JTextField(10);
    JRadioButton baseFlowFixedEstimation = new JRadioButton("Fixed");
    JRadioButton baseFlowLocalMiniumEstimation = new JRadioButton("Local Minimum");
    HydrographChart hydrographBaseFlow = null;
    HydrographChart hydrographHydroEvent = null;

    final static String raisingEdgeString = "raising edges",
                        peakString = "peaks",
                        fallingEdgeString = "falling edges";

    JComboBox hydroEventTypeBox = new JComboBox(new String[]{raisingEdgeString, peakString, fallingEdgeString});
    JTextField windowSizeField = new JTextField(10);
    JSlider qualitySlider = new JSlider();
    JTextField qualitySliderValue = new JTextField(10);

    Measurement m = null;

    boolean isApproved = false;

    public TimeFilterDialog(TimeSerie timeserie){
        this.timeserie = timeserie;
        init(timeserie,null);
    }

    static public boolean isApplicable(DataCollection dc){
        return !dc.getDatasets(TimeSerie.class).isEmpty();
    }

    private JPanel constructYearTimeFilterPanel(YearlyTimeFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select years"));

        TimeInterval t = timeserie.getTimeDomain();

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(t.getStart().getTime());
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(t.getEnd().getTime());
        int endYear = calendar.get(Calendar.YEAR);

        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < endYear - startYear; i++) {
            model.add(i, new Integer(i + startYear));
        }

        yearList.setModel(model);

        yearList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        filterPanel.add(yearList, BorderLayout.CENTER);
        if (filter != null) {
            int selectedYears[] = filter.getYears();
            int indicies[] = new int[selectedYears.length];
            for (int i = 0; i < indicies.length; i++) {
                indicies[i] = selectedYears[i] - startYear;
            }
            yearList.setSelectedIndices(indicies);

            this.hydrologicYearBox.setSelected(filter.isHydrologicYear());
        }

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(hydrologicYearBox, BorderLayout.CENTER);
        dataPanel.add(new JButton("Ok") {
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        List oYears = yearList.getSelectedValuesList();
                        int years[] = new int[oYears.size()];
                        for (int i = 0; i < oYears.size(); i++) {
                            years[i] = ((Integer) oYears.get(i)).intValue();
                        }
                        TimeFilterDialog.this.filter = TimeFilterFactory.getYearlyFilter(years,
                                TimeFilterDialog.this.hydrologicYearBox.isSelected());
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);
        filterPanel.add(dataPanel,BorderLayout.SOUTH);

        return filterPanel;
    }

    private JPanel constructMonthlyTimeFilterPanel(MonthlyTimeFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select months"));

        monthList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        filterPanel.add(monthList, BorderLayout.CENTER);
        if (filter != null) {
            monthList.setSelectedIndices(filter.getMonths());
        }

        seasonBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (seasonBox.getSelectedItem().equals(springString)){
                    monthList.setSelectedIndices(new int[]{3,4,5});
                }
                if (seasonBox.getSelectedItem().equals(summerString)){
                    monthList.setSelectedIndices(new int[]{6,7,8});
                }
                if (seasonBox.getSelectedItem().equals(autumnString)){
                    monthList.setSelectedIndices(new int[]{9,10,11});
                }
                if (seasonBox.getSelectedItem().equals(winterString)){
                    monthList.setSelectedIndices(new int[]{0,1,2});
                }
                if (seasonBox.getSelectedItem().equals(summerHalfYearString)){
                    monthList.setSelectedIndices(new int[]{4,5,6,7,8,9});
                }
                if (seasonBox.getSelectedItem().equals(winterHalfYearString)){
                    monthList.setSelectedIndices(new int[]{9,10,11,1,2,3});
                }
            }
        });

        filterPanel.add(seasonBox, BorderLayout.NORTH);

        filterPanel.add(new JButton("Ok") {

            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        int oMonths[] = monthList.getSelectedIndices();
                        TimeFilterDialog.this.filter = TimeFilterFactory.getMonthlyFilter(oMonths);
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);


        return filterPanel;
    }
    
    private TimeFilter constructHydroEventFilter(){
        int windowSize = 10;

        try{
            windowSize = Integer.parseInt(windowSizeField.getText());
        }catch(NumberFormatException pe){
            JOptionPane.showMessageDialog(rootPane, "Please enter a valid window value");
        }

        this.hydrographHydroEvent.setGroundwaterWindowSize(windowSize);

        EventFilter filter = null;
        if (hydroEventTypeBox.getSelectedItem().equals(raisingEdgeString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.RaisingEdge, windowSize);
        }else if (hydroEventTypeBox.getSelectedItem().equals(fallingEdgeString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.Recession, windowSize);
        }else if (hydroEventTypeBox.getSelectedItem().equals(peakString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.Peak, windowSize);
        }

        if (filter==null)
            return null;
        
        this.qualitySlider.setMinimum((int)(filter.getMinQuality()*100.0));
        this.qualitySlider.setMaximum((int)(filter.getMaxQuality()*100.0));

        return filter;
    }

    ActionListener updateHydroEventListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            hydrographHydroEvent.setFilter(constructHydroEventFilter());
        }
    };

    private JPanel constructHydroEventFilterPanel(TimeSerie m, EventFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select hydrograph event filter"));

        hydrographHydroEvent = new HydrographChart();
        hydrographHydroEvent.setHydrograph(m);

        JPanel northPanel = new JPanel(new FlowLayout());

        JPanel confPanel = new JPanel(new FlowLayout());
        confPanel.add(new JLabel("Select window size"));
        windowSizeField.setText("50");
        confPanel.add(windowSizeField);
        confPanel.add(new JLabel("Select type of event"));
        confPanel.add(hydroEventTypeBox);

        confPanel.add(new JLabel("Select quality"));
        confPanel.add(qualitySlider);

        confPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Configuration"));

        northPanel.add(confPanel, BorderLayout.CENTER);

        filterPanel.add(northPanel, BorderLayout.NORTH);

        ChartPanel chartPanel = new ChartPanel(hydrographHydroEvent.getChart(), true);
        filterPanel.add(chartPanel, BorderLayout.CENTER);

        filterPanel.add(new JButton("Ok") {

            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = constructHydroEventFilter();
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);

        hydroEventTypeBox.addActionListener(updateHydroEventListener);
        windowSizeField.addActionListener(updateHydroEventListener);

        if (filter != null){
            EventFilter eventFilter = (EventFilter)filter;
            this.timeserie = eventFilter.getTimeSerie();
            this.windowSizeField.setText(Integer.toString(eventFilter.getWindowSize()));

            switch (eventFilter.getFilteredEventType()){
                case Peak: this.hydroEventTypeBox.setSelectedItem(peakString); break;
                case RaisingEdge: this.hydroEventTypeBox.setSelectedItem(raisingEdgeString); break;
                case Recession: this.hydroEventTypeBox.setSelectedItem(fallingEdgeString); break;
                default: break;
            }

            this.qualitySlider.setMinimum((int)(filter.getMinQuality()*100.0));
            this.qualitySlider.setMaximum((int)(filter.getMaxQuality()*100.0));
        }


        qualitySlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                if (TimeFilterDialog.this.hydrographHydroEvent.filter!=null){
                    ((EventFilter)TimeFilterDialog.this.hydrographHydroEvent.filter).setQualityThreshold(qualitySlider.getValue()/100.0);
                    hydrographHydroEvent.setFilter(TimeFilterDialog.this.hydrographHydroEvent.filter);

                }
            }
        });
        return filterPanel;
    }

    private TimeFilter constructBaseFlowFilter(){
        double threshold = 1.0;

        try{
            threshold = Double.parseDouble(baseFlowRunoffQuantity.getText());
        }catch(NumberFormatException pe){
            JOptionPane.showMessageDialog(rootPane, "Please enter a valid threshold");
        }
        if (baseFlowFixedEstimation.isSelected()){
            return TimeFilterFactory.getBaseFlowTimeFilter(timeserie, BaseFlowTimeFilter.Method.Fixed, threshold);
        }else{
            return TimeFilterFactory.getBaseFlowTimeFilter(timeserie, BaseFlowTimeFilter.Method.HYSEPLocalMinimum, threshold);
        }
    }

    ActionListener updateBaseFlowListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            hydrographBaseFlow.setFilter(constructBaseFlowFilter());
        }
    };

    private JPanel constructBaseFlowTimeFilterPanel(TimeSerie m, BaseFlowTimeFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select baseflow-filter"));

        hydrographBaseFlow = new HydrographChart();
        hydrographBaseFlow.setHydrograph(m);

        ButtonGroup methodButtonGroup = new ButtonGroup();
        methodButtonGroup.add(baseFlowFixedEstimation);
        methodButtonGroup.add(baseFlowLocalMiniumEstimation);

        JPanel northPanel = new JPanel(new FlowLayout());

        JPanel methodPanel = new JPanel(new FlowLayout());
        methodPanel.add(baseFlowFixedEstimation);
        methodPanel.add(baseFlowLocalMiniumEstimation);
        methodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Method"));

        northPanel.add(methodPanel, BorderLayout.WEST);
        northPanel.add(new JLabel("Threshold"), BorderLayout.CENTER);
        northPanel.add(this.baseFlowRunoffQuantity, BorderLayout.EAST);

        filterPanel.add(northPanel, BorderLayout.NORTH);

        ChartPanel chartPanel = new ChartPanel(hydrographBaseFlow.getChart(), true);
        filterPanel.add(chartPanel, BorderLayout.CENTER);
        
        filterPanel.add(new JButton("Ok") {

            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = constructBaseFlowFilter();
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);

        baseFlowFixedEstimation.addActionListener(updateBaseFlowListener);
        baseFlowLocalMiniumEstimation.addActionListener(updateBaseFlowListener);
        baseFlowFixedEstimation.addActionListener(updateBaseFlowListener);

        if (filter != null){
            BaseFlowTimeFilter baseFlowFilter = (BaseFlowTimeFilter)filter;
            this.timeserie = baseFlowFilter.getTimeSerie();
            this.baseFlowRunoffQuantity.setText(Double.toString(baseFlowFilter.getThreshold()));
            if (baseFlowFilter.getMethod() == BaseFlowTimeFilter.Method.Fixed)
                this.baseFlowFixedEstimation.setSelected(true);
            else
                this.baseFlowLocalMiniumEstimation.setSelected(true);

        }

        return filterPanel;
    }

    public void init(TimeSerie serie, TimeFilter filter){
        this.filter = filter;

        JTabbedPane pane = new JTabbedPane();

        //yearly filter
        if (filter instanceof YearlyTimeFilter || filter == null) {
            YearlyTimeFilter yearlyFilter = null;
            if (filter!=null){
                yearlyFilter = (YearlyTimeFilter)filter;
            }
            
            pane.addTab("Yearly Filter", constructYearTimeFilterPanel(yearlyFilter));
        }

        if (filter instanceof MonthlyTimeFilter || filter == null) {
            MonthlyTimeFilter yearlyFilter = null;
            if (filter!=null){
                yearlyFilter = (MonthlyTimeFilter)filter;
            }

            pane.addTab("Monthly Filter", constructMonthlyTimeFilterPanel(yearlyFilter));
        }

        if (filter instanceof BaseFlowTimeFilter || filter == null) {
            BaseFlowTimeFilter baseFlowFilter = null;
            if (filter!=null){
                baseFlowFilter = (BaseFlowTimeFilter)filter;
            }

            pane.addTab("Baseflow Filter", constructBaseFlowTimeFilterPanel(serie, baseFlowFilter));
        }

        if (filter instanceof EventFilter || filter == null) {
            EventFilter eventFilter = null;
            if (filter!=null){
                eventFilter = (EventFilter)filter;
            }

            pane.addTab("Hydrograph Event Filter", constructHydroEventFilterPanel(serie, eventFilter));
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(pane);

        this.getContentPane().removeAll();
        this.getContentPane().add(mainPanel);
        this.pack();
        this.setModal(true);
    }
    
    public boolean getApproval(){
        return isApproved;
    }
    public TimeFilter getFilter(){
        return this.filter;
    }
    @Override
    public void setVisible(boolean b){
        if (b){
            this.isApproved = false;
        }
        super.setVisible(b);
    }
}
