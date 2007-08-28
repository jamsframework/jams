/*
 * JFCChartPanel.java
 *
 * Created on 27. April 2006, 11:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory.plot;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

/**
 *
 * @author c0krpe
 */
public class JFCChartPanel extends org.jfree.chart.ChartPanel {
    public JFreeChart theChart;
    public JPopupMenu jfcPM = null;
    private XYPlot currentPlot = null;
    
    /** Creates a new instance of JFCChartPanel */
    public JFCChartPanel(JFreeChart chart) {
        super(chart, 640, 480, 640, 480, 640, 480, true, true, true, true, true, true);
        this.theChart = chart;
        
        //create the popup Menu
        jfcPM = new javax.swing.JPopupMenu();
        jfcPM.setFont(new java.awt.Font("Arial", 0, 11));
        this.createPopupMenu(jfcPM);
        
    }
    
    public void createPopupMenu(JPopupMenu pm){
        javax.swing.JMenu csMenu = new javax.swing.JMenu("Change series ...");
        csMenu.setFont(new java.awt.Font("Arial", 0, 11));
        pm.add(csMenu);
        
        JMenuItem menuItem = new JMenuItem("Change diagram");
        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
        menuItem.addActionListener(this);
        pm.add(menuItem);
        
        menuItem = new JMenuItem("Export diagram");
        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
        menuItem.addActionListener(this);
        pm.add(menuItem);
        
        menuItem = new JMenuItem("Print diagram");
        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
        menuItem.addActionListener(this);
        pm.add(menuItem);
    }
    
    
    
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == e.BUTTON3){
            this.jfcPM.show(this, e.getX(), e.getY());
            
            Plot plot = this.getChart().getPlot();
            
            if(plot.getClass() == XYPlot.class){
                System.out.println("I am a XYPlot!");
                
            } else if(plot.getClass() == CombinedDomainXYPlot.class){
                System.out.println("I am a CombinedDomainXYPlot!");
                CombinedDomainXYPlot multiPlot = (CombinedDomainXYPlot)this.getChart().getPlot();
                
                //find out which subplot the mouse was clicked on
                ChartRenderingInfo cri = this.getChartRenderingInfo();
                java.awt.geom.Point2D p = this.translateScreenToJava2D(new Point(e.getX(), e.getY())); 
                this.currentPlot = multiPlot.findSubplot(cri.getPlotInfo(),p);
                
                if(currentPlot != null){
                    ((JMenu)this.jfcPM.getComponent(0)).setEnabled(true);
                    //get the series and add them to the popupMenu
                    int nSeries = currentPlot.getDatasetCount();
                    System.out.println("Plot: " + currentPlot.toString() + " series: " + nSeries);
                    
                    JMenu csMenu = (JMenu)this.jfcPM.getComponent(0);
                    
                    //discard all previous entries
                    csMenu.removeAll();
                    for(int i = 0; i < nSeries; i++){
                        JMenuItem menuItem = new JMenuItem();
                        menuItem = new JMenuItem(currentPlot.getDataset(i).getSeriesKey(0).toString());
                        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
                        menuItem.addActionListener(this);
                        csMenu.add(menuItem);
                    }
                }
                else{
                    ((JMenu)this.jfcPM.getComponent(0)).setEnabled(false);
                }
            }
        }
        else{
            //super.mousePressed(e);
        }

        
    }
    
    //the action from the popupMenu
    public void actionPerformed(java.awt.event.ActionEvent e){
        String command = e.getActionCommand();
        
        if(command.equals("Change diagram")){
            jamschartfactory.gui.DiagramPropertiesDlg dlg = new jamschartfactory.gui.DiagramPropertiesDlg(this);
            dlg.setVisible(true);
        }
        else if(command.equals("Export diagram")){
            try{
                this.doSaveAs();
            }catch(java.io.IOException err){
                System.out.println("Problems occured during export: " + err.getMessage());
            }
        }
        else if(command.equals("Print diagram")){
            this.createChartPrintJob();
        }
        //a serie was selected, because nothing else is left
        else{
            String seriesName = command;
            //retrieve the selected series ID
            int idx = -1;
            for(int i = 0; i < currentPlot.getDatasetCount(); i++){
                if(currentPlot.getDataset(i).getSeriesKey(0).toString().equals(seriesName)){
                    idx = i;
                }
            }
            //retrieve the correct renderer for the selected serie
            if(currentPlot.getRenderer(idx).getClass() == XYLineAndShapeRenderer.class){
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)currentPlot.getRenderer(idx);
                jamschartfactory.gui.SeriesPropertiesDlg spd = new jamschartfactory.gui.SeriesPropertiesDlg(currentPlot, renderer, idx);
                spd.setVisible(true);
            }
            else if (currentPlot.getRenderer(idx).getClass() == XYBarRenderer.class){
                XYBarRenderer renderer = (XYBarRenderer)currentPlot.getRenderer(idx);
                jamschartfactory.gui.SeriesPropertiesDlg spd = new jamschartfactory.gui.SeriesPropertiesDlg(currentPlot, renderer, idx);
                spd.setVisible(true);
            }
        }
        this.validate();
    }
    
}
