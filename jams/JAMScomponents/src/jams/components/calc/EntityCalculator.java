/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.calc;

import de.odysseus.el.util.SimpleContext;
import de.odysseus.el.util.SimpleResolver;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.Entity;
import jams.model.Context;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 *
 * @author christian
 */
public class EntityCalculator extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the attribute for which values are calculated")
    public Attribute.String targetAttribute;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the attribute for which values are calculated")
    public Attribute.EntityCollection entities;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time object")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time period")
    public Attribute.TimeInterval I;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "expression")
    public Attribute.String expr;
    
    transient ExpressionFactory factory = null;
    transient SimpleContext exprContext = new SimpleContext(new SimpleResolver());
    ValueExpression entityExpr = null;
    ValueExpression valueExpr = null;
    ValueExpression timeExpr = null;
    ValueExpression intervalExpr = null;

    transient boolean isInit = false;
    
    public void reinit() {
        java.util.Properties properties = new java.util.Properties();
        properties.put("javax.el.cacheSize", "1000");
        properties.put("javax.el.methodInvocations", "false");
        properties.put("javax.el.nullProperties", "false");
        properties.put("javax.el.varArgs", "false");
        properties.put("javax.el.ignoreReturnType", "false");
        
        factory = new de.odysseus.el.ExpressionFactoryImpl(properties);        

        exprContext = getExpressionContext();

        valueExpr = factory.createValueExpression(exprContext, "${" + this.expr.getValue() + "}", double.class);                
        timeExpr = factory.createValueExpression(exprContext, "${time}", Calendar.class);
        intervalExpr = factory.createValueExpression(exprContext, "${I}", Attribute.TimeInterval.class);
        entityExpr = factory.createValueExpression(exprContext, "${E}", Attribute.Entity.class);
        
        Context searchContext = this.getContext();
        while (searchContext != null) {
            ValueExpression contextExpr = factory.createValueExpression(exprContext, "${"+searchContext.getInstanceName()+"}", Context.class);
            contextExpr.setValue(exprContext, searchContext);
            searchContext = searchContext.getContext();                
        } 
        isInit = true;
    }

    /**
     *
     * @param c1
     * @param c2
     * @param accuracy
     * @return
     */
    public static double getEntityAttribute(Entity e, String attributeName){        
        try{
            return e.getDouble(attributeName);
        }catch(Entity.NoSuchAttributeException nsae){
            nsae.printStackTrace();
            return 0;
        }
    }
    
    private static SimpleContext getExpressionContext(){
        SimpleContext context = DoubleFunctions.getContext();
        try{ 
            context.setFunction("", "getEntityAttribute", EntityCalculator.class.getMethod("getEntityAttribute", Entity.class, String.class)); 
        }catch(NoSuchMethodException nsme){            
            nsme.printStackTrace();
        }
        return context;
    }
    @Override
    public void run() {     
        //needs to be initialized
        if (!isInit){
            reinit();
        }
        if (this.time != null && this.time.getValue() != null) {
            timeExpr.setValue(exprContext, time.getValue());
        }
        
        if (this.I != null && this.I.getValue() != null) {
            intervalExpr.setValue(exprContext, I);
        }
        
        for (Entity e : entities.getEntities()){
            entityExpr.setValue(exprContext, e);
            
            Double d = (Double)valueExpr.getValue(exprContext);
            
            e.setDouble(targetAttribute.getValue(), d);
        }
    }
    
    private void readObject(ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        objIn.defaultReadObject();
        
        isInit = false;
    }
}
