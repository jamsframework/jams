/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.wizard;

import jams.JAMS;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import jams.gui.input.CalendarInput;
import jams.gui.input.ValueChangeListener;
import jams.gui.tools.GUIState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import optas.data.DataCollection;
import optas.data.TimeFilter;
import optas.data.TimeFilterFactory;
import optas.data.TimeFilterFactory.BaseFlowTimeFilter;
import optas.data.TimeFilterFactory.EventFilter;
import optas.data.TimeFilterFactory.MonthlyTimeFilter;
import optas.data.TimeFilterFactory.RangeTimeFilter;
import optas.data.TimeFilterFactory.YearlyTimeFilter;
import optas.data.TimeSerie;
import optas.tools.PatchedChartPanel;


/**
 *
 * @author chris
 */
public class TimeFilterDialog extends JDialog{
    TimeSerie timeserie;        
    TimeFilter filter;

    JList yearList = new JList();
    JCheckBox hydrologicYearBox = new JCheckBox(JAMS.i18n("Hydrologic_Year"));

    final private JList monthList = new JList(new String[]{
            JAMS.i18n("January"), 
            JAMS.i18n("Febuary"), 
            JAMS.i18n("March"), 
            JAMS.i18n("April"), 
            JAMS.i18n("May"), 
            JAMS.i18n("June"), 
            JAMS.i18n("July"), 
            JAMS.i18n("August"), 
            JAMS.i18n("September"), 
            JAMS.i18n("October"), 
            JAMS.i18n("November"), 
            JAMS.i18n("December")});
    
    final static String springString = JAMS.i18n("Spring"),
                        summerString = JAMS.i18n("Summer"),
                        autumnString = JAMS.i18n("Autumn"),
                        winterString = JAMS.i18n("Winter"),
                        winterHalfYearString = JAMS.i18n("Winter_halfyear"),
                        summerHalfYearString = JAMS.i18n("Summer_halfyear");

    JComboBox seasonBox = new JComboBox(new String[]{springString, summerString, autumnString, winterString,winterHalfYearString,summerHalfYearString });

    JTextField baseFlowRunoffQuantity = new JTextField(10);
    JRadioButton baseFlowFixedEstimation = new JRadioButton(JAMS.i18n("constant_threshold"));
    JRadioButton baseFlowLocalMiniumEstimation = new JRadioButton(JAMS.i18n("Local_minimum"));
    
    HydrographChart hydrographBaseFlow = null;    
    HydrographChart eventFilterHydrograph = null;    
    HydrographChart rangeFilterHydrograph = null;
    
    JButton updateBn = new JButton(JAMS.i18n("update"));
    
    final static String raisingEdgeString = JAMS.i18n("raising_limb"),
                        peakString = JAMS.i18n("peak"),
                        fallingEdgeString = JAMS.i18n("falling_limb");

    JComboBox hydroEventTypeBox = new JComboBox(new String[]{raisingEdgeString, peakString, fallingEdgeString});
    JTextField windowSizeField = new JTextField(10);
    JSlider qualitySlider = new JSlider();
    JLabel timeperiodLabel = new JLabel();
    JTextField qualitySliderValue = new JTextField(10);

    jams.gui.input.CalendarInput rangeFilterStartDateInput = new CalendarInput();
    jams.gui.input.CalendarInput rangeFilterEndDateInput = new CalendarInput();
    
    

    boolean isApproved = false;

    public TimeFilterDialog(TimeSerie timeserie){
        super(GUIState.getMainWindow());
        this.timeserie = timeserie;
        init(timeserie,null); //1,2,3
    }

    static public boolean isApplicable(DataCollection dc){
        return !dc.getDatasets(TimeSerie.class).isEmpty();
    }

    private JPanel constructYearTimeFilterPanel(YearlyTimeFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Select_years")));

        TimeInterval t = timeserie.getTimeDomain();

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(t.getStart().getTime());
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(t.getEnd().getTime());
        int endYear = calendar.get(Calendar.YEAR);

        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i <= endYear - startYear; i++) {
            model.add(i, new Integer(i + startYear));
        }

        yearList.setModel(model);

        yearList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane yearlyListScrollPane = new JScrollPane(yearList);
        
        filterPanel.add(yearlyListScrollPane, BorderLayout.CENTER);
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
        dataPanel.add(new JButton(JAMS.i18n("OK")) {
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        Object oYears[] = yearList.getSelectedValues();
                        int years[] = new int[oYears.length];
                        for (int i = 0; i < oYears.length; i++) {
                            years[i] = ((Integer) oYears[i]).intValue();
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
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Select months")));

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
                    monthList.setSelectedIndices(new int[]{9,10,11,0,1,2});
                }
            }
        });

        filterPanel.add(seasonBox, BorderLayout.NORTH);

        filterPanel.add(new JButton(JAMS.i18n("Ok")) {

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
        int windowSize;

        try{
            windowSize = Integer.parseInt(windowSizeField.getText());
        }catch(NumberFormatException pe){
            JOptionPane.showMessageDialog(rootPane, JAMS.i18n("Please_enter_a_valid_window_size"));
            windowSize = EventFilter.DEFAULT_WINDOWSIZE;
        }

        //this.hydrographHydroEvent.setGroundwaterWindowSize(windowSize);

        EventFilter filter = null;
        if (hydroEventTypeBox.getSelectedItem().equals(raisingEdgeString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.RaisingEdge, windowSize);
        }else if (hydroEventTypeBox.getSelectedItem().equals(fallingEdgeString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.Recession, windowSize);
        }else if (hydroEventTypeBox.getSelectedItem().equals(peakString)){
            filter = TimeFilterFactory.getEventFilter(timeserie, TimeFilterFactory.EventFilter.EventType.Peak, windowSize);
        }

        if (filter==null) {
            return null;
        }

        filter.setQualityThreshold(qualitySlider.getValue()/100.0);

        this.qualitySlider.setMinimum((int)(filter.getMinQuality()*100.0));
        this.qualitySlider.setMaximum((int)(filter.getMaxQuality()*100.0));

        return filter;
    }

    ActionListener updateHydroEventListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {                                    
            eventFilterHydrograph.clearTimeFilter();
            eventFilterHydrograph.addTimeFilter(constructHydroEventFilter());
        }
    };
    
    private TimeFilter constructRangeFilter(){
        jams.data.Attribute.Calendar cStart = rangeFilterStartDateInput.getCalendarValue();
        jams.data.Attribute.Calendar cEnd = rangeFilterEndDateInput.getCalendarValue();
        
        if (cStart == null || cEnd == null) {
            return null;
        }
        jams.data.Attribute.TimeInterval I = DefaultDataFactory.getDataFactory().createTimeInterval();
        I.setValue(timeserie.getTimeDomain().getValue());
        I.setStart(cStart);
        I.setEnd(cEnd);
        
        return TimeFilterFactory.getRangeFilter(I);
    }
    
    private JPanel constructRangeFilterPanel(TimeSerie m, RangeTimeFilter f){
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Define_range_filter")));
        
        rangeFilterHydrograph = new HydrographChart();        
        
        JPanel northPanel = new JPanel(new FlowLayout());
        JPanel confPanel = new JPanel(new FlowLayout());
        confPanel.add(new JLabel(JAMS.i18n("Select_start_date")));
        confPanel.add(rangeFilterStartDateInput);
        confPanel.add(new JLabel(JAMS.i18n("Select_end_date")));
        confPanel.add(rangeFilterEndDateInput);
        
        confPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Configuration")));
        northPanel.add(confPanel, BorderLayout.CENTER);
        
        filterPanel.add(northPanel, BorderLayout.NORTH);

        PatchedChartPanel chartPanel = rangeFilterHydrograph.getChartPanel();

        filterPanel.add(chartPanel, BorderLayout.CENTER);

        filterPanel.add(new JButton(JAMS.i18n("Ok")) {
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = constructRangeFilter();
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);
        
        rangeFilterStartDateInput.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged() {                
                filter = constructRangeFilter();
                if (rangeFilterEndDateInput.getCalendarValue()==null){
                    rangeFilterEndDateInput.setValue(rangeFilterStartDateInput.getValue());
                }
                if (filter == null)
                    return;
                rangeFilterHydrograph.clearTimeFilter();
                rangeFilterHydrograph.addTimeFilter(filter);                
            }
        });
        
        rangeFilterEndDateInput.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged() {                
                filter = constructRangeFilter();                
                if (filter == null)
                    return;
                rangeFilterHydrograph.clearTimeFilter();
                rangeFilterHydrograph.addTimeFilter(filter);                
            }
        });
        
        if (m != null && f == null){
            rangeFilterStartDateInput.setValue(m.getTimeDomain().getStart());
            rangeFilterEndDateInput.setValue(m.getTimeDomain().getEnd());
        }
        if (f != null){
            rangeFilterStartDateInput.setValue(f.getRange().getStart());
            rangeFilterEndDateInput.setValue(f.getRange().getEnd());
        }
        
        rangeFilterHydrograph.setHydrograph(m);//do at last to avoid unnecessary redraws
        
        return filterPanel;
    }

    private JPanel constructHydroEventFilterPanel(TimeSerie m, EventFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Select_hydrograph_event_filter")));

        eventFilterHydrograph = new HydrographChart();
        
        JPanel northPanel = new JPanel(new FlowLayout());

        JPanel confPanel = new JPanel(new FlowLayout());
        String windowSizeString = JAMS.i18n("Select_window_size") + " " + JAMS.i18n("in") + " ";
        switch (m.getTimeDomain().getTimeUnit()){
            case 1: windowSizeString += JAMS.i18n("years"); break;
            case 2: windowSizeString += JAMS.i18n("months"); break;
            case 6: windowSizeString += JAMS.i18n("days"); break;
            case 11: windowSizeString += JAMS.i18n("hours"); break;
        }
        confPanel.add(new JLabel(windowSizeString));
        windowSizeField.setText(Integer.toString(EventFilter.DEFAULT_WINDOWSIZE));
        confPanel.add(windowSizeField);
        confPanel.add(new JLabel(JAMS.i18n("Select_type_of_event")));
        confPanel.add(hydroEventTypeBox);

        confPanel.add(new JLabel(JAMS.i18n("Select_event_quality")));
        confPanel.add(qualitySlider);
        
        confPanel.add(updateBn);
        
        confPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Configuration")));

        northPanel.add(confPanel, BorderLayout.CENTER);

        filterPanel.add(northPanel, BorderLayout.NORTH);

        PatchedChartPanel chartPanel = eventFilterHydrograph.getChartPanel();
        filterPanel.add(chartPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        
        filterPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        buttonPanel.add(new JButton(JAMS.i18n("OK")) {
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = constructHydroEventFilter();
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        });
        
        buttonPanel.add(new JButton(JAMS.i18n("Cancel")) {
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = null;
                        TimeFilterDialog.this.isApproved = false;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        });

        updateBn.addActionListener(updateHydroEventListener);
        
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
       
        eventFilterHydrograph.setHydrograph(m);//2
        
        return filterPanel;
    }

    private TimeFilter constructBaseFlowFilter(){
        double threshold = 1.0;

        try{
            threshold = Double.parseDouble(baseFlowRunoffQuantity.getText());
        }catch(NumberFormatException pe){
            JOptionPane.showMessageDialog(rootPane, JAMS.i18n("Please_enter_a_valid_threshold"));
        }
        if (baseFlowFixedEstimation.isSelected()){
            return TimeFilterFactory.getBaseFlowTimeFilter(timeserie, BaseFlowTimeFilter.Method.Fixed, threshold);
        }else{
            return TimeFilterFactory.getBaseFlowTimeFilter(timeserie, BaseFlowTimeFilter.Method.HYSEPLocalMinimum, threshold);
        }
    }

    ActionListener updateBaseFlowListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) { 
            /*if (e.getSource() instanceof JTextField){
                
            }*/
            //((JRadioButton)e.getSource()).isSelected(); 
            hydrographBaseFlow.clearTimeFilter();
            hydrographBaseFlow.addTimeFilter(constructBaseFlowFilter());
        }
    };

    private JPanel constructBaseFlowTimeFilterPanel(TimeSerie m, BaseFlowTimeFilter filter) {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Select_baseflow_filter")));

        hydrographBaseFlow = new HydrographChart();
        
        ButtonGroup methodButtonGroup = new ButtonGroup();
        methodButtonGroup.add(baseFlowFixedEstimation);
        methodButtonGroup.add(baseFlowLocalMiniumEstimation);

        JPanel northPanel = new JPanel(new FlowLayout());

        JPanel methodPanel = new JPanel(new FlowLayout());
        methodPanel.add(baseFlowFixedEstimation);
        methodPanel.add(baseFlowLocalMiniumEstimation);
        methodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), JAMS.i18n("Method")));

        northPanel.add(methodPanel, BorderLayout.WEST);
        northPanel.add(new JLabel(JAMS.i18n("Threshold")), BorderLayout.CENTER);
        northPanel.add(this.baseFlowRunoffQuantity, BorderLayout.EAST);

        filterPanel.add(northPanel, BorderLayout.NORTH);

        PatchedChartPanel chartPanel = hydrographBaseFlow.getChartPanel();
        filterPanel.add(chartPanel, BorderLayout.CENTER);
        
        filterPanel.add(new JButton(JAMS.i18n("OK")) {

            {
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TimeFilterDialog.this.filter = constructBaseFlowFilter();
                        TimeFilterDialog.this.isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        }, BorderLayout.SOUTH);

        for (ActionListener l : baseFlowFixedEstimation.getActionListeners()){
            baseFlowFixedEstimation.removeActionListener(l);
        }
        for (ActionListener l : baseFlowLocalMiniumEstimation.getActionListeners()){
            baseFlowLocalMiniumEstimation.removeActionListener(l);
        }
        baseFlowFixedEstimation.addActionListener(updateBaseFlowListener);
        baseFlowLocalMiniumEstimation.addActionListener(updateBaseFlowListener);
        baseFlowRunoffQuantity.addActionListener(updateBaseFlowListener);
        if (filter != null){
            BaseFlowTimeFilter baseFlowFilter = (BaseFlowTimeFilter)filter;
            this.timeserie = baseFlowFilter.getTimeSerie();
            this.baseFlowRunoffQuantity.setText(Double.toString(baseFlowFilter.getThreshold()));
            if (baseFlowFilter.getMethod() == BaseFlowTimeFilter.Method.Fixed)
                this.baseFlowFixedEstimation.setSelected(true);
            else
                this.baseFlowLocalMiniumEstimation.setSelected(true);

        }else{
            double value = m.getMin() + 0.1*(m.getMax()-m.getMin());
            baseFlowRunoffQuantity.setText(Double.toString(value));
        }

        hydrographBaseFlow.setHydrograph(m); //1
        
        return filterPanel;
    }

    public void init(TimeSerie serie, TimeFilter filter){
        this.filter = filter;
        
        this.setMaximumSize(new Dimension(1024, 800));
        
        JTabbedPane pane = new JTabbedPane();
        
        //yearly filter
        if (filter instanceof YearlyTimeFilter || filter == null) {
            YearlyTimeFilter yearlyFilter = null;
            if (filter!=null){
                yearlyFilter = (YearlyTimeFilter)filter;
            }
            
            pane.addTab(JAMS.i18n("Yearly_Filter"), constructYearTimeFilterPanel(yearlyFilter));
        }

        if (filter instanceof MonthlyTimeFilter || filter == null) {
            MonthlyTimeFilter yearlyFilter = null;
            if (filter!=null){
                yearlyFilter = (MonthlyTimeFilter)filter;
            }

            pane.addTab(JAMS.i18n("Monthly_Filter"), constructMonthlyTimeFilterPanel(yearlyFilter));
        }

        if (filter instanceof BaseFlowTimeFilter || filter == null) {
            BaseFlowTimeFilter baseFlowFilter = null;
            if (filter!=null){
                baseFlowFilter = (BaseFlowTimeFilter)filter;
            }

            pane.addTab(JAMS.i18n("Baseflow_Filter"), constructBaseFlowTimeFilterPanel(serie, baseFlowFilter)); //1
        }

        if (filter instanceof EventFilter || filter == null) {
            EventFilter eventFilter = null;
            if (filter!=null){
                eventFilter = (EventFilter)filter;
            }

            pane.addTab(JAMS.i18n("Hydrograph_Event_Filter"), constructHydroEventFilterPanel(serie, eventFilter));//2
        }
        
        if (filter instanceof TimeFilterFactory.RangeTimeFilter || filter == null) {
            TimeFilterFactory.RangeTimeFilter rangeFilter = null;
            if (filter!=null){
                rangeFilter = (TimeFilterFactory.RangeTimeFilter)filter;
            }

            pane.addTab(JAMS.i18n("Interval_Filter"), constructRangeFilterPanel(serie, rangeFilter));//3
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
