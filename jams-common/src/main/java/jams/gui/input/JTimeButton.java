/**
 * TimeButton.java

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
 * A JTimeButton is a button that displays a popup time (A JTimePopup).
 * Note: This button doesn't use some of the global constants and methods
 * because it is used in other programs where they are not available.
 * @author  Administrator
 * @version 1.0.0
 */
public class JTimeButton extends JButton
    implements PropertyChangeListener, ActionListener
{
	private static final long serialVersionUID = 1L;

	/**
     * The language property key.
     */
    public static final String LANGUAGE_PARAM = "language";
    /**
     * The name of the date property (defaults to "time").
     */
    protected String m_strTimeParam = JCalendarPopup.DATE_PARAM;
    /**
     * The initial date for this button.
     */
    protected Date m_timeTarget = null;
    /**
     * The language to use.
     */
    protected String m_strLanguage = null;
    
    /**
     * Creates new TimeButton.
     */
    public JTimeButton()
    {
        super();
        // Get current classloader
        ClassLoader cl = this.getClass().getClassLoader();
        // Create icons
        try   {
            Icon icon  = new ImageIcon(cl.getResource("images/buttons/" + JTimePopup.TIME_ICON + ".gif"));
            this.setIcon(icon);
        } catch (Exception ex)  {
            this.setText("change");
        }

        this.setMargin(JCalendarPopup.NO_INSETS);
        this.setOpaque(false);
        
        this.addActionListener(this);
    }
    /**
     * Creates new TimeButton.
     * @param dateTarget The initial date for this button.
     */
    public JTimeButton(Date timeTarget)
    {
    	this();
        this.init(null, timeTarget, null);
    }
    /**
     * Creates new TimeButton.
     * @param strDateParam The name of the date property (defaults to 'date').
     * @param dateTarget The initial date for this button.
     */
    public JTimeButton(String strDateParam, Date timeTarget)
    {
    	this();
        this.init(strDateParam, timeTarget, null);
    }
    /**
     * Creates new TimeButton.
     * @param strDateParam The name of the date property (defaults to 'date').
     * @param dateTarget The initial date for this button.
     * @param strLanguage The language to use.
     */
    public JTimeButton(String strDateParam, Date timeTarget, String strLanguage)
    {
    	this();
        this.init(strDateParam, timeTarget, strLanguage);
    }
    /**
     * Creates new TimeButton.
     * @param strDateParam The name of the date property (defaults to 'date').
     * @param dateTarget The initial date for this button.
     * @param strLanguage The language to use.
     */
    public void init(String strTimeParam, Date timeTarget, String strLanguage)
    {
        if (strTimeParam == null)
            strTimeParam = JCalendarPopup.DATE_PARAM;
        m_strTimeParam = strTimeParam; 
        m_timeTarget = timeTarget;
        m_strLanguage = strLanguage;
    }
    /**
     * Set the current date.
     */
    public void setTargetDate(Date timeTarget)
    {
        m_timeTarget = timeTarget;
    }
    /**
     * Get the current date.
     */
    public Date getTargetDate()
    {
        return m_timeTarget;
    }
    /**
     * Get the name of the date property for this button.
     */
    public String getDateParam()
    {
        return m_strTimeParam;
    }
    /**
     * The user pressed the button, display the JTimePopup.
     * @param e The ActionEvent.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this)
        {
            Date dateTarget = this.getTargetDate();
            JTimePopup popup = JTimePopup.createTimePopup(this.getDateParam(), dateTarget, this, m_strLanguage);
            popup.addPropertyChangeListener(this);
        }
    }
    /**
     * Propogate the change to my listeners.
     * Watch for date and language changes, so I can keep up to date.
     * @param evt The property change event.
     */
    public void propertyChange(final java.beans.PropertyChangeEvent evt)
    {
        this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (m_strTimeParam.equalsIgnoreCase(evt.getPropertyName()))
            if (evt.getNewValue() instanceof Date)
                m_timeTarget = (Date)evt.getNewValue();
        if (LANGUAGE_PARAM.equalsIgnoreCase(evt.getPropertyName()))
            if (evt.getNewValue() instanceof String)
        {
                m_strLanguage = (String)evt.getNewValue();
        }
    }
}
