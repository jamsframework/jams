/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kirchner_methodology;

/**
 *
 * @author admin_adamovic
 */
/**
 * JAMS example component - can be used as template for new components
 */
import jams.data.*;
import jams.model.*;

/**
 *
 * @author John Doe
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class Runoff_generator extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double precip;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
   public Attribute.Double area;
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double actET;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double cropCoefficient;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double c1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double c2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double c3;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            defaultValue = "0.0074")
    public Attribute.Double bypassFraction;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description")
    public Attribute.Double bypass;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Description")
    public Attribute.Double discharge;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description")
    
    public Attribute.Double discharge_real;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description")
    /*
     *  Component run stages
     */

    @Override
    public void run() {

        double s, s1, s2, s3, s4;
        double dt = 1;
        double p = precip.getValue();
        double qOld = discharge.getValue();
        double qNew;
        
        
        s1 = qOld + dt/2 * (Math.exp(c1.getValue() + (c2.getValue() - 2) * qOld + c3.getValue() * qOld * qOld)) * 
                 ((p * (1 - bypassFraction.getValue())) - (cropCoefficient.getValue() * actET.getValue()) - Math.exp(qOld));

        s2 = qOld + dt/2 * (Math.exp(c1.getValue() + (c2.getValue() - 2) * s1 + c3.getValue() * s1 * s1)) * 
                 (((p * (1 - bypassFraction.getValue())) - (cropCoefficient.getValue() * actET.getValue())) - Math.exp(s1));

        s3 = qOld + dt* (Math.exp(c1.getValue() + (c2.getValue() - 2) * s2 + c3.getValue() * s2 * s2)) * 
                 (((p * (1 - bypassFraction.getValue())) - (cropCoefficient.getValue() * actET.getValue())) - Math.exp(s2));
 
        s4 = qOld + dt* (Math.exp(c1.getValue() + (c2.getValue() - 2) * s3 + c3.getValue() * s3 * s3)) * 
                 (((p * (1 - bypassFraction.getValue())) - (cropCoefficient.getValue() * actET.getValue())) - Math.exp(s3));
        
        s = (s1 / 3) + (s2 * 2 / 3) + (s3 / 3) + (s4 / 6) - qOld / 2; 

        qNew = ((0.5 * (Math.exp(qOld) + Math.exp(s))) + (p * (bypassFraction.getValue())));
        
        discharge.setValue(s);
        discharge_real.setValue(qNew * area.getValue());
              
    }
}