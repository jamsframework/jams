/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.tools;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Program for calculating the USLE K-Factor with the help of sand, silt and clay content and organinc carbon (to be included Ks-Classes and Aggregate Classes) 
 *
 *
 * @author c8fima
 */
public class K_factor_USLE {

    String[] Header;
    String[][] soils;
    String[][] ka5tex;
    BufferedWriter writer;
    Double ffsandprop = 1.0/9.0;

    public void convert(String inputFileName, String outputFileName) {

        int j = 0;
        BufferedReader reader;
        BufferedReader reader1;

        String line;
        String lineka5;
        int f = 0;
        int ncolka = 0;
        StringTokenizer tokka5;
        String value;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            
            writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write("SID" + "\t" + "A_K_factor" + "\t" + "B_K_factor" + "\t" + "C_K_factor" + "\t" + "D_K_factor" + "\t" + "E_K_factor");
            writer.newLine();

          

            StringTokenizer tok;
            line = reader.readLine();
            tok = new StringTokenizer(line);
            int ncol = tok.countTokens();
            Header = new String[ncol];
            soils = new String[ncol][21];
            int i = 0;

            while (j < 21) {

                if (j == 0) {
                    while (i < ncol) {
                        value = tok.nextToken();
                        Header[i] = value;
                        i++;
                    }

                } else {
                    i = 0;
                    line = reader.readLine();
                    tok = new StringTokenizer(line);
                    while (i < ncol) {
                        value = tok.nextToken();
                        soils[i][j] = value;

                        i++;

                    }

                }

                j++;
            }

            line = readline(ncol, true, ka5tex, j);

            writer.close();
            reader.close();

        } catch (IOException e) {
            System.out.println("io fehler");
            e.printStackTrace();
        }

    }

    private String readline(int ncol, boolean firstline, String[][] ka5pf, int j) throws IOException {

        String result, sid, A_K_factor, B_K_factor, C_K_factor, D_K_factor, E_K_factor, asand, bsand, csand, dsand, esand, asilt, bsilt, csilt, dsilt, esilt, aclay, bclay, cclay, dclay, eclay, acorg, bcorg, ccorg, dcorg, ecorg;

        double asandd = 0;
        double bsandd = 0;
        double csandd = 0;
        double dsandd = 0;
        double esandd = 0;
        double asiltd = 0;
        double bsiltd = 0;
        double csiltd = 0;
        double dsiltd = 0;
        double esiltd = 0;
        double aclayd = 0;
        double bclayd = 0;
        double cclayd = 0;
        double dclayd = 0;
        double eclayd = 0;
        double acorgd = 0;
        double bcorgd = 0;
        double ccorgd = 0;
        double dcorgd = 0;
        double ecorgd = 0;
        sid = "";
        result = "";
        int x = 1;


        while (x < 21) {
            A_K_factor = "";
            B_K_factor = "";
            C_K_factor = "";
            D_K_factor = "";
            E_K_factor = "";
            if (x > 0) {
                for (int y = 0; y < ncol; y++) {
                    if ("SoilId".equals(Header[y])) {
                        sid = soils[y][x];
                    }

                    if ("ASAND".equals(Header[y])) {
                        asand = soils[y][x];
                        asandd = Double.parseDouble(asand);
                    }
                    if ("BSAND".equals(Header[y])) {
                        bsand = soils[y][x];
                        bsandd = Double.parseDouble(bsand);
                    }
                    if ("CSAND".equals(Header[y])) {
                        csand = soils[y][x];
                        csandd = Double.parseDouble(csand);
                    }
                    if ("DSAND".equals(Header[y])) {
                        dsand = soils[y][x];
                        dsandd = Double.parseDouble(dsand);
                    }
                    if ("ESAND".equals(Header[y])) {
                        esand = soils[y][x];
                        esandd = Double.parseDouble(esand);
                    }
                    if ("ASCHLUFF".equals(Header[y])) {
                        asilt = soils[y][x];
                        asiltd = Double.parseDouble(asilt);
                    }
                    if ("BSCHLUFF".equals(Header[y])) {
                        bsilt = soils[y][x];
                        bsiltd = Double.parseDouble(bsilt);
                    }
                    if ("CSCHLUFF".equals(Header[y])) {
                        csilt = soils[y][x];
                        csiltd = Double.parseDouble(csilt);
                    }
                    if ("DSCHLUFF".equals(Header[y])) {
                        dsilt = soils[y][x];
                        dsiltd = Double.parseDouble(dsilt);
                    }
                    if ("ESCHLUFF".equals(Header[y])) {
                        esilt = soils[y][x];
                        esiltd = Double.parseDouble(esilt);
                    }
                    if ("ATON".equals(Header[y])) {
                        aclay = soils[y][x];
                        aclayd = Double.parseDouble(aclay);
                    }
                    if ("BTON".equals(Header[y])) {
                        bclay = soils[y][x];
                        bclayd = Double.parseDouble(bclay);
                    }
                    if ("CTON".equals(Header[y])) {
                        cclay = soils[y][x];
                        cclayd = Double.parseDouble(cclay);
                    }
                    if ("DTON".equals(Header[y])) {
                        dclay = soils[y][x];
                        dclayd = Double.parseDouble(dclay);
                    }
                    if ("ETON".equals(Header[y])) {
                        eclay = soils[y][x];
                        eclayd = Double.parseDouble(eclay);
                    }
                    if ("ACORG".equals(Header[y])) {
                        acorg = soils[y][x];
                        acorgd = Double.parseDouble(acorg);
                    }
                    if ("BCORG".equals(Header[y])) {
                        bcorg = soils[y][x];
                        bcorgd = Double.parseDouble(bcorg);
                    }
                    if ("CCORG".equals(Header[y])) {
                        ccorg = soils[y][x];
                        ccorgd = Double.parseDouble(ccorg);
                    }
                    if ("DCORG".equals(Header[y])) {
                        dcorg = soils[y][x];
                        dclayd = Double.parseDouble(dcorg);
                    }
                    if ("ECORG".equals(Header[y])) {
                        ecorg = soils[y][x];
                        ecorgd = Double.parseDouble(ecorg);
                    }
                    
                   // after Schwertmann 1987 
                   
                   Double Am = (asiltd + (asandd*ffsandprop))*(asiltd + asandd);                    
                   double A_K_Factord =  0.00000277 * Math.pow(Am, 1.14) * (12 - acorgd); // add aggregate and conductivity information (* (AA - 2) * (4 - AKFclass)
                   A_K_factor = String.valueOf(A_K_Factord);
                   
                   Double Bm = (bsiltd + (bsandd*ffsandprop))*(bsiltd + bsandd);                    
                   double B_K_Factord =  0.00000277 * Math.pow(Bm, 1.14) * (12 - bcorgd); // add aggregate and conductivity information (* (AA - 2) * (4 - AKFclass)
                   B_K_factor = String.valueOf(B_K_Factord);
                   
                   Double Cm = (csiltd + (csandd*ffsandprop))*(csiltd + csandd);                    
                   double C_K_Factord =  0.00000277 * Math.pow(Cm, 1.14) * (12 - ccorgd); // add aggregate and conductivity information (* (AA - 2) * (4 - AKFclass)
                   C_K_factor = String.valueOf(C_K_Factord);
                   
                   Double Dm = (dsiltd + (dsandd*ffsandprop))*(dsiltd + dsandd);                    
                   double D_K_Factord =  0.00000277 * Math.pow(Dm, 1.14) * (12 - dcorgd); // add aggregate and conductivity information (* (AA - 2) * (4 - AKFclass)
                   D_K_factor = String.valueOf(D_K_Factord);
                   
                   Double Em = (esiltd + (esandd*ffsandprop))*(esiltd + esandd);                    
                   double E_K_Factord =  0.00000277 * Math.pow(Em, 1.14) * (12 - ecorgd); // add aggregate and conductivity information (* (AA - 2) * (4 - AKFclass)
                   E_K_factor = String.valueOf(E_K_Factord);

                }

            }

            result = sid + "\t" + A_K_factor + "\t" + B_K_factor + "\t" + C_K_factor + "\t" + D_K_factor + "\t" + E_K_factor;

         
            writer.write(result);
            writer.newLine();
            x++;
        }

        return result;

    }

    public static void main(String[] args) {
        K_factor_USLE Tex = new K_factor_USLE();

        Tex.convert("D:/LUCCI/gisdata/Soil/soil_bearbeitet_neu.TXT", "D:/LUCCI/gisdata/Soil/K_Factor.TXT");
    }
}