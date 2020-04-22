/*
 * CalendarInput.java
 * Created on 2. October 2007, 15:10
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.gui.input;

import jams.gui.tools.GUIHelper;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.tools.StringTools;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

/**
 *
 * @author S. Kralisch
 */
public class CalendarInput extends JPanel implements InputComponent {

    private JTextField dateText, timeText;
    private JPanel datePanel, timePanel;
    private ValueChangeListener l;
    private JCalendarButton dateButton;
    private JTimeButton timeButton;
    private String oldDateString;
//    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static DateFormat dateFormat = new SimpleDateFormat(Attribute.Calendar.DEFAULT_FORMAT_PATTERN.split(" ")[0]);
//    private static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static DateFormat timeFormat = new SimpleDateFormat(Attribute.Calendar.DEFAULT_FORMAT_PATTERN.split(" ")[1]);
//    private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    private static DateFormat dateTimeFormat = new SimpleDateFormat(Attribute.Calendar.DEFAULT_FORMAT_PATTERN);
    private Color oldColor;

    public CalendarInput() {
        this(true);
        timeFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        dateFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        dateTimeFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
    }

    public CalendarInput(boolean doLayout) {

        createPanels();

        if (doLayout) {
            GridBagLayout gbl = new GridBagLayout();
            this.setBorder(BorderFactory.createEtchedBorder());

            this.setLayout(gbl);
            GUIHelper.addGBComponent(this, gbl, datePanel, 1, 1, 1, 1, 0, 0);
            GUIHelper.addGBComponent(this, gbl, timePanel, 11, 1, 1, 1, 0, 0);
            dateText.setToolTipText(jams.JAMS.i18n("Date"));
            timeText.setToolTipText(jams.JAMS.i18n("Time"));
        }
    }

    public void createPanels() {
        dateFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        dateTimeFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);

        // create the time panel
        datePanel = new JPanel();
        //datePanel.setBorder(BorderFactory.c reateEtchedBorder());

        dateText = new JTextField();
        dateText.setPreferredSize(new Dimension(100, 20));
        dateText.setBorder(BorderFactory.createEtchedBorder());

        datePanel.add(dateText);

        dateButton = new JCalendarButton();
        dateButton.setText("");
        dateButton.setPreferredSize(new Dimension(20, 20));
        dateButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Calendar.gif")));
        dateButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (evt.getNewValue() instanceof Date) {
                    setDate((Date) evt.getNewValue());
                }
            }
        });
        dateText.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                String dateString = getDateText().getText();
                Date date = null;
                try {
                    if (!StringTools.isEmptyString(dateString)) {
                        date = dateFormat.parse(dateString);
                    }
                } catch (Exception e) {
                    date = null;
                }
                setDate(date);
            }
        });

        datePanel.add(dateButton);

        // create the time panel
        timePanel = new JPanel();
        //timePanel.setBorder(BorderFactory.createEtchedBorder());

        timeText = new JTextField();
        timeText.setPreferredSize(new Dimension(80, 20));
        timeText.setBorder(BorderFactory.createEtchedBorder());

        timePanel.add(timeText);

        timeButton = new JTimeButton();
        timeButton.setText("");
        timeButton.setPreferredSize(new Dimension(20, 20));
        timeButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Time.gif")));
        timeButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (evt.getNewValue() instanceof Date) {
                    setTime((Date) evt.getNewValue());
                }
            }
        });
        timeText.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                String timeString = getTimeText().getText();
                Date time = null;
                try {
                    if (!StringTools.isEmptyString(timeString)) {
                        String dateString = dateText.getText();
                        if (StringTools.isEmptyString(dateString)) {
                            dateString = "1970-01-01";
                        }
                        time = timeFormat.parse(timeString);
                        DateTimeFormatter format = DateTimeFormatter.ofPattern(Attribute.Calendar.DEFAULT_FORMAT_PATTERN);
                        LocalDateTime dt = LocalDateTime.parse(dateString + " " + timeString, format);
                        time = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
                    }
                } catch (Exception e) {
                    time = null;
                }
                setTime(time);
            }
        });

        timePanel.add(timeButton);

    }

    public void setDate(Date date) {
        String dateString = oldDateString;
        if (date != null) {
            dateString = dateFormat.format(date);
            oldDateString = dateString;
        }
        dateText.setText(dateString);
        dateButton.setTargetDate(date);
    }

    public void setTime(Date time) {
        if (time != null) {
            Instant instant = Instant.ofEpochMilli(time.getTime());
//            LocalTime localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault());
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            Date dateTime = Date.from(instant);
//            timeText.setText(localTime.toString());
            timeText.setText(ldt.toLocalTime().toString());
            timeButton.setTargetDate(dateTime);
        }
    }

    public String getValue() {
        return getCalendarValue().toString();
    }

    public Attribute.Calendar getCalendarValue() {
        String dateString = dateText.getText();
        String timeString = timeText.getText();
        if (StringTools.isEmptyString(timeString)) {
            timeString = "00:00";
        }
        Date dateTime = new Date(0);

        DateTimeFormatter format = DateTimeFormatter.ofPattern(Attribute.Calendar.DEFAULT_FORMAT_PATTERN);

        try {
            LocalDateTime dt = LocalDateTime.parse(dateString + " " + timeString, format);
            dateTime = Date.from(dt.atZone(ZoneId.of(Attribute.Calendar.DEFAULT_TIME_ZONE.getID())).toInstant());
        } catch (DateTimeParseException ex) {
        }
        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        cal.setTime(dateTime);
        return cal;
    }

    public void setValue(String value) {

        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        if (!StringTools.isEmptyString(value)) {
            cal.setValue(value);
        }

        setValue(cal);
    }

    public void setValue(Attribute.Calendar calendar) {

        Date d = calendar.getTime();

        String dateString = dateFormat.format(d);
        String timeString = timeFormat.format(d);

        oldDateString = dateString;

        dateText.setText(dateString);
        timeText.setText(timeString);

        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(Attribute.Calendar.DEFAULT_FORMAT_PATTERN);
            LocalDateTime dt = LocalDateTime.parse(dateText.getText() + " " + timeText.getText(), format);
            Date newDate = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
            timeButton.setTargetDate(newDate);
            dateButton.setTargetDate(newDate);
        } catch (Exception ex) {
        }
    }

    public JComponent getComponent() {
        return this;
    }

    public void setEnabled(boolean enabled) {
        dateText.setEnabled(enabled);
        timeText.setEnabled(enabled);
        dateButton.setEnabled(enabled);
        timeButton.setEnabled(enabled);
    }

    public void setRange(double lower, double upper) {
    }

    public boolean verify() {

        try {
            if (StringTools.isEmptyString(this.getValue())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public int getErrorCode() {
        return INPUT_OK;
    }

    public void setLength(int length) {
    }

    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.dateText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }
        });
        this.timeText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                CalendarInput.this.l.valueChanged();
            }
        });
    }

    public void setMarked(boolean marked) {
        if (marked == true) {
            oldColor = getBackground();
            setBackground(new Color(255, 0, 0));
        } else {
            setBackground(oldColor);
        }
    }

    public static void main(String[] args) {

        LocalDateTime dt = LocalDateTime.parse("1996-11-01T23:30");
//        System.out.println(dt);

        InputComponent tii = new CalendarInput();

        Attribute.Calendar c = DefaultDataFactory.getDataFactory().createCalendar();
        c.setValue("1996-11-01 00:30");
        System.out.println(c);
        Date d = c.getTime();

        //tii.setMarked(true);
        tii.setValue(c.toString());
        System.out.println("in : " + tii.getValue());

        JDialog frame = new JDialog();
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(tii.getComponent());
        frame.pack();
        frame.setModal(true);
        frame.setVisible(true);
        System.out.println("out: " + tii.getValue());
        frame.dispose();
    }

    /**
     * @return the datePanel
     */
    public JPanel getDatePanel() {
        return datePanel;
    }

    /**
     * @return the timePanel
     */
    public JPanel getTimePanel() {
        return timePanel;
    }

    /**
     * @return the dateText
     */
    public JTextField getDateText() {
        return dateText;
    }

    /**
     * @return the timeText
     */
    public JTextField getTimeText() {
        return timeText;
    }

    public void setHelpText(String text) {
        text = "<html>" + text + "</html>";
        getComponent().setToolTipText(text);
    }
}
