/*
 * CalendarInput.java
 * Created on 2. October 2007, 15:10
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.jams.gui.input;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.data.JAMSTimeInterval;
import org.unijena.jams.gui.*;

/**
 *
 * @author S. Kralisch
 */
public class CalendarInput extends JPanel implements InputComponent {
    
    JTextField syear, smonth, sday, shour, sminute;
    Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
    Map<Integer, Integer> fieldMap = new HashMap<Integer, Integer>();
    JPanel panel;
    
    /** Creates a new instance of TimeintervalInput */
    public CalendarInput() {
        
        GridBagLayout gbl = new GridBagLayout();
        this.setBorder(BorderFactory.createEtchedBorder());
        
        this.setLayout(gbl);
        
        LHelper.addGBComponent(this, gbl, new JLabel("Date (YYYY/MM/DD)"), 1, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(this, gbl, new JLabel("Time (HH:MM)"), 11, 0, 1, 1, 0, 0);
        
        syear = new JTextField();
        syear.setInputVerifier(new NumericIntervalVerifier(1900, 2100));
        syear.setPreferredSize(new Dimension(40, 20));
        
        smonth = new JTextField();
        smonth.setInputVerifier(new NumericIntervalVerifier(1, 12));
        smonth.setPreferredSize(new Dimension(25, 20));
        
        sday = new JTextField();
        sday.setInputVerifier(new NumericIntervalVerifier(1, 31));
        sday.setPreferredSize(new Dimension(25, 20));
        
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(syear);
        panel.add(new JLabel("/"));
        panel.add(smonth);
        panel.add(new JLabel("/"));
        panel.add(sday);
        LHelper.addGBComponent(this, gbl, panel, 1, 1, 10, 1, 0, 0);
        
        shour = new JTextField();
        shour.setInputVerifier(new NumericIntervalVerifier(0, 23));
        shour.setPreferredSize(new Dimension(25, 20));
        
        sminute = new JTextField();
        sminute.setInputVerifier(new NumericIntervalVerifier(0, 59));
        sminute.setPreferredSize(new Dimension(25, 20));
        
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(shour);
        panel.add(new JLabel(":"));
        panel.add(sminute);
        LHelper.addGBComponent(this, gbl, panel, 11, 1, 1, 1, 0, 0);
        
        indexMap.put(JAMSCalendar.YEAR, 0);
        indexMap.put(JAMSCalendar.MONTH, 1);
        indexMap.put(JAMSCalendar.DAY_OF_YEAR, 2);
        indexMap.put(JAMSCalendar.HOUR_OF_DAY, 3);
        indexMap.put(JAMSCalendar.MINUTE, 4);
        indexMap.put(JAMSCalendar.SECOND, 5);
        
        fieldMap.put(0, JAMSCalendar.YEAR);
        fieldMap.put(1, JAMSCalendar.MONTH);
        fieldMap.put(2, JAMSCalendar.DAY_OF_YEAR);
        fieldMap.put(3, JAMSCalendar.HOUR_OF_DAY);
        fieldMap.put(4, JAMSCalendar.MINUTE);
        fieldMap.put(5, JAMSCalendar.SECOND);
        
        sday.setBorder(BorderFactory.createEtchedBorder());
        smonth.setBorder(BorderFactory.createEtchedBorder());
        syear.setBorder(BorderFactory.createEtchedBorder());
        shour.setBorder(BorderFactory.createEtchedBorder());
        sminute.setBorder(BorderFactory.createEtchedBorder());        
        
    }
    
    public String getValue() {
        JAMSCalendar cal = new JAMSCalendar(
                Integer.parseInt(syear.getText()),
                Integer.parseInt(smonth.getText())-1,
                Integer.parseInt(sday.getText()),
                Integer.parseInt(shour.getText()),
                Integer.parseInt(sminute.getText()),
                0
                );
        return cal.toString();
    }
    
    public void setValue(String value) {
        
        JAMSCalendar cal = new JAMSCalendar();
        
        if (value != "") {
            cal.setValue(value);
        }
        
        syear.setText(Integer.toString(cal.get(JAMSCalendar.YEAR)));
        smonth.setText(Integer.toString(cal.get(JAMSCalendar.MONTH)+1));
        sday.setText(Integer.toString(cal.get(JAMSCalendar.DAY_OF_MONTH)));
        shour.setText(Integer.toString(cal.get(JAMSCalendar.HOUR_OF_DAY)));
        sminute.setText(Integer.toString(cal.get(JAMSCalendar.MINUTE)));
        
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    class NumericIntervalVerifier extends InputVerifier {
        
        double lower, upper;
        
        public NumericIntervalVerifier(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }
        
        public boolean verify(JComponent input) {
            
            boolean result = false;
            double value;
            
            try {
                value = Double.parseDouble(((JTextField) input).getText());
                if ((value >= lower) && (value <= upper))
                    result = true;
            } catch (NumberFormatException nfe) {}
            
            return (result);
        }
    }
    
    public void setRange(double lower, double upper){};
    
    public boolean verify() {
        
//        JAMSTimeInterval ti = new JAMSTimeInterval();
//        ti.setValue(getValue());
        
        return true;
    }
    
    public int getErrorCode() {
        return INPUT_OK;
    }
}
