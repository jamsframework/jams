/**
 * SIDRA: SImulation DRAinage
 * Model for simulation of water table dynamics in tile-drained agricultural fields
 * First version of SIDRA developed by Lesaffre and Zimmer (1988), Cemagref
 * Current version based on the PESTDRAIN model (Branger et al., 2009), Cemagref
 */

package sidra;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author nsk
 */
 @JAMSComponentDescription(
        title="SIDRA: SImulation DRAinage",
        author="nsk",
        description="SIDRA: SImulation DRAinage"
         + "Model for simulation of water table dynamics in tile-drained agricultural fields"
         + "First version of SIDRA developed by Lesaffre and Zimmer (1988), Cemagref"
         + "Current version based on the PESTDRAIN model (Branger et al., 2009), Cemagref",
        date = "2012-03-12",
        version = "0.1_0"
        )
public class SIDRA extends JAMSComponent {

    /*
     *  Component attributes
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precip",
            unit = "mm"
            )
            public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current water table level",
            unit = "m"
            )
            public Attribute.Double h;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated discharge",
            unit = "mm"
            )
            public Attribute.Double q;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total discharge",
            unit = "m³/s"
            )
            public Attribute.Double Q;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time step",
            defaultValue = "3600",
            unit = "s"
            )
            public Attribute.Integer timestep;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "initial water table level",
            unit = "m"
            )
            public Attribute.Double h0;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "field surface",
            unit = "m²"
            )
            public Attribute.Double s;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "half drain spacing",
            unit = "m"
            )
            public Attribute.Double l;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hydraulic conductivity",
            unit = "m"
            )
            public Attribute.Double k;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "drainable porosity"
            )
            public Attribute.Double mu;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "watertable shape factor1"
            )
            public Attribute.Double a1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "watertable shape factor2"
            )
            public Attribute.Double a2;
    
      
    /*
     *  Component run stages
     */
    
    @Override
    public void run() {
        sidra();
    }
    
    private void sidra() {

        double precip = this.precip.getValue();
        double q = this.q.getValue();
        double h = this.h.getValue();
        double timestep = this.timestep.getValue();
        double h0 = this.h0.getValue();
        double s = this.s.getValue();
        double l = this.l.getValue();
        double k = this.k.getValue();
        double mu = this.mu.getValue();
        double a1 = this.a1.getValue();
        double a2 = this.a2.getValue();
        
        // convert values from the R input vector in m/s
        double r = precip / (1000 * timestep);
        
        // calculate the variation of h (in m/s)
        double varh = (r - k * h*h / (l*l)) / (mu * a2);
        
        // update h
        h = h + varh * timestep;
        
        // calculate discharge
        q = a1 * k * h*h / (l*l) + (1 - a1) * r;
        
        this.h.setValue(h);
        this.Q.setValue(q * s);

        // convert Output q in mm/time step
        q = q * 1000 * timestep;
        
        this.q.setValue(q);
                
    }
    
}