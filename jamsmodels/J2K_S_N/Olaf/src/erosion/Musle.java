/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package erosion;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringTokenizer;

@Description
    ("Musle.")
@Keywords
    ("Erosion")
@VersionInfo
    ("$Id: Musle.java 893 2010-01-29 16:06:46Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/erosion/Musle.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class Musle  {
    
    @Description ("Current hru object")
    @In public HashMap<String, Double> hru;
    
    @Description ("Current time")
    @In public java.util.Calendar time;
        
    @Description ("state variable rain")
    @In public double rain;
  
    @Description ("precipitation")
    @Unit("mm")
    @In public double precip;
   
    @Description ("HRU statevar RD1")
    @In public double outRD1;

    @Description ("soil loss")
    @Unit("t/d")
    @Out  public double gensed;
 
    // TODO READWRITE subst
    @Description ("HRU statevar sediment inflow")
    public double insed;
     
    @Description ("HRU statevar sediment outflow")
    @Out public double outsed;
     
    @Description ("HRU statevar sediment outflow")
    @Out public double akksed;
    
    @Description ("HRU statevar sediment outflow")
    @Out public double sedpool;
    
    double run_outRD1, run_outsed, run_insed, run_gensed, run_akksed;

    @Execute public void execute()  {
        double Sfac = 0;
        double Pfac = 0;

        double rainmus = rain;
        rainmus = Math.ceil(rainmus * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

        double precipmus = precip;
        precipmus = Math.ceil(precipmus * 100) / 100;

        HashMap<String, Double> entity = hru;

        this.run_outRD1 = outRD1;
        this.run_insed = insed;

        int Day = time.get(java.util.Calendar.DAY_OF_YEAR);

        //System.out.println("Tag: " + Day+ "RD1 l/hru :" + this.run_genRD1);

        //Regen gr??er 0.0 mm dann erst m?glicher Bodenabtrag
        if (this.run_outRD1 > 0) {
            this.run_gensed = 0;
            this.run_outsed = 0;
            this.run_akksed = 0;

            double id = entity.get("ID");
            double are = entity.get("area");
            double slope = entity.get("slope");
            double Cfac = entity.get("C_factor");
            double ROK = entity.get("A_skel_h0");
            double flowlength = entity.get("flowlength");
            double Kfac = entity.get("kvalue_h0"); //[kg*h*N-1*m-2]

            double are_ha = are / 10000; //m2 in ha

            //System.out.println(" RD1 l/hru : " + this.run_genRD1);
            double overflow = (this.run_outRD1 / are) * 10000 ; // outRD1 kommt in Liter/HRU und wird zu mm/ha
            //System.out.println(" RD1 mm/ha : " + overflow);
//
//            if (Day == 2000 && id == 104.0) {
//                double[][] datei = null;
//                datei = Einlesen("C:/c8kiho/wd_erosion/L_Faktor1/xport1.asc");
//                Ausgabe(datei, "C:/c8kiho/wd_erosion/L_Faktor1/neu.txt");
//            }

            //slope-Umrechung von ï¿½ in %
            double slopeperc = Math.round((Math.tan(Math.toRadians(slope))) * 100);

            //Hangneigunsgradient Sfac = S-Factor f?r slope kleiner 9% und g??er gleich 9%
            if (slopeperc >= 9) {
                Sfac = 16.8 * Math.sin(Math.toRadians(slope)) - 0.5; // steil
            } else {
                Sfac = 10.8 * Math.sin(Math.toRadians(slope)) + 0.03; // flach
            }

            Sfac = Math.ceil(Sfac * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

            double rainm = rainmus / are; // Regen in mm / m?
            rainm = Math.ceil(rainm * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

            double Lfacbeta = (Math.sin(Math.toRadians(slope)) / 0.0896) / (3 * Math.pow(Math.sin(Math.toRadians(slope)), 0.8) + 0.56);
            Lfacbeta = Math.ceil(Lfacbeta * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

            double Lfacm = Lfacbeta / (1 + Lfacbeta);
            Lfacm = Math.ceil(Lfacm * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

            double Lfac = Math.pow((flowlength / 22.13), Lfacm);

            Lfac = Math.ceil(Lfac * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

            double LSfac = Lfac * Sfac;
            LSfac = Math.ceil(LSfac * 100) / 100;// Formatierung auf 2 Nachkommastellen 0.00

            double Pvorl = 0.4 * 0.02 * slopeperc;
            double HLkrit = 170 * Math.pow((Math.E), (-0.13 * slopeperc));
            if (flowlength < HLkrit) {
                Pfac = Pvorl;
            } else {
                Pfac = 1;
            }

            double ROKF = Math.pow((Math.E), (-0.53 * ROK));
            double alpha = 1 - Math.exp(24 * (Math.log(1 - 0.6)));
            double tpeak = (alpha * overflow * are_ha) / (3.6 * 12);
//--> m3/s ??? tpeak

            //if (overflow > 0.1)  System.out.println(" ScheitelQ : " + tpeak + " m3/s" + " Overflow: "+ overflow);

            double sedperhainto = (11.8 * Math.pow(((overflow* tpeak) * are_ha), 0.56)) * Kfac * LSfac * Pfac * Cfac * ROKF;

            //this.run_gensed = sedperkminto *(are / 10000); // t / HRU
            this.run_gensed = sedperhainto;


            //System.out.println(" K: " + Kfac + " Flowlg :" + flowlength + " L :" + Lfac + " S :" + Sfac + " Surf: " + this.run_inRD1 + " P: " + Pfac );

            //if (sedmusle > 0.0) {
            // System.out.println("ID " + id + "hru/ha" + are_ha + ": SED_musle " + this.run_gensed + " Rain/m2: " + rainm + " Overflow: " + this.run_outRD1);
            //System.out.println(" ID: " + id + " Flaeche :" + are + " Slope: " + slope + " Sfac: "+ Sfac + " Rain: " + rainmus + " Precip: " + precipmus + " Rain/m2: " + rainm + " Lfacm: " + Lfacm + " LS: " + LSfac + " Lfac: " + Lfac);

            gensed = this.run_gensed;

            double raus = 0;
            double acc = 0;
            double accpool = entity.get("sedpool");
            double wert1 = this.run_gensed - this.run_insed;
            double neuaccpool = accpool - wert1;

            if (neuaccpool < 0) {
                    raus = (-1 * neuaccpool);
                    neuaccpool=0;
            } else {
                if (wert1 < 0) {
                    acc = (-1 * wert1);
                    neuaccpool = accpool + acc;
                }
            }

            sedpool = neuaccpool;
            outsed = raus;
            insed = raus;

            //if (accpool > 1000) {
               //System.out.println("ID " + id + " Pool1: " + neuaccpool + " gen: " + this.run_gensed + " in: " + this.run_insed + " akk: " + acc + " out: " + raus);
            //}
            akksed = acc;
             neuaccpool = entity.get("sedpool");
//            entity.setDouble("sedpool", neuaccpool);
        }
    }

    public double[][] Einlesen(String path) {

        int ncols = 0;
        int nrows = 0;
        double x11corner;
        double y11corner;

        double cellsize;

        double nodatavalue = 0.0;
        double grid[][] = null;

        double lowest_value = Double.POSITIVE_INFINITY;
        try {
            //search for ncols, nrows, x11corner, y11corner, cellsize, nodatevalue
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int datafound = 0;
            String line;
            StringTokenizer st;

            while (datafound < 5) {
                line = reader.readLine();
                st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    if (tok.contains("ncols")) {
                        ncols = new Integer(st.nextToken()).intValue();
                        datafound++;
                    }
                    if (tok.contains("nrows")) {
                        nrows = new Integer(st.nextToken()).intValue();
                        datafound++;
                    }
                    if (tok.contains("xllcorner")) {
                        x11corner = new Double(st.nextToken()).doubleValue();
                        datafound++;
                    }
                    if (tok.contains("yllcorner")) {
                        y11corner = new Double(st.nextToken()).doubleValue();
                        datafound++;
                    }
                    if (tok.contains("cellsize")) {
                        cellsize = new Double(st.nextToken()).doubleValue();
                        datafound++;
                    }
                }
            }
            //now read grid

            grid = new double[ncols][nrows];

            int x = 0;
            int y = 0;
            boolean firsttoken = true;
            while ((line = reader.readLine()) != null) {
                st = new StringTokenizer(line);

                while (st.hasMoreTokens()) {
                    if (y >= nrows) {
                        System.out.println("error to many entrys");
                        break;
                    }
                    String tok = st.nextToken();
                    //look for optional components
                    if (firsttoken) {
                        firsttoken = false;
                        //optional
                        if (tok.contains("NODATA_value")) {
                            nodatavalue = new Double(st.nextToken()).doubleValue();
                        }
                        if (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        } else {
                            continue;
                        }
                    }
                    grid[x][y] = new Double(tok).doubleValue();

                    if (Math.abs(grid[x][y] - nodatavalue) < 0.0001) {
                        grid[x][y] = -1.0;
                    }
                    if (grid[x][y] != -1.0 && grid[x][y] < lowest_value) {
                        lowest_value = grid[x][y];
                    }
                    x++;
                    if (x >= ncols) {
                        x = 0;
                        y++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grid;
    }

//Funktion zur Ausgabe
    public void Ausgabe(double[][] fuellstand, String path) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            for (int spalte = 0; spalte < fuellstand[0].length; spalte++) {
                for (int zeile = 0; zeile < fuellstand.length; zeile++) {
                    writer.write((int) (fuellstand[zeile][spalte]) + " ");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
