/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import jams.data.Attribute.TimeInterval;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import optas.hydro.data.DataCollection;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeFilterFactory;
import optas.hydro.data.TimeFilterFactory.YearlyTimeFilter;

/**
 *
 * @author chris
 */
public class TimeFilterDialog extends JDialog{
    DataCollection dc;

    TimeFilter filter;

    JList yearList = new JList();
    boolean isApproved = false;

    public TimeFilterDialog(DataCollection dc){
        this.dc = dc;
        init();
    }

    private void init(){
        filter = null;

        JTabbedPane pane = new JTabbedPane();

        //yearly filter
        JPanel yearlyFilterPanel = new JPanel(new BorderLayout());
        yearlyFilterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select years"));

        TimeInterval t = dc.getTimeDomain();
        int startYear = t.getStart().getTime().getYear();
        int endYear   = t.getEnd().getTime().getYear();

        DefaultListModel model = new DefaultListModel();
        for (int i=0;i<startYear-endYear;i++)
            model.add(i, new Integer(i+startYear));

        yearList.setModel(model);
        yearList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        yearlyFilterPanel.add(yearList);


        yearlyFilterPanel.add(new JButton("Ok"){
            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        Object oYears[] = yearList.getSelectedValues();
                        int years[] = new int[oYears.length];
                        for (int i=0;i<oYears.length;i++)
                            years[i] = ((Integer)oYears[i]).intValue();

                        filter = TimeFilterFactory.getYearlyFilter(years);
                        isApproved = true;
                        TimeFilterDialog.this.setVisible(false);
                    }
                });
            }
        });

        pane.addTab("Yearly Filter", yearlyFilterPanel);


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(pane);

        this.getContentPane().add(mainPanel);

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
        this.filter = null;
        this.isApproved = false;
        super.setVisible(b);
    }
}
