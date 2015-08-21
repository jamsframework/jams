/*
 * GKConversion.java
 *
 * Created on 24. November 2005, 11:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

/**
 *
 * @author c0krpe
 */
public class GKConversion {
    
    /**
     * Creates a new instance of GKConversion
     */
    public GKConversion() {
    }
    
    public static double[] LatLon2GK(double lat, double lon){
        double[] gk = new double[2];
        /**
        {==============================================================================}
{ Umrechnung von Geographischen Koordinaten in Gauss-Krueger-Koordinaten       }
{ Formel: Grossmann,W., Geodï¿½tische Abbildungen, 1964, Seite 151               }
{ Parameter: geo.Breite (Grad.Min.Sek) in Altgrad  : Twinkel                   }
{            geo.Laenge (Grad.Min.Sek) in Altgrad  : Twinkel                   }
{            Zielsystemnummer (Meridiankennziffer) : longint                   }
{            Rechtswert (X) im Zielsystem          : double                    }
{            Hochwert (Y) im Zielsystem            : double                    }
{==============================================================================}
procedure GeoGk(br,la:Twinkel;sy:Longint;var x,y:double);
const
  {26}
  rho = 180 / pi;
var
  brDezimal,laDezimal,rm,e2,c,bf,g,co,g2,g1,t,dl,fa,grad,min,sek :extended;
begin
  {25}
  e2 := 0.0067192188;
  {27}
  c := 6398786.849;
  {in Dezimal}
  {Breite}
  brDezimal := br.grad + br.min / 60 + br.sek / 3600;
  {Laenge}
  laDezimal := la.grad + la.min / 60 + la.sek / 3600;
  {64}
  bf := brDezimal / rho;
  {65}
  g := 111120.61962 * brDezimal
       -15988.63853 * sin(2*bf)
       +16.72995 * sin(4*bf)
       -0.02178 * sin(6*bf)
       +0.00003 * sin(8*bf);
  {70}
  co := cos(bf);
  {71}
  g2 := e2 * (co * co);
  {72}
  g1 := c / sqrt(1+g2);
  {73}
  t := sin(bf) / cos(bf); {=tan(t)}
  {74}
  dl := laDezimal - sy * 3;
  {77}
  fa := co * dl / rho;
  {78}
  y := g
       + fa * fa * t * g1 / 2
       + fa * fa * fa * fa * t * g1 * (5 - t * t + 9 * g2) / 24;
  {81}
  rm := fa * g1
        + fa * fa * fa * g1 * (1 - t * t + g2) / 6
        + fa * fa * fa * fa * fa * g1 * (5 - 18 * t * t * t * t * t * t) / 120;
  {84}
  x := rm + sy * 1000000 + 500000;
end;
         */
        return gk;
    }
    /**
     * converts Gauss-Krueger coordinates to latitute longitude in dec. degree
     * after: Grossmann,W., Geodtische Abbildungen, 1964, page 153
     * @param Rw gauss-krueger longitude (Rechtswert)
     * @param Hw gauss-krueger latitude (Hochwert)
     * @return latitude [0] and longitude [1] as dec. degree
     */
    public static double[] GK2LatLon(double Rw, double Hw){
       
        double rho = 180 / Math.PI;
        double e2 = 0.0067192188;
        double c = 6398786.849;
        int mKen = (int)(Rw / 1000000);
        double rm = Rw - mKen * 1000000 - 500000;
        double bI = Hw / 10000855.7646;
        double bISq = bI * bI;
        double bf = 325632.08677 * bI *((((((0.00000562025
                * bISq - 0.00004363980)
                * bISq + 0.00022976983)
                * bISq - 0.00113566119)
                * bISq + 0.00424914906)
                * bISq - 0.00831729565)
                * bISq + 1);
//                * Math.pow(bI, 2) - 0.00004363980)
//                * Math.pow(bI, 2) + 0.00022976983)
//                * Math.pow(bI, 2) - 0.00113566119)
//                * Math.pow(bI, 2) + 0.00424914906)
//                * Math.pow(bI, 2) - 0.00831729565)
//                * Math.pow(bI, 2) + 1);
        
        bf = bf / 3600 / rho;
        
        double g2 = e2 * (Math.pow(Math.cos(bf),2));
        double g1 = c / Math.sqrt(1 + g2);
        double t = Math.tan(bf);
        double tSq = t * t;
        double fa = rm / g1;
        
        double dl = fa - Math.pow(fa, 3) * (1 + 2 * tSq + g2) / 6
                + Math.pow(fa,5) * (1 + 28 * tSq + 24 * Math.pow(t,4)) / 120;
        
        double[] LatLon = new double[2];
        LatLon[0] = (bf - fa * fa * t * (1 + g2) / 2
                + Math.pow(fa,4) * t * (5 + 3 * tSq + 6 * g2 - 6 * g2 * tSq) / 24) * rho;
        LatLon[1] = dl * rho / Math.cos(bf) + mKen * 3;
        return LatLon;
    }

}
