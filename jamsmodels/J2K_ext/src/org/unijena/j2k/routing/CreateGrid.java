/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.routing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.awt.Point;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import jams.data.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author sa63kul
 */
public class CreateGrid extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "spatial grid resolution")
    public Attribute.Double gridresolution;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Time bin of simulation")
    public Attribute.Double timescale;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "The current hru entity")
    public Attribute.Entity information;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "The current hru entity")
    public Attribute.Entity fuellstand;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RD1_Weg_Koeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RD2_Weg_Koeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RG1_Weg_Koeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RG2_Weg_Koeff;
    
    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.String hruRasterFile;
    
    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.String dgmFile;
    
    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.String flgewFile;
        
    final int MAGIC_NUMBER = 123456;
    
    public int getRasterSize(File path) {
        double cellsize = 0;
        try {
            //search for ncols, nrows, x11corner, y11corner, cellsize, nodatevalue
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int datafound = 0;
            String line;
            StringTokenizer st;

            while (datafound < 1) {
                line = reader.readLine();
                st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();

                    if (tok.contains("cellsize")) {
                        cellsize = new Double(st.nextToken()).doubleValue();
                        datafound++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return (int)cellsize;

    }

    public double[][] readRasterFile(File path) {
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
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return grid;

    }

    public void Ausgabe(Object[][] fuellstand, String path) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            for (int spalte = 0; spalte < fuellstand[0].length; spalte++) {
                for (int zeile = 0; zeile < fuellstand.length; zeile++) {
                    writer.write((fuellstand[zeile][spalte]) + " ");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public double[][] calcSlope(double[][] dgm, int[][] fliessrichtung, int cellsize) {
        //Funktion berechnet die Hangneigung. Der Winkel wird auf mindestens 1 Grad gesetzt.
        double[][] slope = new double[dgm.length][dgm[0].length];
        
        int i, j;
        double alpha;
        double a = 0;
        double b = 0;
        double c = 0;

        /*
         C
         |\ 
         |a\b 
         |  \
         B---A
         c
         */
        for (i = 0; i < dgm.length; i++) {
            Arrays.fill(slope[i], -1);
            if (i==0 || i == dgm.length-1)
                continue;
            
            for (j = 1; j < dgm[i].length-1; j++) {
                if (dgm[i][j] == -1) {
                    continue;
                }
                int flr = fliessrichtung[i][j];
                if (flr == 1) {
                    c = cellsize;
                    a = dgm[i][j] - dgm[i + 1][j];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 2) {
                    c = Math.sqrt(2) * cellsize;
                    a = dgm[i][j] - dgm[i + 1][j + 1];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 4) {
                    c = cellsize;
                    a = dgm[i][j] - dgm[i][j + 1];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 8) {
                    c = Math.sqrt(2) * cellsize;
                    a = dgm[i][j] - dgm[i - 1][j + 1];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 16) {
                    c = cellsize;
                    a = dgm[i][j] - dgm[i - 1][j];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 32) {
                    c = Math.sqrt(2) * cellsize;
                    a = dgm[i][j] - dgm[i - 1][j - 1];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 64) {
                    c = cellsize;
                    a = dgm[i][j] - dgm[i][j - 1];
                    b = Math.sqrt(a * a + c * c);
                }
                if (flr == 128) {
                    c = Math.sqrt(2) * cellsize;
                    a = dgm[i][j] - dgm[i + 1][j - 1];
                    b = Math.sqrt(a * a + c * c);
                }
                alpha = Math.asin(a / b);
                alpha = 360. * alpha / (2. * Math.PI);//Umrechnung Gradmass in Bogenmass
                if (alpha < 0.1) {
                    alpha = 0.1;
                } //negative Winkel entstehen, weil dass Fliessgewaesser nicht vollstaendig korrekt eingearbeitet wurden ist
                slope[i][j] = alpha;
            }
        }
        return slope;
    }

    public int[][] calculateFlowDirection(double[][] dgm) {
        int[][] fliessrichtung = new int[dgm.length][dgm[0].length];

        for (int i = 1; i < dgm.length - 1; i++) {
            for (int j = 1; j < dgm[0].length - 1; j++) {
                int count = 10000;
                if (dgm[i][j + 1] != -1 && dgm[i][j + 1] < count) {
                    count = (int) dgm[i][j + 1];
                    fliessrichtung[i][j] = 4;
                }
                if (dgm[i + 1][j + 1] != -1 && dgm[i + 1][j + 1] < count) {
                    count = (int) dgm[i + 1][j + 1];
                    fliessrichtung[i][j] = 2;
                }
                if (dgm[i + 1][j] != -1 && dgm[i + 1][j] < count) {
                    count = (int) dgm[i + 1][j];
                    fliessrichtung[i][j] = 1;
                }
                if (dgm[i + 1][j - 1] != -1 && dgm[i + 1][j - 1] < count) {
                    count = (int) dgm[i + 1][j - 1];
                    fliessrichtung[i][j] = 128;
                }
                if (dgm[i][j - 1] != -1 && dgm[i][j - 1] < count) {
                    count = (int) dgm[i][j - 1];
                    fliessrichtung[i][j] = 64;
                }

                if (dgm[i - 1][j - 1] != -1 && dgm[i - 1][j - 1] < count) {
                    count = (int) dgm[i - 1][j - 1];
                    fliessrichtung[i][j] = 32;
                }
                if (dgm[i - 1][j]!=-1 && dgm[i - 1][j] < count) {
                    count = (int) dgm[i - 1][j];
                    fliessrichtung[i][j] = 16;
                }
                if (dgm[i - 1][j + 1] != -1 && dgm[i - 1][j + 1] < count) {
                    count = (int) dgm[i - 1][j + 1];
                    fliessrichtung[i][j] = 8;
                }

            }
        }


        return fliessrichtung;
    }

    public int[][][] fliessgewaesser(int i, int j, int ID, int[][][] fliess) {
        //Prozedur zur besonderen Verwaltung der Flei?gew?sser
        int count = 0;
        int richtung = 0;
        int x = 0, y = 0;


        //Pixel sind direkt benachbart
        for (int k = -1; k < 2; k++) {
            for (int l = -1; l < 2; l++) {
                if (k + l == 0 || k + l == 2 || k + l == -2) {
                    //das heißt??
                } else {
                    if (fliess[1][i][j] == ID && fliess[1][i + k][j + l] == ID) {
                        if (k == 1 && l == 0) {
                            richtung = 1;
                        }
                        if (k == 0 && l == 1) {
                            richtung = 4;
                        }
                        if (k == -1 && l == 0) {
                            richtung = 16;
                        }
                        if (k == 0 && l == -1) {
                            richtung = 64;
                        }
                        x = i + k;
                        y = j + l;
                        fliess[0][i][j] = richtung;
                        fliess[1][i][j] = -1;

                        fliess = fliessgewaesser(x, y, ID, fliess);
                        k = 5;
                        l = 5;
                    }
                }
            }
        }
        
        //Pixel sind diagonal benachbart
        for (int k = -1; k < 2; k++) {
            for (int l = -1; l < 2; l++) {
                if ((k == 0 && l == 0) || k + l == 1 || k + l == -1) {
                } else {
                    if (fliess[1][i][j] == ID && fliess[1][i + k][j + l] == ID) {
                        if (k == 1 && l == 1) {
                            richtung = 2;
                        }
                        if (k == -1 && l == 1) {
                            richtung = 8;
                        }
                        if (k == -1 && l == -1) {
                            richtung = 32;
                        }
                        if (k == 1 && l == -1) {
                            richtung = 128;
                        }
                        x = i + k;
                        y = j + l;
                        fliess[0][i][j] = richtung;
                        fliess[1][i][j] = -1;
                        fliess = fliessgewaesser(x, y, ID, fliess);
                        k = 5;
                        l = 5;
                    }
                }
            }
        }

        //Fuer den Fall, dass noch keien Richtung gefunder worden ist
        //Verarbeitung des letzten Pixels
        for (int k = -1; k < 2; k++) {
            for (int l = -1; l < 2; l++) {
                if (k == 0 && l == 0) {
                } else {
                    if (fliess[1][i + k][j + l] != -1 && fliess[1][i][j] == ID) {
                        if (k == 1 && l == 0) {
                            richtung = 1;
                        }
                        if (k == 1 && l == 1) {
                            richtung = 2;
                        }
                        if (k == 0 && l == 1) {
                            richtung = 4;
                        }
                        if (k == -1 && l == 1) {
                            richtung = 8;
                        }
                        if (k == -1 && l == 0) {
                            richtung = 16;
                        }
                        if (k == -1 && l == -1) {
                            richtung = 32;
                        }
                        if (k == 0 && l == -1) {
                            richtung = 64;
                        }
                        if (k == 1 && l == -1) {
                            richtung = 128;
                        }
                        x = i + k;
                        y = j + l;
                        fliess[0][i][j] = richtung;
                        fliess[1][i][j] = -1;

                        k = 5;
                        l = 5;

                    }
                }
            }
        }
        return fliess;
    }

    public int[][] flgew(double[][] dgm, int[][] fliessrichtung, int[][] flgew, int[][] flgew1, int[][] hruraster) {

        //Variablendaklaration
        int[][][] fliess = new int[2][fliessrichtung.length][fliessrichtung[0].length];
        int count = 1;
        int anzahl = 0;

        //Initialisierung der Array fliess
        
        //erste Ebene: Fliessrichtung
        //zweite Ebene Fliessgewaesser
        for (int i = 0; i < fliessrichtung.length; i++) {
            for (int j = 0; j < fliessrichtung[0].length; j++) {
                fliess[0][i][j] = fliessrichtung[i][j];
                fliess[1][i][j] = flgew[i][j];
            }
        }

        int zahl = 1;
        
        while (zahl != 0) {
            zahl = 0;

            for (int i = 1; i < fliessrichtung.length - 1; i++) {
                for (int j = 1; j < fliessrichtung[0].length - 1; j++) {
                    if (fliess[1][i][j] != -1 && fliess[1][i][j] != MAGIC_NUMBER) //MAGIC_NUMBER steht fuer den Pegel
                    {
                        count = 0;
                        if (fliess[1][i + 1][j] == -1) {
                            count++;
                        }
                        if (fliess[1][i - 1][j] == -1) {
                            count++;
                        }
                        if (fliess[1][i][j + 1] == -1) {
                            count++;
                        }
                        if (fliess[1][i][j - 1] == -1) {
                            count++;
                        }
                        if (fliess[1][i + 1][j + 1] == -1) {
                            count++;
                        }
                        if (fliess[1][i + 1][j - 1] == -1) {
                            count++;
                        }
                        if (fliess[1][i - 1][j + 1] == -1) {
                            count++;
                        }
                        if (fliess[1][i - 1][j - 1] == -1) {
                            count++;
                        }
                        if (count == 7) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }

                        if (count == 6 && fliess[1][i + 1][j + 1] != -1 && fliess[1][i][j + 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i + 1][j + 1] != -1 && fliess[1][i + 1][j] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i + 1][j] != -1 && fliess[1][i + 1][j - 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i + 1][j - 1] != -1 && fliess[1][i][j - 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i - 1][j - 1] != -1 && fliess[1][i][j - 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i - 1][j] != -1 && fliess[1][i - 1][j - 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i - 1][j + 1] != -1 && fliess[1][i - 1][j] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                        if (count == 6 && fliess[1][i - 1][j + 1] != -1 && fliess[1][i][j + 1] != -1) {
                            fliess = fliessgewaesser(i, j, fliess[1][i][j], fliess);
                            zahl++;
                            anzahl++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < fliessrichtung.length; i++) {
            for (int j = 0; j < fliessrichtung[0].length; j++) {
                fliessrichtung[i][j] = fliess[0][i][j];
                flgew[i][j] = fliess[1][i][j];
            }
        }

        //Flussbett Bearbeitung  
        for (int i = 1; i < fliessrichtung.length - 1; i++) {
            for (int j = 1; j < fliessrichtung[0].length - 1; j++) {

                if (flgew1[i][j] != -1) {

                    if (flgew1[i + 1][j] == -1) {
                        fliessrichtung[i + 1][j] = 16;
                    }
                    if (flgew1[i - 1][j] == -1) {
                        fliessrichtung[i - 1][j] = 1;
                    }
                    if (flgew1[i][j + 1] == -1) {
                        fliessrichtung[i][j + 1] = 64;
                    }
                    if (flgew1[i][j - 1] == -1) {
                        fliessrichtung[i][j - 1] = 4;
                    }

                }
            }
        }
        return fliessrichtung;
    }

    private double[][] calculateSlopeLength(double[][] Hangneigung, double kalibrierung, double zeitintervall, double cellsize) {
        double[][] Weg = new double[Hangneigung.length][Hangneigung[0].length];
        //Weg gibt an, wieviele Rasterzellen in Abh?ngigkeit von der Hangneigung geroutet wird;
        for (int i = 0; i < Weg.length; i++) {
            for (int j = 0; j < Weg[0].length; j++) {
                if (Hangneigung[i][j] == -1){
                    Weg[i][j] = -1;
                }else{
                    double alpha = (Hangneigung[i][j] * 2. * Math.PI) / 360.; //Umrechnung Gradmass in Bogenmass
                    Weg[i][j] = (9.9 * Math.sin(alpha) * kalibrierung * zeitintervall) / (cellsize);
                }
            }
        }
        return Weg;
    }

    int externalGridCells = 0;
    private Point[][] calculateFlowPath(double[][] dgm, Point[][] routing, int actx, int acty, double zahl, int[][] fliessrichtung, double[][] weg, int[][] flgew,int startx, int starty, double aufloesung) {
        Point p = new Point();
        
        if (dgm[actx][acty] == -1){
            //außerhalb
            p.x = startx;
            p.y = starty;
            routing[startx][starty] = p;
            externalGridCells++;            
        }
        
        if (flgew[actx][acty] == -1 && //no routing in reach!!
            (zahl < 1 || (actx == startx && acty == starty) )&& //zeit
            actx > 0 && actx < fliessrichtung.length - 1 && acty > 0 && acty < fliessrichtung[0].length - 1) {
            
            zahl = zahl + (1. / weg[actx][acty]);//Ist ein Zeitschritt schon vorbei?
            if (fliessrichtung[actx][acty] == 1) {
                routing = calculateFlowPath(dgm, routing, actx + 1, acty, zahl, fliessrichtung, weg, flgew, startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 2) {
                routing = calculateFlowPath(dgm, routing, actx + 1, acty + 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 4) {
                routing = calculateFlowPath(dgm, routing, actx, acty + 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 8) {
                routing = calculateFlowPath(dgm, routing, actx - 1, acty + 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 16) {
                routing = calculateFlowPath(dgm, routing, actx - 1, acty, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 32) {
                routing = calculateFlowPath(dgm, routing, actx - 1, acty - 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 64) {
                routing = calculateFlowPath(dgm, routing, actx, acty - 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 128) {
                routing = calculateFlowPath(dgm, routing, actx + 1, acty - 1, zahl, fliessrichtung, weg, flgew,startx, starty, aufloesung);
            }
            if (fliessrichtung[actx][acty] == 0) //Pegel; hier wird nicht weitergeroutet!
            {
                p.x = actx;
                p.y = acty;
                routing[startx][starty] = p;
            }
        } else {
            //Weist zu, in welche Rasterzelle geroutet wird
            p.x = actx;
            p.y = acty;
            routing[startx][starty] = p;
        }


        return routing;

    }

    private Point[][] calculateFlowPath1(double[][] dgm, int[][] fliessrichtung, double[][] weg, int[][] flgew, double aufloesung) {
        Point[][] routing = new Point[dgm.length][dgm[0].length];
        for (int i = 1; i < weg.length - 1; i++) {
            for (int j = 1; j < weg[0].length - 1; j++) {
                if (dgm[i][j] != -1) {
                    routing = calculateFlowPath(dgm, routing, i, j, 0, fliessrichtung, weg, flgew, i, j, aufloesung);
                }
            }
        }
        return routing;
    }
    
    private int[][] mapRasterToPolygon(int raster[][], PolygonRasterMap map, int zeile, int spalte, int type) {
        //Die Variable "hilf" ist daf?r zust?ndig zu bestimmen, ob es die erste Zelle der Liste ist. Dort wird die HRU_ID angehangen.
        //Type 1 HRU
        //Type 2 Reach 

        if (raster[zeile][spalte] == map.polygonID || raster[zeile][spalte] == MAGIC_NUMBER) {
            map.add(new Point(zeile, spalte));
            raster[zeile][spalte] = -1;
            // Suchrichtungen
            //       1
            //    2     3
            //       4
            //
            if (zeile > 1 && spalte > 1 && zeile < raster.length - 1 && spalte < raster[0].length - 1) {
                raster = mapRasterToPolygon(raster, map, zeile - 1, spalte, type);
                raster = mapRasterToPolygon(raster, map, zeile, spalte - 1, type);
                raster = mapRasterToPolygon(raster, map, zeile, spalte + 1, type);
                raster = mapRasterToPolygon(raster, map, zeile + 1, spalte, type);
                
                if (type == 2) {
                    raster = mapRasterToPolygon(raster, map, zeile - 1, spalte - 1, type);
                    raster = mapRasterToPolygon(raster, map, zeile + 1, spalte - 1, type);
                    raster = mapRasterToPolygon(raster, map, zeile - 1, spalte + 1, type);
                    raster = mapRasterToPolygon(raster, map, zeile + 1, spalte + 1, type);
                }
            }
        }
        return raster;
    }

    private int[][] quickSort(int liste[][], int untereGrenze, int obereGrenze) {
        int links = untereGrenze;
        int rechts = obereGrenze;
        int pivot = liste[0][((untereGrenze + obereGrenze) / 2)];
        do {
            while (liste[0][links] < pivot) {
                links++;
            }
            while (pivot < liste[0][rechts]) {
                rechts--;
            }
            if (links <= rechts) {
                //Elemente tauschen  
                int tmp = liste[0][links];
                liste[0][links] = liste[0][rechts];
                liste[0][rechts] = tmp;
                tmp = liste[1][links];
                liste[1][links] = liste[1][rechts];
                liste[1][rechts] = tmp;
                tmp = liste[2][links];
                liste[2][links] = liste[2][rechts];
                liste[2][rechts] = tmp;
                links++;
                rechts--;
            }
        } while (links <= rechts);
        if (untereGrenze < rechts) {
            liste = quickSort(liste, untereGrenze, rechts);
        }
        if (links < obereGrenze) {
            liste = quickSort(liste, links, obereGrenze);
        }
        return liste;
    }
    
    private int[][] sortDGM(int[][] dgm_sort, double[][] dgm) {
        int i, j;
        int count = 0;
        //warum abschneiden der kanten???
        for (i = 1; i < dgm.length - 1; i++) {
            for (j = 1; j < dgm[0].length - 1; j++) {
                if (dgm[i][j] != -1) {
                    dgm_sort[0][count] = (int) (dgm[i][j]);
                    dgm_sort[1][count] = i;
                    dgm_sort[2][count] = j;
                    count++;
                }
            }
        }

        //H?henwerte mit dem Quicksortalgorithmus sortieren
        dgm_sort = quickSort(dgm_sort, 0, count - 1);
        return dgm_sort;
    }

    public void runInit() {
        //Einlesen der relevanten Informationen als ASCII-Datensaetze
        int rastersize = (int)getRasterSize(new File(this.getModel().getWorkspaceDirectory(), dgmFile.getValue()));
        double[][] hruraster_start = readRasterFile(new File(this.getModel().getWorkspaceDirectory(), hruRasterFile.getValue()));
        double[][] dgm_start = readRasterFile(new File(this.getModel().getWorkspaceDirectory(), dgmFile.getValue()));
        double[][] flgew_start = readRasterFile(new File(this.getModel().getWorkspaceDirectory(), flgewFile.getValue()));

        double aufloesung = gridresolution.getValue();
        double zeitschritt = timescale.getValue();

        if (zeitschritt == 1) {
            zeitschritt = 60 * 60 * 24;
        } //in seconds
        if (zeitschritt == 2) {
            zeitschritt = 60 * 60;
        }    //in seconds

        rastersize = (int) (rastersize * aufloesung);

        int orgDGMSize[] = {dgm_start.length, dgm_start[0].length};
        int DGMSize[] = {(int) (orgDGMSize[0] / aufloesung), (int)(orgDGMSize[1] / aufloesung)};
        
        double[][] dgm = new double[DGMSize[0]][DGMSize[1]];
        int hruraster[][] = new int[DGMSize[0]][DGMSize[1]];

        for (int i = 0; i < DGMSize[0]; i++) {
            for (int j = 0; j < DGMSize[1]; j++) {
                dgm[(int) (i / aufloesung)][(int) (j / aufloesung)] = dgm[(int) (i / aufloesung)][(int) (j / aufloesung)] + dgm_start[i][j];
            }
        }

        int[][] flgew = new int[DGMSize[0]][DGMSize[1]];
        int[][] flgew1 = new int[DGMSize[0]][DGMSize[1]];
        int[][] flgew2 = new int[DGMSize[0]][DGMSize[1]];
        for (int i = 0; i < DGMSize[0]; i++) {
            for (int j = 0; j < DGMSize[1]; j++) {
                flgew[i][j] = -1;
            }
        }

        for (int i = 0; i < DGMSize[0]; i++) {
            for (int j = 0; j < DGMSize[1]; j++) {
                for (int k = 0; k < aufloesung; k++) {
                    for (int l = 0; l < aufloesung; l++) {
                        if (flgew_start[(int) (i * aufloesung + k)][(int) (j * aufloesung + l)] != -1) {
                            //Pegel soll immer erhalten bleiben
                            if (flgew_start[(int) (i * aufloesung + k)][(int) (j * aufloesung + l)] == MAGIC_NUMBER) {
                                flgew[i][j] = MAGIC_NUMBER;
                                k = (int) aufloesung + 1;
                                l = (int) aufloesung + 1;
                            } else {
                                flgew[i][j] = (int)flgew_start[(int) (i * aufloesung + k)][(int) (j * aufloesung + l)];
                            }
                        }
                    }
                }
                flgew1[i][j] = flgew[i][j];
                flgew2[i][j] = flgew[i][j];
            }
        }


        for (int i = 0; i < DGMSize[0]; i++) {
            for (int j = 0; j < DGMSize[1]; j++) {
                dgm[i][j] = dgm[i][j] / (aufloesung * aufloesung);
                if ((dgm[i][j] > -1) && (dgm[i][j] < 0.1)) {
                    dgm[i][j] = -1;
                }

                if ((i * aufloesung < hruraster_start.length) && (j * aufloesung < hruraster_start[0].length)) {
                    hruraster[i][j] = (int)hruraster_start[(int) (i * aufloesung)][(int) (j * aufloesung)];
                }else{
                    //TODO??
                }
            }
        }

        int[][] fliessrichtung = calculateFlowDirection(dgm);

        //Bestimmen der Abflussrichtung entlang des Fliessgewaessers 
        //Aufbau einer Datenstruktur zur Verwaltung der Fliessgewaesserabschnitte
        HashMap<Integer, PolygonRasterMap> reachList = new HashMap<Integer, PolygonRasterMap>();
        
        int count = 0;
        int zahl = 1;
        while (zahl != 0) //pruefe, ob es Reaches gibt, die noch nicht verarbeitet worden sind
        {
            zahl = 0;

            for (int i = 0; i < DGMSize[0]; i++) {
                for (int j = 0; j < DGMSize[1]; j++) {
                    //schauen, ob es sich um ein reach-Start-Pixel handelt
                    if (flgew2[i][j] != -1 && flgew2[i][j] != MAGIC_NUMBER) {
                        zahl=1;
                        PolygonRasterMap rasterReachMap = new PolygonRasterMap((int)flgew2[i][j]);
                        //raster_in_reach = new ArrayList<Point>(); //Neue Liste erzeugen
                        count = 0;
                        //zaehlt, wieviele Nachbarpixel im flgew liegen um das Startpixel zu ermitteln
                        if (flgew2[i + 1][j] == -1) {
                            count++;
                        }
                        if (flgew2[i - 1][j] == -1) {
                            count++;
                        }
                        if (flgew2[i][j + 1] == -1) {
                            count++;
                        }
                        if (flgew2[i][j - 1] == -1) {
                            count++;
                        }
                        if (flgew2[i + 1][j + 1] == -1) {
                            count++;
                        }
                        if (flgew2[i + 1][j - 1] == -1) {
                            count++;
                        }
                        if (flgew2[i - 1][j + 1] == -1) {
                            count++;
                        }
                        if (flgew2[i - 1][j - 1] == -1) {
                            count++;
                        }
                        if (count == 7) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i + 1][j + 1] != -1 && flgew2[i][j + 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i + 1][j + 1] != -1 && flgew2[i + 1][j] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i + 1][j] != -1 && flgew2[i + 1][j - 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i + 1][j - 1] != -1 && flgew2[i][j - 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i - 1][j - 1] != -1 && flgew2[i][j - 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i - 1][j] != -1 && flgew2[i - 1][j - 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i - 1][j + 1] != -1 && flgew2[i - 1][j] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                        if (count == 6 && flgew2[i - 1][j + 1] != -1 && flgew2[i][j + 1] != -1) {
                            flgew2 = mapRasterToPolygon(flgew2, rasterReachMap, i, j, 2);
                            zahl++;
                            reachList.put(rasterReachMap.polygonID, rasterReachMap);
                        }
                    }
                }
            }
        }


        fliessrichtung = flgew(dgm, fliessrichtung, flgew, flgew1, hruraster);

        for (int i = 0; i < fliessrichtung.length; i++) {
            for (int j = 0; j < fliessrichtung[0].length; j++) {
                if (flgew1[i][j] != -1) {
                    flgew1[i][j] = fliessrichtung[i][j];
                }
            }
        }

        //gehlbergspezifisch

        //Flie?richtungen am Pegel anpassen
        for (int i = 0; i < flgew.length; i++) {
            for (int j = 0; j < flgew[0].length; j++) {
                if (flgew[i][j] == MAGIC_NUMBER) {
                    flgew1[i][j] = 0;
                    //don't forget to add this point!!
                    //it would be better to use the last one in cascade
                    reachList.values().iterator().next().add(new Point(i,j));
                    
                    //ich glaube das heißt, dass alles zum pegel fließt .. 
                    flgew1[i - 1][j - 1] = 2;
                    flgew1[i - 1][j] = 1;
                    flgew1[i - 1][j + 1] = 128;
                    flgew1[i][j - 1] = 4;
                    flgew1[i][j + 1] = 64;
                    flgew1[i + 1][j - 1] = 8;
                    flgew1[i + 1][j] = 16;
                    flgew1[i + 1][j + 1] = 32;
                    fliessrichtung[i][j] = 0;
                    fliessrichtung[i - 1][j - 1] = 2;
                    fliessrichtung[i - 1][j] = 1;
                    fliessrichtung[i - 1][j + 1] = 128;
                    fliessrichtung[i][j - 1] = 4;
                    fliessrichtung[i][j + 1] = 64;
                    fliessrichtung[i + 1][j - 1] = 8;
                    fliessrichtung[i + 1][j] = 16;
                    fliessrichtung[i + 1][j + 1] = 32;
                    i = flgew.length;
                    j = flgew[0].length;
                }

            }
        }

        //  Ausgabe(flgew1,"C:/Arbeit/flgew1");
        //  Ausgabe(flgew2,"C:/Arbeit/flgew2");
        //  Ausgabe(fliessrichtung,"C:/Arbeit/fliessrichtung");
        //  Ausgabe(dgm,"C:/Arbeit/dgm");
        
        //Hangneigung berechnen         
        double[][] hangneigung = calcSlope(dgm, fliessrichtung, (int)rastersize);


        //Anlegen einer Datenstruktur die das spaetere Mapping zwischen verschiedenen 
        //raumlichen Diskretisierungen ermoeglichen soll. 

        HashMap<Integer, PolygonRasterMap> hruList = new HashMap<Integer, PolygonRasterMap>();
        
        //Aufbau der Datenstruktur  
        count = 0;
        for (int i = 0; i < hruraster.length; i++) {
            for (int j = 0; j < hruraster[0].length; j++) {
                if (hruraster[i][j] != -1) {
                    PolygonRasterMap rasterToHRUMap = new PolygonRasterMap(hruraster[i][j]); //Neue Liste erzeugen
                    hruraster = mapRasterToPolygon(hruraster, rasterToHRUMap, i, j, 1);
                    hruList.put(rasterToHRUMap.polygonID,rasterToHRUMap);
                    count++;
                    //Zaehlt nur HRUs die groesser als zwei pixel sind. Eleminierung aus der Liste???
                }
            }
        }

        //DGM Sortieren, damit Rasterzellen von der niedrigsten zur hoechsten abgearbeitet werden koennen
        int dgm_sort[][] = new int[3][dgm.length * dgm[0].length]; //Array, indem die H?henwerte sortiert gespeichert werden
        dgm_sort = sortDGM(dgm_sort, dgm); //Sortieren der dgm-Werte nach Ihrer H?he

        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
                       
        double[][] Fuellstand_Reaches = new double[dgm.length][dgm[0].length]; //Gedaechtnis des Wasserstandes
        double[][] Qin_Reaches = new double[dgm.length][dgm[0].length]; //Gedaechtnis der Uebergaenge von Zeitschritt t zu t+1

        double RD1_Koeff = RD1_Weg_Koeff.getValue();
        double RD2_Koeff = RD2_Weg_Koeff.getValue();
        double RG1_Koeff = RG1_Weg_Koeff.getValue();
        double RG2_Koeff = RG2_Weg_Koeff.getValue();

        double[][] slopeLength;
        
        slopeLength = calculateSlopeLength(hangneigung, RD1_Koeff, zeitschritt, rastersize);        
        Point[][] RoutingRD1 = calculateFlowPath1(dgm, fliessrichtung, slopeLength, flgew, aufloesung);
        slopeLength = calculateSlopeLength(hangneigung, RD2_Koeff, zeitschritt, rastersize);
        Point[][] RoutingRD2 = calculateFlowPath1(dgm, fliessrichtung, slopeLength, flgew,aufloesung);
        slopeLength = calculateSlopeLength(hangneigung, RG1_Koeff, zeitschritt, rastersize);
        Point[][] RoutingRG1 = calculateFlowPath1(dgm, fliessrichtung, slopeLength, flgew,aufloesung);
        slopeLength = calculateSlopeLength(hangneigung, RG2_Koeff, zeitschritt, rastersize);
        Point[][] RoutingRG2 = calculateFlowPath1(dgm, fliessrichtung, slopeLength, flgew,aufloesung);

        Ausgabe(RoutingRD1,"E:/ModelData/flgewRD1.txt");
        Ausgabe(RoutingRD2,"E:/ModelData/flgewRD2.txt");
        Ausgabe(RoutingRG1,"E:/ModelData/flgewRG1.txt");
        Ausgabe(RoutingRG2,"E:/ModelData/flgewRG2.txt");
        //Anlegen der Raster, die sich die Wasserverteil?ng im Gebiet merken
        double[][] RD1_vraster = new double[hruraster.length][hruraster[0].length];
        double[][] RD2_vraster = new double[hruraster.length][hruraster[0].length];
        double[][] RG1_vraster = new double[hruraster.length][hruraster[0].length];
        double[][] RG2_vraster = new double[hruraster.length][hruraster[0].length];
        double[][] reachinfo = new double[4][reachList.size() + 100];//falls welche durch das zusammenfassen wegfallen

        information.setObject("hruraster", hruraster);
        information.setObject("fliessrichtung", fliessrichtung);
        information.setObject("dgm", dgm);
        information.setObject("flgew", flgew);
        information.setObject("flgew1", flgew1);//flie?richtung innerhalb des flie?gew?ssers
        information.setObject("routingRD1", RoutingRD1);
        information.setObject("routingRD2", RoutingRD2);
        information.setObject("routingRG1", RoutingRG1);
        information.setObject("routingRG2", RoutingRG2);
        information.setObject("hangneigung", hangneigung);
        information.setObject("dgm_sort", dgm_sort);
        information.setObject("hrulist", hruList);
        information.setObject("reachlist", reachList);
        information.setObject("RD1_vraster", RD1_vraster);
        information.setObject("RD2_vraster", RD2_vraster);
        information.setObject("RG1_vraster", RG1_vraster);
        information.setObject("RG2_vraster", RG2_vraster);
        information.setObject("reachinfo", reachinfo);

        double[][] RD1_raster = new double[hruraster.length][hruraster[0].length];
        double[][] RD1_reach = new double[reachList.size()][2];
        double[][] RD2_raster = new double[hruraster.length][hruraster[0].length];
        double[][] RD2_reach = new double[reachList.size()][2];
        double[][] RG1_raster = new double[hruraster.length][hruraster[0].length];
        double[][] RG1_reach = new double[reachList.size()][2];
        double[][] RG2_raster = new double[hruraster.length][hruraster[0].length];
        double[][] RG2_reach = new double[reachList.size()][2];

        fuellstand.setObject("RD1_raster", RD1_raster);
        //fuellstand.setObject("RD1_hru", RD1_hru);
        fuellstand.setObject("RD2_raster", RD2_raster);
        //fuellstand.setObject("RD2_hru", RD2_hru);
        fuellstand.setObject("RG1_raster", RG1_raster);
        //fuellstand.setObject("RG1_hru", RG1_hru);
        fuellstand.setObject("RG2_raster", RG2_raster);
        //fuellstand.setObject("RG2_hru", RG2_hru);
        fuellstand.setObject("RD1_reach", RD1_reach);
        fuellstand.setObject("RD2_reach", RD2_reach);
        fuellstand.setObject("RG1_reach", RG1_reach);
        fuellstand.setObject("RG2_reach", RG2_reach);
        fuellstand.setObject("fuellstand_reaches", Fuellstand_Reaches);
        fuellstand.setObject("Qin_reaches", Qin_Reaches);
        
        System.out.println("External Points: " + externalGridCells);
    }

    @Override
    public void run() {
        runInit();
    }

    @Override
    public void cleanup() {
    }
}