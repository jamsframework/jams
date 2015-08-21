/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package staging.j2k.types;

/**
 *
 * @author od
 */
public class Utils {

     public static String soils(HRU h) {
        return "HRU [satSoil=" + h.satSoil + ","+
                "maxMPS=" + h.maxMPS + "," +
                "maxLPS=" + h.maxLPS + "," +
                "actMPS=" + h.actMPS + "," +
                "actLPS=" + h.actLPS + "," +
                "satMPS=" + h.satMPS + "," +
                "inRD1=" + h.inRD1 + "," +
                "inRD2=" + h.inRD2 + "," +
                "outRD1=" + h.outRD1 + "," +
                "outRD2=" + h.outRD2 + "," +
                "interflow=" + h.interflow + "]";
    }

}
