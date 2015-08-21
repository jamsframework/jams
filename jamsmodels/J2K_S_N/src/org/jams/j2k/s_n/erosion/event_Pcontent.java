/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author holm
 */
public class event_Pcontent extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current hru object")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment outflow")
    public JAMSDouble outsed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar total phosphorus")
    public JAMSDouble totPout;

    /** Creates a new instance of musle */
    @Override
    public void init() {
    }

    @Override
    public void run() throws JAMSEntity.NoSuchAttributeException {

        totPout.setValue(0);
        double p_con, p_sed, p_sed_100g, b = 0;

        Attribute.Entity entity = entities.getCurrent();

        p_con = entity.getDouble("p_content_h0"); // mg/100g soil
        p_sed = outsed.getValue(); //tons
        p_sed_100g = p_sed * 10000;
        b = p_sed_100g * p_con;


        totPout.setValue(b);
        //totP = entity.setDouble(null, p_sed);

        //if (totPout.getValue() > 0.0) {
           // System.out.println("ID:" + entity.getDouble("ID") + " :dry totP: " + totPout.getValue());
        //}
        
        
    }

    @Override
    public void cleanup() {
    }
}
