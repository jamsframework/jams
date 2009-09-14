/*
 * TimeintervalInput.java
 * Created on 5. September 2006, 23:43
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.gui.input;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jams.data.JAMSCalendar;
import jams.data.JAMSTimeInterval;
import jams.gui.*;
import java.awt.Color;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;

/**
 *
 * @author S. Kralisch
 */
public class TimeintervalInput extends JPanel implements InputComponent {

    private JTextField tuCount,  syear,  smonth,  sday,  shour,  sminute,  eyear,  emonth,  eday,  ehour,  eminute;

    private JComboBox timeUnit;

    private Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

    private Map<Integer, Integer> fieldMap = new HashMap<Integer, Integer>();

    private JPanel panel;

    private ValueChangeListener l;

    protected boolean intervalEdit = false;

    public TimeintervalInput() {
        this(false);
    }

    public TimeintervalInput(boolean intervalEdit) {

        this.intervalEdit = intervalEdit;

        GridBagLayout gbl = new GridBagLayout();
        this.setBorder(BorderFactory.createEtchedBorder());

        this.setLayout(gbl);

        GUIHelper.addGBComponent(this, gbl, new JLabel(JAMS.resources.getString("Date_(YYYY/MM/DD)")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, gbl, new JLabel(JAMS.resources.getString("Time_(HH:MM)")), 11, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, gbl, new JLabel(JAMS.resources.getString("Start:_")), 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, gbl, new JLabel(JAMS.resources.getString("End:_")), 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, gbl, new JLabel(JAMS.resources.getString("Unit_Count:_")), 0, 3, 1, 1, 0, 0);

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
        GUIHelper.addGBComponent(this, gbl, panel, 1, 1, 10, 1, 0, 0);

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
        GUIHelper.addGBComponent(this, gbl, panel, 11, 1, 1, 1, 0, 0);


        eyear = new JTextField();
        eyear.setInputVerifier(new NumericIntervalVerifier(1900, 2100));
        eyear.setPreferredSize(new Dimension(40, 20));

        emonth = new JTextField();
        emonth.setInputVerifier(new NumericIntervalVerifier(1, 12));
        emonth.setPreferredSize(new Dimension(25, 20));

        eday = new JTextField();
        eday.setInputVerifier(new NumericIntervalVerifier(1, 31));
        eday.setPreferredSize(new Dimension(25, 20));

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(eyear);
        panel.add(new JLabel("/"));
        panel.add(emonth);
        panel.add(new JLabel("/"));
        panel.add(eday);
        GUIHelper.addGBComponent(this, gbl, panel, 1, 2, 10, 1, 0, 0);

        ehour = new JTextField();
        ehour.setInputVerifier(new NumericIntervalVerifier(0, 23));
        ehour.setPreferredSize(new Dimension(25, 20));

        eminute = new JTextField();
        eminute.setInputVerifier(new NumericIntervalVerifier(0, 59));
        eminute.setPreferredSize(new Dimension(25, 20));

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(ehour);
        panel.add(new JLabel(":"));
        panel.add(eminute);
        GUIHelper.addGBComponent(this, gbl, panel, 11, 2, 1, 1, 0, 0);

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

        tuCount = new JTextField();
        tuCount.setInputVerifier(new NumericIntervalVerifier(0, 1000));
        tuCount.setPreferredSize(new Dimension(10, 10));
        GUIHelper.addGBComponent(this, gbl, tuCount, 1, 3, 1, 1, 0, 0);


        timeUnit = new JComboBox();
        timeUnit.addItem(JAMS.resources.getString("YEAR"));
        timeUnit.addItem(JAMS.resources.getString("MONTH"));
        timeUnit.addItem(JAMS.resources.getString("DAY"));
        timeUnit.addItem(JAMS.resources.getString("HOUR"));
        timeUnit.addItem(JAMS.resources.getString("MINUTE"));
        timeUnit.addItem(JAMS.resources.getString("SECOND"));
        timeUnit.setPreferredSize(new Dimension(40, 20));
        GUIHelper.addGBComponent(this, gbl, timeUnit, 2, 3, 1, 1, 1, 0);

        sday.setBorder(BorderFactory.createEtchedBorder());
        smonth.setBorder(BorderFactory.createEtchedBorder());
        syear.setBorder(BorderFactory.createEtchedBorder());
        shour.setBorder(BorderFactory.createEtchedBorder());
        sminute.setBorder(BorderFactory.createEtchedBorder());
        eday.setBorder(BorderFactory.createEtchedBorder());
        emonth.setBorder(BorderFactory.createEtchedBorder());
        eyear.setBorder(BorderFactory.createEtchedBorder());
        ehour.setBorder(BorderFactory.createEtchedBorder());
        eminute.setBorder(BorderFactory.createEtchedBorder());
        tuCount.setBorder(BorderFactory.createEtchedBorder());
        timeUnit.setBorder(BorderFactory.createEtchedBorder());

        tuCount.setEnabled(intervalEdit);
        timeUnit.setEnabled(intervalEdit);
    }

    @Override
    public String getValue() {
        try {
            JAMSTimeInterval ti = new JAMSTimeInterval();
            JAMSCalendar start = JAMSDataFactory.createCalendar();
            start.set(
                    Integer.parseInt(syear.getText()),
                    Integer.parseInt(smonth.getText()) - 1,
                    Integer.parseInt(sday.getText()),
                    Integer.parseInt(shour.getText()),
                    Integer.parseInt(sminute.getText()),
                    0);
            ti.setStart(start);
            JAMSCalendar end = JAMSDataFactory.createCalendar();
            end.set(
                    Integer.parseInt(eyear.getText()),
                    Integer.parseInt(emonth.getText()) - 1,
                    Integer.parseInt(eday.getText()),
                    Integer.parseInt(ehour.getText()),
                    Integer.parseInt(eminute.getText()),
                    0);
            ti.setEnd(end);
            ti.setTimeUnit(fieldMap.get(timeUnit.getSelectedIndex()));
            ti.setTimeUnitCount(Integer.parseInt(tuCount.getText()));
            if (!ti.getStart().before(ti.getEnd())) {
                return null;
            } else {
                return ti.toString();
            }
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Override
    public void setValue(String value) {
        //1996-11-01 7:30 2000-10-31 7:30 6 1

        JAMSTimeInterval ti = new JAMSTimeInterval();
        if ((value != null) && !value.equals("")) {
            ti.setValue(value);
        }

        Attribute.Calendar start = ti.getStart();
        syear.setText(Integer.toString(start.get(JAMSCalendar.YEAR)));
        smonth.setText(Integer.toString(start.get(JAMSCalendar.MONTH) + 1));
        sday.setText(Integer.toString(start.get(JAMSCalendar.DAY_OF_MONTH)));
        shour.setText(Integer.toString(start.get(JAMSCalendar.HOUR_OF_DAY)));
        sminute.setText(Integer.toString(start.get(JAMSCalendar.MINUTE)));

        Attribute.Calendar end = ti.getEnd();
        eyear.setText(Integer.toString(end.get(JAMSCalendar.YEAR)));
        emonth.setText(Integer.toString(end.get(JAMSCalendar.MONTH) + 1));
        eday.setText(Integer.toString(end.get(JAMSCalendar.DAY_OF_MONTH)));
        ehour.setText(Integer.toString(end.get(JAMSCalendar.HOUR_OF_DAY)));
        eminute.setText(Integer.toString(end.get(JAMSCalendar.MINUTE)));

        timeUnit.setSelectedIndex(indexMap.get(ti.getTimeUnit()));
        tuCount.setText(Integer.toString(ti.getTimeUnitCount()));
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setRange(double lower, double upper) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        syear.setEnabled(enabled);
        smonth.setEnabled(enabled);
        sday.setEnabled(enabled);
        shour.setEnabled(enabled);
        sminute.setEnabled(enabled);
        eyear.setEnabled(enabled);
        emonth.setEnabled(enabled);
        eday.setEnabled(enabled);
        ehour.setEnabled(enabled);
        eminute.setEnabled(enabled);
        timeUnit.setEnabled(enabled && intervalEdit);
        tuCount.setEnabled(enabled && intervalEdit);
    }

    @Override
    public boolean verify() {

        try {
            if (this.getValue() != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getErrorCode() {
        if (verify()) {
            return INPUT_OK;
        } else {
            return INPUT_WRONG_FORMAT;
        }
    }

    @Override
    public void setLength(int length) {
    }

    @Override
    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.syear.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.smonth.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.sday.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.shour.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.sminute.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.eyear.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.emonth.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.eday.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.ehour.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.eminute.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.timeUnit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
        this.tuCount.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TimeintervalInput.this.l.valueChanged();
            }
        });
    }
    private Color oldColor;

    @Override
    public void setMarked(boolean marked) {
        if (marked == true) {
            oldColor = getBackground();
            setBackground(new Color(255, 0, 0));
        } else {
            setBackground(oldColor);
        }
    }

}
