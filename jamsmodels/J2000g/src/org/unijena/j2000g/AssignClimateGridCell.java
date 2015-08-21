/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2000g;

import jams.data.Attribute;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.Iterator;

/**
 *
 * @author c0krpe
 */
public class AssignClimateGridCell extends JAMSComponent{
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "EntitySet of modelling units"
            )
            public Attribute.EntityCollection entitySet;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of numerical GRID cell ids",
            defaultValue=""
            )
            public Attribute.DoubleArray statId;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Column of first data value",
            defaultValue = "gridcellID"
            )
            public Attribute.String gridCellColumnName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Column of first data value",
            defaultValue = "gridcellID"
            )
            public Attribute.String gridCellAttributeName;

    public void init() {
       System.out.println("init in ACGC");
       Iterator<Attribute.Entity> unitIterator;
       Attribute.Entity e;
       double[] sid = statId.getValue();

        unitIterator = entitySet.getEntities().iterator();
        while (unitIterator.hasNext()) {
            e = unitIterator.next();
            boolean notFound = true;
            int idx = 0;
            try{
                double gc = e.getDouble(gridCellColumnName.getValue());
                while(notFound){
                    if(gc == sid[idx]){
                        notFound = false;
                        e.setInt(gridCellAttributeName.getValue(), idx+1);
                    }
                    idx++;
                }
            }catch(NoSuchAttributeException exc){
            }
        }
       }

    public void run() {
    System.out.println("run in ACGC");
       
    }



    public void cleanup() {

    }
}
