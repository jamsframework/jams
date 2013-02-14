/**
 * CalendarPopup.java
 *
 * Created on March 5, 2000, 5:07 AM
 * @author Don Corley <don@donandann.com>
 * @version 1.0.0
 */
 
package jams.gui.input;

import jams.data.Attribute;
import jams.data.JAMSCalendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** 
 * A JCalendarPopup is a popup calendar the user can click on to change a date.
 * @author  Administrator
 * @version 1.0.0
 */
public class JCalendarPopup extends javax.swing.JPanel
    implements MouseListener
{
	private static final long serialVersionUID = 1L;

	/**
     * The default param for the date property.
     */
    public static final String DATE_PARAM = "date";
    /**
     * The date param for this popup.
     */
    protected String m_strDateParam = DATE_PARAM;

    public static Border ROLLOVER_BORDER = new LineBorder(Color.GRAY, 1);
    public static Border SELECTED_BORDER = new LineBorder(Color.BLUE, 1);
    public static Border EMPTY_BORDER = new EmptyBorder(1, 1, 1, 1);
    /**
     * Constant - Milliseconds in a day.
     */
    public static final long KMS_IN_A_DAY = 24 * 60 * 60 * 1000;    // Milliseconds in a day
    protected Date m_dateTarget = null;
    protected Date m_datePanelTarget = null;
    protected Date m_dateSelected = null;
    protected Date m_dateNow = null;
    protected boolean m_bFirstTime = true;
    protected int m_iTargetComponent = 0;

    protected Calendar m_calendar = Calendar.getInstance();
    protected StringBuffer m_sb = new StringBuffer();
    protected DateFormat m_df = DateFormat.getDateInstance(DateFormat.FULL);

    // Variables declaration - do not modify
    private javax.swing.JPanel m_panelMonth;
    private javax.swing.JButton m_buttonPrevMonth;
    private javax.swing.JLabel m_labelMonth;
    private javax.swing.JButton m_buttonNextMonth;

    private javax.swing.JPanel m_panelYear;
    private javax.swing.JButton m_buttonPrevYear;
    private javax.swing.JLabel m_labelYear;
    private javax.swing.JButton m_buttonNextYear;

    private javax.swing.JPanel m_panelDays;
    // End of variables declaration
    /**
     * Transfer the focus after selecting the date (default = true).
     */
    protected boolean m_bTransferFocus = true;
    /**
     * The name of the calendar popup icon.
     */
    public static final String CALENDAR_ICON = "Calendar";
    /**
     * The Insets around this button.
     */
    public static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

    /**
     * Creates new form CalendarPopup.
     */
    public JCalendarPopup()
    {
        super();
    }
    /**
     * Creates new form CalendarPopup.
     * @param date The initial date for this button.
     */
    public JCalendarPopup(Date date)
    {
        this();
        this.init(null, date, null);
    }
    /**
     * Creates new form CalendarPopup.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param date The initial date for this button.
     */
    public JCalendarPopup(String strDateParam, Date date)
    {
        this();
        this.init(strDateParam, date, null);
    }
    /**
     * Creates new form CalendarPopup.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param date The initial date for this button.
     * @param strLanguage The language to use.
     */
    public JCalendarPopup(String strDateParam, Date date, String strLanguage)
    {
        this();
        this.init(strDateParam, date, strLanguage);
    }
    /**
     * Creates new form CalendarPopup.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param date The initial date for this button.
     * @param strLanguage The language to use.
     */
    public void init(String strDateParam, Date dateTarget, String strLanguage)
    {
        if (strDateParam != null)
            m_strDateParam = strDateParam;      // Property name
        initComponents();

        m_dateNow = new Date();
        if (dateTarget == null)
            dateTarget = m_dateNow;
        m_dateSelected = dateTarget;

        if (strLanguage != null)
        {
            Locale locale = new Locale(strLanguage, "");
            if (locale != null)
            {
                m_calendar = Calendar.getInstance(locale);
                m_df = DateFormat.getDateInstance(DateFormat.FULL, locale);
            }
        }
        m_calendar.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        m_calendar.setTime(dateTarget);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_dateTarget = m_calendar.getTime();
        this.layoutCalendar(m_dateTarget);
        if (dateTarget == m_dateNow)
            m_dateSelected = m_dateTarget;  // If they want a default date, make the time 12:00
    }
    /**
     * Add all the components to this calendar panel.
     * @param dateTarget This date needs to be in the calendar.
     */
    public void layoutCalendar(Date dateTarget)
    {
        m_calendar.setTime(dateTarget);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_datePanelTarget = m_calendar.getTime();
        m_calendar.set(Calendar.DATE, 1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 0);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        Date dateFirstOfMonth = m_calendar.getTime();
        m_calendar.setTime(dateFirstOfMonth);
        m_calendar.add(Calendar.MONTH, 1);
        m_calendar.add(Calendar.DATE, -1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 23);
        m_calendar.set(Calendar.MINUTE, 59);
        m_calendar.set(Calendar.SECOND, 59);
        m_calendar.set(Calendar.MILLISECOND, 999);
        Date dateLastOfMonth = m_calendar.getTime();
        Date dateCalendarFirstDate = this.getFirstDateInCalendar(dateFirstOfMonth);
        Date dateCalendar = new Date(dateCalendarFirstDate.getTime());

        m_iTargetComponent = (int)((m_dateTarget.getTime() - dateCalendarFirstDate.getTime()) / KMS_IN_A_DAY);
        if (m_iTargetComponent < 0)
            m_iTargetComponent--;

        String strYear = this.getDateString(dateTarget, DateFormat.YEAR_FIELD);
        String strMonth = this.getDateString(dateTarget, DateFormat.MONTH_FIELD);
        m_labelMonth.setText(strMonth);
        m_labelYear.setText(strYear);
        int iDayOfWeekComponent = 0;
        int iDayComponent = 7;
        for (; iDayComponent < m_panelDays.getComponentCount(); iDayOfWeekComponent++, iDayComponent++)
        {
            if (iDayOfWeekComponent < 7)
            {
                JLabel labelDayOfWeek = (JLabel)m_panelDays.getComponent(iDayOfWeekComponent);
                String strWeek = this.getDateString(dateCalendar, DateFormat.DAY_OF_WEEK_FIELD);
                if ((strWeek != null) && (strWeek.length() > 0))
                    labelDayOfWeek.setText(strWeek.substring(0, 1));
                else
                    labelDayOfWeek.setText(Integer.toString(iDayOfWeekComponent));
            }
            JLabel labelDay = (JLabel)m_panelDays.getComponent(iDayComponent);
            String strDay = this.getDateString(dateCalendar, DateFormat.DATE_FIELD);
            labelDay.setText(strDay);
            if ((dateCalendar.before(dateFirstOfMonth))
                || (dateCalendar.after(dateLastOfMonth)))
                    labelDay.setForeground(Color.GRAY);
            else
                    labelDay.setForeground(Color.BLACK);
            labelDay.setBackground(m_panelDays.getBackground());
            if (m_iTargetComponent == iDayComponent - 7)
                labelDay.setBorder(SELECTED_BORDER);
            else
                labelDay.setBorder(EMPTY_BORDER);

            m_calendar.setTime(dateCalendar);
            m_calendar.add(Calendar.DATE, 1);
            dateCalendar = m_calendar.getTime();

            if (m_bFirstTime)
            {
                labelDay.addMouseListener(this);
                labelDay.setName(Integer.toString(iDayComponent - 7));
            }
        }
        m_bFirstTime = false;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents()
    {
        ClassLoader cl = this.getClass().getClassLoader();

        this.setLayout(new javax.swing.BoxLayout(this, BoxLayout.Y_AXIS));

        m_panelMonth = new javax.swing.JPanel();
        m_panelMonth.setLayout (new javax.swing.BoxLayout(m_panelMonth, 0));
        m_buttonPrevMonth = new javax.swing.JButton();
        m_labelMonth = new javax.swing.JLabel();
        m_buttonPrevMonth.setPreferredSize(new java.awt.Dimension(22, 17));
        m_buttonPrevMonth.setMaximumSize(new java.awt.Dimension(22, 17));
        m_buttonPrevMonth.setFont(new java.awt.Font ("Dialog", 1, 11));
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + "Back" + ".gif"));
            m_buttonPrevMonth.setIcon(icon);
        } catch (Exception ex)  {
            m_buttonPrevMonth.setText("<");
        }
        m_buttonPrevMonth.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_buttonPrevMonth.setMinimumSize(new java.awt.Dimension(22, 17));
        m_buttonPrevMonth.addActionListener(new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                prevMonthActionPerformed (evt);
            }
        }
        );
        m_panelMonth.add(m_buttonPrevMonth);
        m_labelMonth.setPreferredSize (new java.awt.Dimension(80, 17));
        m_labelMonth.setMinimumSize (new java.awt.Dimension(80, 17));
        m_labelMonth.setText ("month");
        m_labelMonth.setHorizontalAlignment (javax.swing.SwingConstants.CENTER);
        m_labelMonth.setFont (new java.awt.Font ("Dialog", 0, 12));
        m_labelMonth.setMaximumSize (new java.awt.Dimension(200, 20));
        m_panelMonth.add (m_labelMonth);

        m_buttonNextMonth = new javax.swing.JButton ();
        m_buttonNextMonth.setAlignmentX (1.0F);
        m_buttonNextMonth.setPreferredSize (new java.awt.Dimension(22, 17));
        m_buttonNextMonth.setMaximumSize (new java.awt.Dimension(22, 17));
        m_buttonNextMonth.setFont (new java.awt.Font ("Dialog", 1, 11));
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + "Forward" + ".gif"));
            m_buttonNextMonth.setIcon(icon);
        } catch (Exception ex)  {
            m_buttonNextMonth.setText(">");
        }
        m_buttonNextMonth.setMargin (new java.awt.Insets(2, 2, 2, 2));
        m_buttonNextMonth.setMinimumSize (new java.awt.Dimension(22, 17));
        m_buttonNextMonth.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                nextMonthActionPerformed (evt);
            }
        }
        );
        m_panelMonth.add (m_buttonNextMonth);
        add (m_panelMonth);

        m_panelYear = new javax.swing.JPanel ();
        m_panelYear.setLayout (new javax.swing.BoxLayout (m_panelYear, 0));
        m_buttonPrevYear = new javax.swing.JButton ();
        m_labelYear = new javax.swing.JLabel ();
        m_buttonPrevYear.setPreferredSize (new java.awt.Dimension(22, 17));
        m_buttonPrevYear.setMaximumSize (new java.awt.Dimension(22, 17));
        m_buttonPrevYear.setFont (new java.awt.Font ("Dialog", 1, 11));
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + "Back" + ".gif"));
            m_buttonPrevYear.setIcon(icon);
        } catch (Exception ex)  {
            m_buttonPrevYear.setText("<");
        }
        m_buttonPrevYear.setMargin (new java.awt.Insets(2, 2, 2, 2));
        m_buttonPrevYear.setMinimumSize (new java.awt.Dimension(22, 17));
        m_buttonPrevYear.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                prevYearActionPerformed (evt);
            }
        }
        );
        m_panelYear.add (m_buttonPrevYear);
        m_labelYear.setPreferredSize (new java.awt.Dimension(44, 17));
        m_labelYear.setMinimumSize (new java.awt.Dimension(44, 17));
        m_labelYear.setText ("Year");
        m_labelYear.setHorizontalAlignment (javax.swing.SwingConstants.CENTER);
        m_labelYear.setFont (new java.awt.Font ("Dialog", 0, 12));
        m_labelYear.setMaximumSize (new java.awt.Dimension(200, 20));
        m_panelYear.add (m_labelYear);

        m_buttonNextYear = new javax.swing.JButton ();
        m_buttonNextYear.setAlignmentX (1.0F);
        m_buttonNextYear.setPreferredSize (new java.awt.Dimension(22, 17));
        m_buttonNextYear.setMaximumSize (new java.awt.Dimension(22, 17));
        m_buttonNextYear.setFont (new java.awt.Font ("Dialog", 1, 11));
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + "Forward" + ".gif"));
            m_buttonNextYear.setIcon(icon);
        } catch (Exception ex)  {
            m_buttonNextYear.setText(">");
        }
        m_buttonNextYear.setMargin (new java.awt.Insets(2, 2, 2, 2));
        m_buttonNextYear.setMinimumSize (new java.awt.Dimension(22, 17));
        m_buttonNextYear.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                nextYearActionPerformed (evt);
            }
        }
        );
        m_panelYear.add (m_buttonNextYear);
        add (m_panelYear);

        m_panelDays = new javax.swing.JPanel ();
        m_panelDays.setLayout (new java.awt.GridLayout (7, 7));
        for (int i = 1; i <= 7; i++)
        {
            JLabel label = new JLabel();
            label.setText(Integer.toString(i));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new java.awt.Font ("Dialog", 1, 11));
            m_panelDays.add(label);
        }

        for (int i = 1; i <= 7 * 6; i++)
        {
            JLabel label = new JLabel();
            label.setBorder(EMPTY_BORDER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            m_panelDays.add(label);
        }

        add (m_panelDays);
        
        this.setBorder(new EmptyBorder(2, 2, 2, 2));
    }
    /**
     * User pressed the "next month" button, change the calendar.
     * @param evt The action event (ignored).
     */
    private void nextMonthActionPerformed (java.awt.event.ActionEvent evt)
    {
        m_calendar.setTime(m_datePanelTarget);
        m_calendar.add(Calendar.MONTH, 1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_datePanelTarget = m_calendar.getTime();
        this.layoutCalendar(m_datePanelTarget);
    }
    /**
     * User pressed the "previous month" button, change the calendar.
     * @param evt The action event (ignored).
     */
    private void prevMonthActionPerformed (java.awt.event.ActionEvent evt)
    {
        m_calendar.setTime(m_datePanelTarget);
        m_calendar.add(Calendar.MONTH, -1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_datePanelTarget = m_calendar.getTime();
        this.layoutCalendar(m_datePanelTarget);
    }
    /**
     * User pressed the "next year" button, change the calendar.
     * @param evt The action event (ignored).
     */
    private void nextYearActionPerformed (java.awt.event.ActionEvent evt)
    {
        m_calendar.setTime(m_datePanelTarget);
        m_calendar.add(Calendar.YEAR, 1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_datePanelTarget = m_calendar.getTime();
        this.layoutCalendar(m_datePanelTarget);
    }
    /**
     * User pressed the "previous year" button, change the calendar.
     * @param evt The action event (ignored).
     */
    private void prevYearActionPerformed (java.awt.event.ActionEvent evt)
    {
        m_calendar.setTime(m_datePanelTarget);
        m_calendar.add(Calendar.YEAR, -1);
        m_calendar.set(Calendar.HOUR_OF_DAY, 12);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);
        m_datePanelTarget = m_calendar.getTime();
        this.layoutCalendar(m_datePanelTarget);
    }
    /**
     * Given the first date of the calendar, get the first date of that week.
     * @param dateTarget A valid date.
     * @return The first day in the week (day of week depends on Locale).
     */
    public Date getFirstDateInCalendar(Date dateTarget)
    {
        // Now get the first box on the calendar
        int iFirstDayOfWeek = m_calendar.getFirstDayOfWeek();
        m_calendar.setTime(dateTarget);
        int iTargetDayOfWeek = m_calendar.get(Calendar.DAY_OF_WEEK);
        int iOffset = -Math.abs(iTargetDayOfWeek - iFirstDayOfWeek);
        m_calendar.add(Calendar.DATE, iOffset);

        m_calendar.set(Calendar.HOUR_OF_DAY, 0);
        m_calendar.set(Calendar.MINUTE, 0);
        m_calendar.set(Calendar.SECOND, 0);
        m_calendar.set(Calendar.MILLISECOND, 0);

        return m_calendar.getTime();
    }
    /**
     * Convert this data to a string (using the supplied format).
     * @param dateTarget The date to convert to a string.
     * @param iDateFormat The format for the date.
     * @return The date as a string.
     */
    public String getDateString(Date dateTarget, int iDateFormat)
    {
        m_sb.setLength(0);
        FieldPosition fieldPosition = new FieldPosition(iDateFormat);
        String string = null;
        string = m_df.format(dateTarget, m_sb, fieldPosition).toString();
        int iBegin = fieldPosition.getBeginIndex();
        int iEnd = fieldPosition.getEndIndex();
        string = string.substring(iBegin, iEnd);
        return string;
    }
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent evt)
    {
        JLabel button = (JLabel)evt.getSource();
        int iOffsetDay = Integer.parseInt(button.getName());
        iOffsetDay -= m_iTargetComponent;
        m_calendar.setTime(m_dateSelected);
        int hour = m_calendar.get(Calendar.HOUR_OF_DAY);
        int minute = m_calendar.get(Calendar.MINUTE);
        int second = m_calendar.get(Calendar.SECOND);
        int ms = m_calendar.get(Calendar.MILLISECOND);
        m_calendar.setTime(m_dateTarget);
        m_calendar.add(Calendar.DATE, iOffsetDay);
        if (hour == 0)
            if (minute == 0)
            if (second == 0)
            if (ms == 0)
                hour = 12;
        m_calendar.set(Calendar.HOUR_OF_DAY, hour);
        m_calendar.set(Calendar.MINUTE, minute);
        m_calendar.set(Calendar.SECOND, second);
        m_calendar.set(Calendar.MILLISECOND, ms);
        Date date = m_calendar.getTime();
        JPopupMenu popupMenu = this.getJPopupMenu();
        if (popupMenu != null)
        { // I'm not sure this is the correct code, but here it is!
            Component invoker = popupMenu.getInvoker();
            this.getParent().remove(this);      // Just being careful
            Container container = popupMenu.getParent();
            container.remove(popupMenu);
            popupMenu.setVisible(false);
            if (invoker != null)
                if (m_bTransferFocus)
                    invoker.transferFocus();    // Focus on next component after invoker
        }
        Date oldDate = m_dateSelected;
        if (m_dateSelected == m_dateTarget)
            oldDate = null;
        this.firePropertyChange(m_strDateParam, oldDate, date);
    }
    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e)
    {
    }
    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e)
    {
    }
    private Border oldBorder = EMPTY_BORDER;
    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent evt)
    {
        JLabel button = (JLabel)evt.getSource();
        oldBorder = button.getBorder();
        button.setBorder(ROLLOVER_BORDER);
    }
    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent evt)
    {
        JLabel button = (JLabel)evt.getSource();
        button.setBorder(oldBorder);
    }
    /**
     * Get the parent popup menu.
     * @return The popup menu.
     */
    private JPopupMenu getJPopupMenu()
    {
        Container parent = this.getParent();
        while (parent != null)
        {
            if (parent instanceof JPopupMenu)
                return (JPopupMenu)parent;
            parent = parent.getParent();
        }
        return null;
    }
    /**
     * Create this calendar in a popup menu and synchronize the text field on change.
     * @param dateTarget The initial date for this button.
     * @param button The calling button.
     */
    public static JCalendarPopup createCalendarPopup(Date dateTarget, Component button)
    {
        return JCalendarPopup.createCalendarPopup(null, dateTarget, button, null);
    }
    /**
     * Create this calendar in a popup menu and synchronize the text field on change.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     * @param button The calling button.
     */
    public static JCalendarPopup createCalendarPopup(String strDateParam, Date dateTarget, Component button)
    {
        return JCalendarPopup.createCalendarPopup(null, dateTarget, button, null);
    }
    /**
     * Create this calendar in a popup menu and synchronize the text field on change.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     * @param strLanguage The language to use.
     * @param button The calling button.
     */
    public static JCalendarPopup createCalendarPopup(String strDateParam, Date dateTarget, Component button, String strLanguage)
    {
        JPopupMenu popup = new JPopupMenu();
        JComponent c = (JComponent)popup; //?.getContentPane();
        c.setLayout(new BorderLayout());
        JCalendarPopup calendar = new JCalendarPopup(strDateParam, dateTarget, strLanguage);
        c.add(calendar, BorderLayout.CENTER);
        popup.show(button, button.getBounds().width, 0);
        return calendar;
    }
    /**
     * Create this calendar in a popup menu and synchronize the text field on change.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     */
    public static JButton createCalendarButton(String strDateParam, Date dateTarget)
    {
        JCalendarButton button = new JCalendarButton(strDateParam, dateTarget);
        button.setMargin(NO_INSETS);
        button.setOpaque(false);

        return button;
    }
    /**
     * Enable/Disable the transfer of focus after selecting date.
     */
    public void setTransferFocus(boolean bTransferFocus)
    {
        m_bTransferFocus = bTransferFocus;
    }
}
