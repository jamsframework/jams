/**
 * CalendarButton.java
 *
 * @author Don Corley <don@donandann.com>
 * @version 1.0.0
 */
 
package jams.gui.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/** 
 * A JCalendarButton is a button that displays a popup calendar (A JCalendarPopup).
 * Note: This button doesn't use some of the global constants and methods
 * because it is used in other programs where they are not available.
 * @author  Administrator
 * @version 1.0.0
 */
public class JCalendarButton extends JButton
    implements PropertyChangeListener, ActionListener
{
    private static final long serialVersionUID = 1L;

    /**
     * The language property key.
     */
    public static final String LANGUAGE_PARAM = "language";
    /**
     * The name of the date property (defaults to "date").
     */
    protected String m_strDateParam = JCalendarPopup.DATE_PARAM;
    /**
     * The initial date for this button.
     */
    protected Date m_dateTarget = null;
    /**
     * The language to use.
     */
    protected String m_strLanguage = null;
    
    /**
     * Creates new CalendarButton.
     */
    public JCalendarButton()
    {
        super();
        // Get current classloader
        ClassLoader cl = this.getClass().getClassLoader();
        // Create icons
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + JCalendarPopup.CALENDAR_ICON + ".gif"));
            this.setIcon(icon);
        } catch (Exception ex)  {
            this.setText("change");
        }

        this.setMargin(JCalendarPopup.NO_INSETS);
        this.setOpaque(false);
        
        this.addActionListener(this);
    }
    /**
     * Creates new CalendarButton.
     * @param dateTarget The initial date for this button.
     */
    public JCalendarButton(Date dateTarget)
    {
        this();
        this.init(null, dateTarget, null);
    }
    /**
     * Creates new CalendarButton.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     */
    public JCalendarButton(String strDateParam, Date dateTarget)
    {
    	this();
        this.init(strDateParam, dateTarget, null);
    }
    /**
     * Creates new CalendarButton.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     * @param strLanguage The language to use.
     */
    public JCalendarButton(String strDateParam, Date dateTarget, String strLanguage)
    {
    	this();
        this.init(strDateParam, dateTarget, strLanguage);
    }
    /**
     * Creates new CalendarButton.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param dateTarget The initial date for this button.
     * @param strLanguage The language to use.
     */
    public void init(String strDateParam, Date dateTarget, String strLanguage)
    {
        if (strDateParam == null)
            strDateParam = JCalendarPopup.DATE_PARAM;
        m_strDateParam = strDateParam; 
        m_dateTarget = dateTarget;
        m_strLanguage = strLanguage;
    }
    /**
     * Get the current date.
     */
    public Date getTargetDate()
    {
        return m_dateTarget;
    }
    /**
     * Set the current date.
     */
    public void setTargetDate(Date dateTarget)
    {
        m_dateTarget = dateTarget;
    }
    /**
     * Get the name of the date property for this button.
     */
    public String getDateParam()
    {
        return m_strDateParam;
    }
    /**
     * The user pressed the button, display the JCalendarPopup.
     * @param e The ActionEvent.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this)
        {
            Date dateTarget = this.getTargetDate();
            JCalendarPopup popup = JCalendarPopup.createCalendarPopup(this.getDateParam(), dateTarget, this, m_strLanguage);
            popup.addPropertyChangeListener(this);
        }
    }
    /**
     * Propagate the change to my listeners.
     * Watch for date and language changes, so I can keep up to date.
     * @param evt The property change event.
     */
    public void propertyChange(final java.beans.PropertyChangeEvent evt)
    {
        this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (m_strDateParam.equalsIgnoreCase(evt.getPropertyName()))
            if (evt.getNewValue() instanceof Date)
                m_dateTarget = (Date)evt.getNewValue();
        if (LANGUAGE_PARAM.equalsIgnoreCase(evt.getPropertyName()))
            if (evt.getNewValue() instanceof String)
        {
                m_strLanguage = (String)evt.getNewValue();
        }
    }
}
