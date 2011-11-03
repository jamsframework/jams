/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import java.io.Serializable;
import java.text.ParseException;
import optas.metamodel.Tools.Range;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author chris
 */
public class Parameter extends AttributeWrapper implements Serializable {

    private double lowerBound;
    private double upperBound;
    private boolean startValueValid;
    private double startValue;
    private int id;

    static int staticID = 0;
    public Parameter(){
        id = staticID++;
    }
    public Parameter(AttributeWrapper attr) {
        id = staticID++;

        startValueValid = false;
        setAttributeName(attr.getAttributeName());
        setComponentName(attr.getComponentName());
        setContextName(attr.getContextName());
        setVariableName(attr.getVariableName());
        setIsSetByValue(attr.isIsSetByValue());        
    }

    public Parameter(AttributeWrapper attr, Range range) {
        id = staticID++;
        
        startValueValid = false;
        setAttributeName(attr.getAttributeName());
        setComponentName(attr.getComponentName());
        setContextName(attr.getContextName());
        setVariableName(attr.getVariableName());
        setIsSetByValue(attr.isIsSetByValue());
        
        if (range != null) {
            this.lowerBound = range.lowerBound;
            this.upperBound = range.upperBound;
        }
    }
        
    /**
     * @return the lowerBound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * @param lowerBound the lowerBound to set
     */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * @return the upperBound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * @param upperBound the upperBound to set
     */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * @return the startValueValid
     */
    public boolean isStartValueValid() {
        return startValueValid;
    }

    /**
     * @param startValueValid the startValueValid to set
     */
    public void setStartValueValid(boolean startValueValid) {
        this.startValueValid = startValueValid;
    }

    /**
     * @return the startValue
     */
    public double getStartValue() {
        return startValue;
    }

    /**
     * @param startValue the startValue to set
     */
    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
}
