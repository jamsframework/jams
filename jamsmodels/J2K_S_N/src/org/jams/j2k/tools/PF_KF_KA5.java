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
 * Program for implementation of the pedo transfer function of the KA5 for the pf values using bulk densitty, soil textur classes and organic carbon 
 * 
 *
 * @author c8fima
 */
public class PF_KF_KA5 {

    String[] Header;
    String[][] soils;
    BufferedWriter writer;
    BufferedWriter writer1;

    public void convert(String inputFileName, String outputFileName) {

        int j = 0;
        BufferedReader reader;
        BufferedReader reader1;

        String line;
        String lineka5;
        String inputFileName1 = "D:/LUCCI/gisdata/Soil/KA5PF.txt";
        int f = 0;
        int ncolka = 0;
        StringTokenizer tokka5;
        String value;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            reader1 = new BufferedReader(new FileReader(inputFileName1));
            writer = new BufferedWriter(new FileWriter(outputFileName));
            writer1 = new BufferedWriter(new FileWriter("D:/LUCCI/gisdata/Soil/soils.par"));




            lineka5 = reader1.readLine();
            tokka5 = new StringTokenizer(lineka5);
            ncolka = tokka5.countTokens();
            String[] Headerka = new String[ncolka];
            String[][] ka5pf = new String[ncolka][38];


            while (f < 39) {
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
                        ka5pf[e][f - 1] = value;
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




            line = readline(ncol, true, Headerka, ka5pf, j);





            writer1.close();
            writer.close();
            reader.close();



        } catch (IOException e) {
            System.out.println("io fehler");
            e.printStackTrace();
        }

    }

    private String readline(int ncol, boolean firstline, String[] Headerka, String[][] ka5pf, int j) throws IOException {

        String result, asubs, bsubs, csubs, dsubs, esubs, acorg, bcorg, ccorg, dcorg, ecorg, adichte, bdichte, cdichte, ddichte, edichte, atiefe, btiefe, ctiefe, dtiefe, etiefe;



        double acorgd = 0;
        double bcorgd = 0;
        double ccorgd = 0;
        double dcorgd = 0;
        double ecorgd = 0;

        double adichted = 0;
        double bdichted = 0;
        double cdichted = 0;
        double ddichted = 0;
        double edichted = 0;
        float atiefed = 0;
        float btiefed = 0;
        float ctiefed = 0;
        float dtiefed = 0;
        float etiefed = 0;
        double aLPS = 0;
        double bLPS = 0;
        double cLPS = 0;
        double dLPS = 0;
        double eLPS = 0;
        double aMPS = 0;
        double bMPS = 0;
        double cMPS = 0;
        double dMPS = 0;
        double eMPS = 0;
        double aFPS = 0;
        double bFPS = 0;
        double cFPS = 0;
        double dFPS = 0;
        double eFPS = 0;
        asubs = "";
        bsubs = "";
        csubs = "";
        dsubs = "";
        esubs = "";
        result = "";
        int x = 0;
        double deptharray[] = new double[5];
        double MPSarray[] = new double[5]; 
        double LPSarray[] = new double[5]; 
        double FPSarray[] = new double[5];










        while (x < 21) {
            if (x > 0) {
                for (int y = 1; y < ncol; y++) {
                    if ("ASUBS".equals(Header[y])) {
                        asubs = soils[y][x];
                    }
                    if ("BSUBS".equals(Header[y])) {
                        bsubs = soils[y][x];
                    }
                    if ("CSUBS".equals(Header[y])) {
                        csubs = soils[y][x];
                    }
                    if ("DSUBS".equals(Header[y])) {
                        dsubs = soils[y][x];
                    }
                    if ("ESUBS".equals(Header[y])) {
                        esubs = soils[y][x];
                    }
                    if ("ACORG".equals(Header[y])) {
                        acorg = soils[y][x];
                        acorgd = Double.parseDouble(acorg) * 1.72;
                    }
                    if ("BCORG".equals(Header[y])) {
                        bcorg = soils[y][x];
                        bcorgd = Double.parseDouble(bcorg) * 1.72;
                    }
                    if ("CCORG".equals(Header[y])) {
                        ccorg = soils[y][x];
                        ccorgd = Double.parseDouble(ccorg) * 1.72;
                    }
                    if ("DCORG".equals(Header[y])) {
                        dcorg = soils[y][x];
                        dcorgd = Double.parseDouble(dcorg) * 1.72;
                    }
                    if ("ECORG".equals(Header[y])) {
                        ecorg = soils[y][x];
                        ecorgd = Double.parseDouble(ecorg) * 1.72;
                    }
                    if ("ADICHTE".equals(Header[y])) {
                        adichte = soils[y][x];
                        adichted = Double.parseDouble(adichte);
                    }
                    if ("BDICHTE".equals(Header[y])) {
                        bdichte = soils[y][x];
                        bdichted = Double.parseDouble(bdichte);
                    }
                    if ("CDICHTE".equals(Header[y])) {
                        cdichte = soils[y][x];
                        cdichted = Double.parseDouble(cdichte);
                    }
                    if ("DDICHTE".equals(Header[y])) {
                        ddichte = soils[y][x];
                        ddichted = Double.parseDouble(ddichte);
                    }
                    if ("EDICHTE".equals(Header[y])) {
                        edichte = soils[y][x];
                        edichted = Double.parseDouble(edichte);
                    }
                    if ("ATIEFE".equals(Header[y])) {
                        atiefe = soils[y][x];
                        atiefed = Float.parseFloat(atiefe);
                        deptharray[0] = atiefed; 
                    }
                    if ("BTIEFE".equals(Header[y])) {
                        btiefe = soils[y][x];
                        btiefed = Float.parseFloat(btiefe);
                        deptharray[1] = btiefed; 
                    }
                    if ("CTIEFE".equals(Header[y])) {
                        ctiefe = soils[y][x];
                        ctiefed = Float.parseFloat(ctiefe);
                        deptharray[2] = ctiefed; 
                    }
                    if ("DTIEFE".equals(Header[y])) {
                        dtiefe = soils[y][x];
                        dtiefed = Float.parseFloat(dtiefe);
                        deptharray[3] = dtiefed; 
                    }
                    if ("ETIEFE".equals(Header[y])) {
                        etiefe = soils[y][x];
                        etiefed = Float.parseFloat(etiefe);
                        deptharray[4] = etiefed; 
                    }
                }

                int f = 0;

                while (f < 37) {
                    if (asubs.equals("0")){
                      aLPS = 0;
                      aMPS = 0;
                      aFPS = 0; 
                    }
                    if (asubs.equals(ka5pf[0][f])) {
                        if (adichted < 1.2) {
                            aLPS = Double.parseDouble(ka5pf[1][f]);
                            aMPS = Double.parseDouble(ka5pf[6][f]);
                            aFPS = Double.parseDouble(ka5pf[16][f]);
                        } else if (adichted >= 1.2 && adichted < 1.4) {
                            aLPS = Double.parseDouble(ka5pf[2][f]);
                            aMPS = Double.parseDouble(ka5pf[7][f]);
                            aFPS = Double.parseDouble(ka5pf[17][f]);
                        } else if (adichted >= 1.4 && adichted < 1.6) {
                            aLPS = Double.parseDouble(ka5pf[3][f]);
                            aMPS = Double.parseDouble(ka5pf[8][f]);
                            aFPS = Double.parseDouble(ka5pf[18][f]);
                        } else if (adichted >= 1.6 && adichted < 1.8) {
                            aLPS = Double.parseDouble(ka5pf[4][f]);
                            aMPS = Double.parseDouble(ka5pf[9][f]);
                            aFPS = Double.parseDouble(ka5pf[19][f]);
                        } else if (adichted >= 1.8) {
                            aLPS = Double.parseDouble(ka5pf[5][f]);
                            aMPS = Double.parseDouble(ka5pf[10][f]);
                            aFPS = Double.parseDouble(ka5pf[20][f]);
                        }
                        if (acorgd >= 1 && acorgd < 2) {
                            aLPS = aLPS + Double.parseDouble(ka5pf[21][f]);
                            aMPS = aMPS + Double.parseDouble(ka5pf[25][f]);
                            aFPS = aFPS + (Double.parseDouble(ka5pf[29][f]) - Double.parseDouble(ka5pf[25][f]));
                        } else if (acorgd >= 2 && acorgd < 4) {
                            aLPS = aLPS + Double.parseDouble(ka5pf[22][f]);
                            aMPS = aMPS + Double.parseDouble(ka5pf[26][f]);
                            aFPS = aFPS + (Double.parseDouble(ka5pf[30][f]) - Double.parseDouble(ka5pf[26][f]));
                        } else if (acorgd >= 4 && acorgd < 8) {
                            aLPS = aLPS + Double.parseDouble(ka5pf[23][f]);
                            aMPS = aMPS + Double.parseDouble(ka5pf[27][f]);
                            aFPS = aFPS + (Double.parseDouble(ka5pf[31][f]) - Double.parseDouble(ka5pf[27][f]));
                        } else if (acorgd >= 8) {
                            aLPS = aLPS + Double.parseDouble(ka5pf[24][f]);
                            aMPS = aMPS + Double.parseDouble(ka5pf[28][f]);
                            aFPS = aFPS + (Double.parseDouble(ka5pf[32][f]) - Double.parseDouble(ka5pf[28][f]));
                        }
                    }
                    if (bsubs.equals("0")){
                      bLPS = 0;
                      bMPS = 0;
                      bFPS = 0; 
                    }
                    if (bsubs.equals(ka5pf[0][f])) {
                        if (bdichted < 1.2) {
                            bLPS = Double.parseDouble(ka5pf[1][f]);
                            bMPS = Double.parseDouble(ka5pf[6][f]);
                            bFPS = Double.parseDouble(ka5pf[16][f]);
                        } else if (bdichted >= 1.2 && bdichted < 1.4) {
                            bLPS = Double.parseDouble(ka5pf[2][f]);
                            bMPS = Double.parseDouble(ka5pf[7][f]);
                            bFPS = Double.parseDouble(ka5pf[17][f]);
                        } else if (bdichted >= 1.4 && bdichted < 1.6) {
                            bLPS = Double.parseDouble(ka5pf[3][f]);
                            bMPS = Double.parseDouble(ka5pf[8][f]);
                            bFPS = Double.parseDouble(ka5pf[18][f]);
                        } else if (bdichted >= 1.6 && bdichted < 1.8) {
                            bLPS = Double.parseDouble(ka5pf[4][f]);
                            bMPS = Double.parseDouble(ka5pf[9][f]);
                            bFPS = Double.parseDouble(ka5pf[19][f]);
                        } else if (bdichted >= 1.8) {
                            bLPS = Double.parseDouble(ka5pf[5][f]);
                            bMPS = Double.parseDouble(ka5pf[10][f]);
                            bFPS = Double.parseDouble(ka5pf[20][f]);
                        }
                        if (bcorgd >= 1 && bcorgd < 2) {
                            bLPS = bLPS + Double.parseDouble(ka5pf[21][f]);
                            bMPS = bMPS + Double.parseDouble(ka5pf[25][f]);
                            bFPS = bFPS + (Double.parseDouble(ka5pf[29][f]) - Double.parseDouble(ka5pf[25][f]));
                        } else if (bcorgd >= 2 && bcorgd < 4) {
                            bLPS = bLPS + Double.parseDouble(ka5pf[22][f]);
                            bMPS = bMPS + Double.parseDouble(ka5pf[26][f]);
                            bFPS = bFPS + (Double.parseDouble(ka5pf[30][f]) - Double.parseDouble(ka5pf[26][f]));
                        } else if (bcorgd >= 4 && bcorgd < 8) {
                            bLPS = bLPS + Double.parseDouble(ka5pf[23][f]);
                            bMPS = bMPS + Double.parseDouble(ka5pf[27][f]);
                            bFPS = bFPS + (Double.parseDouble(ka5pf[31][f]) - Double.parseDouble(ka5pf[27][f]));
                        } else if (bcorgd >= 8) {
                            bLPS = bLPS + Double.parseDouble(ka5pf[24][f]);
                            bMPS = bMPS + Double.parseDouble(ka5pf[28][f]);
                            bFPS = bFPS + (Double.parseDouble(ka5pf[32][f]) - Double.parseDouble(ka5pf[28][f]));
                        }
                    }
                    if (csubs.equals("0")){
                      cLPS = 0;
                      cMPS = 0;
                      cFPS = 0; 
                    }
                    if (csubs.equals(ka5pf[0][f])) {
                        if (cdichted < 1.2) {
                            cLPS = Double.parseDouble(ka5pf[1][f]);
                            cMPS = Double.parseDouble(ka5pf[6][f]);
                            cFPS = Double.parseDouble(ka5pf[16][f]);
                        } else if (cdichted >= 1.2 && cdichted < 1.4) {
                            cLPS = Double.parseDouble(ka5pf[2][f]);
                            cMPS = Double.parseDouble(ka5pf[7][f]);
                            cFPS = Double.parseDouble(ka5pf[17][f]);
                        } else if (cdichted >= 1.4 && cdichted < 1.6) {
                            cLPS = Double.parseDouble(ka5pf[3][f]);
                            cMPS = Double.parseDouble(ka5pf[8][f]);
                            cFPS = Double.parseDouble(ka5pf[18][f]);
                        } else if (cdichted >= 1.6 && cdichted < 1.8) {
                            cLPS = Double.parseDouble(ka5pf[4][f]);
                            cMPS = Double.parseDouble(ka5pf[9][f]);
                            cFPS = Double.parseDouble(ka5pf[19][f]);
                        } else if (cdichted >= 1.8) {
                            cLPS = Double.parseDouble(ka5pf[5][f]);
                            cMPS = Double.parseDouble(ka5pf[10][f]);
                            cFPS = Double.parseDouble(ka5pf[20][f]);
                        }
                        if (ccorgd >= 1 && ccorgd < 2) {
                            cLPS = cLPS + Double.parseDouble(ka5pf[21][f]);
                            cMPS = cMPS + Double.parseDouble(ka5pf[25][f]);
                            cFPS = cFPS + (Double.parseDouble(ka5pf[29][f]) - Double.parseDouble(ka5pf[25][f]));
                        } else if (ccorgd >= 2 && ccorgd < 4) {
                            cLPS = cLPS + Double.parseDouble(ka5pf[22][f]);
                            cMPS = cMPS + Double.parseDouble(ka5pf[26][f]);
                            cFPS = cFPS + (Double.parseDouble(ka5pf[30][f]) - Double.parseDouble(ka5pf[26][f]));
                        } else if (ccorgd >= 4 && ccorgd < 8) {
                            cLPS = cLPS + Double.parseDouble(ka5pf[23][f]);
                            cMPS = cMPS + Double.parseDouble(ka5pf[27][f]);
                            cFPS = cFPS + (Double.parseDouble(ka5pf[31][f]) - Double.parseDouble(ka5pf[27][f]));
                        } else if (ccorgd >= 8) {
                            cLPS = cLPS + Double.parseDouble(ka5pf[24][f]);
                            cMPS = cMPS + Double.parseDouble(ka5pf[28][f]);
                            cFPS = cFPS + (Double.parseDouble(ka5pf[32][f]) - Double.parseDouble(ka5pf[28][f]));
                        }
                    }
                    if (dsubs.equals("0")){
                      dLPS = 0;
                      dMPS = 0;
                      dFPS = 0; 
                    }
                    if (dsubs.equals(ka5pf[0][f])) {
                        if (ddichted < 1.2) {
                            dLPS = Double.parseDouble(ka5pf[1][f]);
                            dMPS = Double.parseDouble(ka5pf[6][f]);
                            dFPS = Double.parseDouble(ka5pf[16][f]);
                        } else if (ddichted >= 1.2 && ddichted < 1.4) {
                            dLPS = Double.parseDouble(ka5pf[2][f]);
                            dMPS = Double.parseDouble(ka5pf[7][f]);
                            dFPS = Double.parseDouble(ka5pf[17][f]);
                        } else if (ddichted >= 1.4 && ddichted < 1.6) {
                            dLPS = Double.parseDouble(ka5pf[3][f]);
                            dMPS = Double.parseDouble(ka5pf[8][f]);
                            dFPS = Double.parseDouble(ka5pf[18][f]);
                        } else if (ddichted >= 1.6 && ddichted < 1.8) {
                            dLPS = Double.parseDouble(ka5pf[4][f]);
                            dMPS = Double.parseDouble(ka5pf[9][f]);
                            dFPS = Double.parseDouble(ka5pf[19][f]);
                        } else if (ddichted >= 1.8) {
                            dLPS = Double.parseDouble(ka5pf[5][f]);
                            dMPS = Double.parseDouble(ka5pf[10][f]);
                            dFPS = Double.parseDouble(ka5pf[20][f]);
                        }
                        if (dcorgd >= 1 && dcorgd < 2) {
                            dLPS = dLPS + Double.parseDouble(ka5pf[21][f]);
                            dMPS = dMPS + Double.parseDouble(ka5pf[25][f]);
                            dFPS = dFPS + (Double.parseDouble(ka5pf[29][f]) - Double.parseDouble(ka5pf[25][f]));
                        } else if (dcorgd >= 2 && dcorgd < 4) {
                            dLPS = dLPS + Double.parseDouble(ka5pf[22][f]);
                            dMPS = dMPS + Double.parseDouble(ka5pf[26][f]);
                            dFPS = dFPS + (Double.parseDouble(ka5pf[30][f]) - Double.parseDouble(ka5pf[26][f]));
                        } else if (dcorgd >= 4 && dcorgd < 8) {
                            dLPS = dLPS + Double.parseDouble(ka5pf[23][f]);
                            dMPS = dMPS + Double.parseDouble(ka5pf[27][f]);
                            dFPS = dFPS + (Double.parseDouble(ka5pf[31][f]) - Double.parseDouble(ka5pf[27][f]));
                        } else if (dcorgd >= 8) {
                            dLPS = dLPS + Double.parseDouble(ka5pf[24][f]);
                            dMPS = dMPS + Double.parseDouble(ka5pf[28][f]);
                            dFPS = dFPS + (Double.parseDouble(ka5pf[32][f]) - Double.parseDouble(ka5pf[28][f]));
                        }
                    }
                    if (esubs.equals("0")){
                      eLPS = 0;
                      eMPS = 0;
                      eFPS = 0; 
                    }
                    if (esubs.equals(ka5pf[0][f])) {
                        if (edichted < 1.2) {
                            eLPS = Double.parseDouble(ka5pf[1][f]);
                            eMPS = Double.parseDouble(ka5pf[6][f]);
                            eFPS = Double.parseDouble(ka5pf[16][f]);
                        } else if (edichted >= 1.2 && edichted < 1.4) {
                            eLPS = Double.parseDouble(ka5pf[2][f]);
                            eMPS = Double.parseDouble(ka5pf[7][f]);
                            eFPS = Double.parseDouble(ka5pf[17][f]);
                        } else if (edichted >= 1.4 && edichted < 1.6) {
                            eLPS = Double.parseDouble(ka5pf[3][f]);
                            eMPS = Double.parseDouble(ka5pf[8][f]);
                            eFPS = Double.parseDouble(ka5pf[18][f]);
                        } else if (edichted >= 1.6 && edichted < 1.8) {
                            eLPS = Double.parseDouble(ka5pf[4][f]);
                            eMPS = Double.parseDouble(ka5pf[9][f]);
                            eFPS = Double.parseDouble(ka5pf[19][f]);
                        } else if (edichted >= 1.8) {
                            eLPS = Double.parseDouble(ka5pf[5][f]);
                            eMPS = Double.parseDouble(ka5pf[10][f]);
                            eFPS = Double.parseDouble(ka5pf[20][f]);
                        }
                        if (ecorgd >= 1 && ecorgd < 2) {
                            eLPS = eLPS + Double.parseDouble(ka5pf[21][f]);
                            eMPS = eMPS + Double.parseDouble(ka5pf[25][f]);
                            eFPS = eFPS + (Double.parseDouble(ka5pf[29][f]) - Double.parseDouble(ka5pf[25][f]));
                        } else if (ecorgd >= 2 && ecorgd < 4) {
                            eLPS = eLPS + Double.parseDouble(ka5pf[22][f]);
                            eMPS = eMPS + Double.parseDouble(ka5pf[26][f]);
                            eFPS = eFPS + (Double.parseDouble(ka5pf[30][f]) - Double.parseDouble(ka5pf[26][f]));
                        } else if (ecorgd >= 4 && ecorgd < 8) {
                            eLPS = eLPS + Double.parseDouble(ka5pf[23][f]);
                            eMPS = eMPS + Double.parseDouble(ka5pf[27][f]);
                            eFPS = eFPS + (Double.parseDouble(ka5pf[31][f]) - Double.parseDouble(ka5pf[27][f]));
                        } else if (ecorgd >= 8) {
                            eLPS = eLPS + Double.parseDouble(ka5pf[24][f]);
                            eMPS = eMPS + Double.parseDouble(ka5pf[28][f]);
                            eFPS = eFPS + (Double.parseDouble(ka5pf[32][f]) - Double.parseDouble(ka5pf[28][f]));
                        }
                    }











                    f++;
                }
                String saLPS = String.valueOf(aLPS);
                String saMPS = String.valueOf(aMPS);
                String saFPS = String.valueOf(aFPS);
                String sbLPS = String.valueOf(bLPS);                
                String sbMPS = String.valueOf(bMPS);
                String sbFPS = String.valueOf(bFPS);
                String scLPS = String.valueOf(cLPS);                
                String scMPS = String.valueOf(cMPS);
                String scFPS = String.valueOf(cFPS);
                String sdLPS = String.valueOf(dLPS);                
                String sdMPS = String.valueOf(dMPS);
                String sdFPS = String.valueOf(dFPS);
                String seLPS = String.valueOf(eLPS);                
                String seMPS = String.valueOf(eMPS);
                String seFPS = String.valueOf(eFPS);
                
                LPSarray[0] = aLPS;
                LPSarray[1] = bLPS;
                LPSarray[2] = cLPS;
                LPSarray[3] = dLPS;
                LPSarray[4] = eLPS;
                                
                MPSarray[0] = aMPS;
                MPSarray[1] = bMPS;
                MPSarray[2] = cMPS;
                MPSarray[3] = dMPS;
                MPSarray[4] = eMPS;
                
                FPSarray[0] = aFPS;
                FPSarray[1] = bFPS;
                FPSarray[2] = cFPS;
                FPSarray[3] = dFPS;
                FPSarray[4] = eFPS;
                
                

                result = saLPS + "\t" + saMPS + "\t" + saFPS + "\t" + sbLPS + "\t" + sbMPS + "\t" + sbFPS + "\t" + scLPS + "\t" + scMPS + "\t" + scFPS + "\t" + sdLPS + "\t" + sdMPS + "\t" + sdFPS + "\t" + seLPS + "\t" + seMPS + "\t" + seFPS;
            } else {
                result = "aLPS" + "\t" + "aMPS" + "\t" + "aFPS" + "\t" + "bLPS" + "\t" + "bMPS" + "\t" + "bFPS" + "\t" + "cLPS" + "\t" + "cMPS" + "\t" + "cFPS" + "\t" + "dLPS" + "\t" + "dMPS" + "\t" + "dFPS" + "\t" + "eLPS" + "\t" + "eMPS" + "\t" + "eFPS";
            }



            writer.write(result);
            writer.newLine();

            System.out.println(result + " x = " + x);
            
        
            //write J2k soil.par
            int c = 0;
            double actdepth = 10;
            double hordepth = deptharray[0];
            float totdepth = atiefed + btiefed + ctiefed + dtiefed + etiefed;
            int cmax =  Math.round(totdepth/10);
            int h = 0;
            double LPStot = 0;
            double MPStot = 0;
            String J2kline = "";
            String J2kMPSline = "";
                    
            double MPSj2karray[] = new double[cmax];
            
            while (c <  cmax){
                
                if (hordepth >= actdepth){
                   MPSj2karray[c] = MPSarray[h];
                   MPStot = MPStot + MPSj2karray[c];
                   LPStot = LPStot + LPSarray[h];
                }else{
                   if ((actdepth - hordepth) > 0){
                       if (h < 4 && MPSarray[h+1] > 0){
                       MPSj2karray[c] = ((hordepth + 10 - actdepth)/10) * MPSarray[h] + (((actdepth - hordepth)/10)) * MPSarray[h+1];
                       LPStot = LPStot + ((hordepth + 10 - actdepth)/10) * LPSarray[h] + (((actdepth - hordepth)/10)) * LPSarray[h+1];
                       h++;
                       hordepth = hordepth + deptharray[h];  
                       MPStot = MPStot + MPSj2karray[c];
                       }else{
                       MPSj2karray[c] = ((hordepth - (actdepth - 10))/10) * MPSarray[h];
                       LPStot = LPStot + ((hordepth - (actdepth - 10))/10) * LPSarray[h];
                       MPStot = MPStot + MPSj2karray[c];
                       }
                   }
                    
                }
           
                
                J2kMPSline = J2kMPSline + "\t" +  String.valueOf(MPSj2karray[c]);
                actdepth = actdepth + 10;
                c++;
                
            }
            
            if (x > 0){
            J2kline = String.valueOf(x) + "\t" +  String.valueOf(totdepth)  + "\t" +  String.valueOf(0)   + "\t" +  String.valueOf(0)+ "\t" +  String.valueOf(0) + "\t" +  String.valueOf(0)  + "\t" +  String.valueOf(LPStot) + "\t" +  String.valueOf(MPStot) + J2kMPSline;
            }
            x++;
            
            
            writer1.write(J2kline);
            writer1.newLine();
        
            
            
            
        
        }

        return result;


    }

    public static void main(String[] args) {
        PF_KF_KA5 PF = new PF_KF_KA5();
        
        PF.convert("D:/LUCCI/gisdata/Soil/soil_bearbeitet_neu.txt", "D:/LUCCI/gisdata/Soil/soil_bearbeitet_PF.TXT");
    }
}