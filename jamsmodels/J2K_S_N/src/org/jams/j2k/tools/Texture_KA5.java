/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.tools;

import org.jams.j2k.s_n.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Program for classification of soil texture to soil texture classes after KA5
 * using sand, silt and clay conten in % 
 *
 *
 * @author c8fima
 */
public class Texture_KA5 {

    String[] Header;
    String[][] soils;
    String[][] ka5tex;
    BufferedWriter writer;

    public void convert(String inputFileName, String outputFileName) {

        int j = 0;
        BufferedReader reader;
        BufferedReader reader1;

        String line;
        String lineka5;
        String inputFileName1 = "D:/LUCCI/gisdata/Soil/TextureKA5.txt";
        int f = 0;
        int ncolka = 0;
        StringTokenizer tokka5;
        String value;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            reader1 = new BufferedReader(new FileReader(inputFileName1));
            writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write("SID" + "\t" + "Asubs" + "\t" + "Bsubs" + "\t" + "Csubs" + "\t" + "Dsubs" + "\t" + "Esubs");
            writer.newLine();

            lineka5 = reader1.readLine();
            tokka5 = new StringTokenizer(lineka5);
            ncolka = tokka5.countTokens();
            String[] Headerka = new String[ncolka];
            ka5tex = new String[ncolka][32];


            while (f < 32) {
                int e = 0;
                if (f == 0) {

                    while (e < ncolka) {
                        value = tokka5.nextToken();
                        Headerka[e] = value;
                        e++;
                    }
                } else {
                    lineka5 = reader1.readLine();
                    tokka5 = new StringTokenizer(lineka5);


                    while (e < ncolka) {
                        value = tokka5.nextToken();
                        ka5tex[e][f - 1] = value;
                        e++;
                    }
                }
                f++;
            }

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

            line = readline(ncol, true, Headerka, ka5tex, j);

            writer.close();
            reader.close();

        } catch (IOException e) {
            System.out.println("io fehler");
            e.printStackTrace();
        }

    }

    private String readline(int ncol, boolean firstline, String[] Headerka, String[][] ka5pf, int j) throws IOException {

        String result, sid, asubs, bsubs, csubs, dsubs, esubs, asand, bsand, csand, dsand, esand, asilt, bsilt, csilt, dsilt, esilt, aclay, bclay, cclay, dclay, eclay;



        double claymin[] = new double[32];
        double claymax[] = new double[32];
        double siltmin[] = new double[32];
        double siltmax[] = new double[32];
        double sandmin[] = new double[32];
        double sandmax[] = new double[32];



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
        sid = "";
        asubs = "";
        bsubs = "";
        csubs = "";
        dsubs = "";
        esubs = "";
        result = "";
        int x = 1;


        while (x < 21) {
            asubs = "";
            bsubs = "";
            csubs = "";
            dsubs = "";
            esubs = "";
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

                }



            }

            int i = 0;




            while (i < 31) {
                j = 1;
                while (j < Headerka.length) {

                    if ("Claymin".equals(Headerka[j])) {
                        claymin[i] = Double.parseDouble(ka5tex[j][i]);
                    } else if ("Claymax".equals(Headerka[j])) {
                        claymax[i] = Double.parseDouble(ka5tex[j][i]);
                    } else if ("Siltmin".equals(Headerka[j])) {
                        siltmin[i] = Double.parseDouble(ka5tex[j][i]);
                    } else if ("Siltmax".equals(Headerka[j])) {
                        siltmax[i] = Double.parseDouble(ka5tex[j][i]);
                    } else if ("Sandmin".equals(Headerka[j])) {
                        sandmin[i] = Double.parseDouble(ka5tex[j][i]);
                    } else if ("Sandmax".equals(Headerka[j])) {
                        sandmax[i] = Double.parseDouble(ka5tex[j][i]);
                    }
                    j++;
                }
                i++;
            }
            i = 0;
            while (i < 32) {

                if (((asandd >= sandmin[i]) && (asandd < sandmax[i])) && ((asiltd >= siltmin[i]) && (asiltd < siltmax[i])) && ((aclayd >= claymin[i]) && (aclayd < claymax[i]))) {
                    asubs = ka5tex[0][i];
                }

                if (((bsandd >= sandmin[i]) && (bsandd < sandmax[i])) && ((bsiltd >= siltmin[i]) && (bsiltd < siltmax[i])) && ((bclayd >= claymin[i]) && (bclayd < claymax[i]))) {
                    bsubs = ka5tex[0][i];
                }

                if (((csandd >= sandmin[i]) && (csandd < sandmax[i])) && ((csiltd >= siltmin[i]) && (csiltd < siltmax[i])) && ((cclayd >= claymin[i]) && (cclayd < claymax[i]))) {
                    csubs = ka5tex[0][i];
                }

                if (((dsandd >= sandmin[i]) && (dsandd < sandmax[i])) && ((dsiltd >= siltmin[i]) && (dsiltd < siltmax[i])) && ((dclayd >= claymin[i]) && (dclayd < claymax[i]))) {
                    dsubs = ka5tex[0][i];
                }

                if (((esandd >= sandmin[i]) && (esandd < sandmax[i])) && ((esiltd >= siltmin[i]) && (esiltd < siltmax[i])) && ((eclayd >= claymin[i]) && (eclayd < claymax[i]))) {
                    esubs = ka5tex[0][i];
                }
                i++;

            }
            result = sid + "\t" + asubs + "\t" + bsubs + "\t" + csubs + "\t" + dsubs + "\t" + esubs;



            
            writer.write(result);
            writer.newLine();
            x++;
        }












        return result;


    }

    public static void main(String[] args) {
        Texture_KA5 Tex = new Texture_KA5();

        Tex.convert("D:/LUCCI/gisdata/Soil/soil_bearbeitet_neu.TXT", "D:/LUCCI/gisdata/Soil/Textureportions.TXT");
    }
}