/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.tools;

import jams.data.Attribute;
import org.jams.j2k.s_n.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Program for implementation of the pedo transfer function of the KA5 for the
 * pf values using bulk densitty, soil textur classes and organic carbon
 *
 *
 * @author c8fima
 */
public class Disaggregate_rain24 {

    BufferedWriter writer;

    public void convert(String inputFileName, String outputFileName) {

        int j = 0;
        int i = 0;
        BufferedReader reader;

        String lineprecip;

        int f = 0;
        int e = 0;
        int ncolprecip = 0;
        StringTokenizer tokprecip;
        StringTokenizer tokdate;
        StringTokenizer toktime;

        String value, day, mounth, year, hour, minute;
        double dataval = 0;
        double[] disdat = null;
        String[][] precip_string = null;

        day = null;
        mounth = null;
        year = null;
        hour = null;
        minute = null;
        String datestr[] = new String[24];
        String Headerpr = null;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            writer = new BufferedWriter(new FileWriter(outputFileName));

            lineprecip = reader.readLine();
            tokprecip = new StringTokenizer(lineprecip);
            ncolprecip = tokprecip.countTokens();
            String[] Headerprecip = new String[ncolprecip];

            e = 0;

            while (!lineprecip.equals("@end")) {

                if (f == 0) {

                    while (tokprecip.hasMoreTokens()) {
                        value = tokprecip.nextToken();
                        Headerprecip[e] = value;
                        e++;
                    }
                    precip_string = new String[e][100000];
                    int iii = 0;
                    while (iii < e) {

                        if (iii == 0) {
                            Headerpr = Headerprecip[iii];
                        } else if (iii == 1) {
                            Headerpr = Headerpr + " " + Headerprecip[iii];
                        } else {
                            Headerpr = Headerpr + "\t" + Headerprecip[iii];
                        }
                        iii++;
                    }
                    writer.write(Headerpr);
                    writer.newLine();
                    lineprecip = reader.readLine();

                } else {

                    tokprecip = new StringTokenizer(lineprecip);

                    int eee = 0;

                    while (eee < e) {
                        value = tokprecip.nextToken();
                        precip_string[eee][f - 1] = value;
                        eee++;
                    }

                    lineprecip = reader.readLine();
                }

                f++;
            }

            j = 0;
            int hourd = 0;
            String[] valuestr = new String[24];
            while (j < f - 2) {

                i = 0;
                disdat = new double[24];

                while (i < e) {
                    int jj = 0;
                    while (jj < 24) {
                        if (i == 0) {
                            tokdate = new StringTokenizer(precip_string[i][j], "-");
                            year = tokdate.nextToken();
                            mounth = tokdate.nextToken();
                            day = tokdate.nextToken();
                        } else if (i == 1) {
                            toktime = new StringTokenizer(precip_string[i][j], ":");
                            hour = toktime.nextToken();
                            hourd = Integer.parseInt(hour);
                            minute = toktime.nextToken();
                        } else {
                            dataval = Double.parseDouble(precip_string[i][j]);

                            if (dataval == 0) {

                                disdat[0] = 0;
                                disdat[1] = 0;
                                disdat[2] = 0;
                                disdat[3] = 0;
                                disdat[4] = 0;
                                disdat[5] = 0;
                                 disdat[6] = 0;
                                disdat[7] = 0;
                                disdat[8] = 0;
                                disdat[9] = 0;
                                disdat[10] = 0;
                                disdat[11] = 0;
                                disdat[12] = 0;
                                disdat[13] = 0;
                                disdat[14] = 0;
                                disdat[15] = 0;
                                disdat[16] = 0;
                                disdat[17] = 0;
                                disdat[18] = 0;
                                disdat[19] = 0;
                                disdat[20] = 0;
                                disdat[21] = 0;
                                disdat[22] = 0;
                                disdat[23] = 0;
                            } else if (dataval == -9999) {

                                disdat[0] = -9999.0;
                                disdat[1] = -9999.0;
                                disdat[2] = -9999.0;
                                disdat[3] = -9999.0;
                                disdat[4] = -9999.0;
                                disdat[5] = -9999.0;
                                 disdat[6] = -9999.0;
                                disdat[7] = -9999.0;
                                disdat[8] = -9999.0;
                                disdat[9] = -9999.0;
                                disdat[10] = -9999.0;
                                disdat[11] = -9999.0;
                                disdat[12] = -9999.0;
                                disdat[13] = -9999.0;
                                disdat[14] = -9999.0;
                                disdat[15] = -9999.0;
                                disdat[16] = -9999.0;
                                disdat[17] = -9999.0;
                                disdat[18] = -9999.0;
                                disdat[19] = -9999.0;
                                disdat[20] = -9999.0;
                                disdat[21] = -9999.0;
                                disdat[22] = -9999.0;
                                disdat[23] = -9999.0;

                            } else if (dataval <= 1) {

                                disdat[0] = 0;
                                disdat[1] = 0;
                                disdat[2] = dataval * 0.5;
                                disdat[3] = dataval * 0.5;
                                disdat[4] = 0;
                                disdat[5] = 0;
                                 disdat[6] = 0;
                                disdat[7] = 0;
                                disdat[8] = 0;
                                disdat[9] = 0;
                                disdat[10] = 0;
                                disdat[11] = 0;
                                disdat[12] = 0;
                                disdat[13] = 0;
                                disdat[14] = 0;
                                disdat[15] = 0;
                                disdat[16] = 0;
                                disdat[17] = 0;
                                disdat[18] = 0;
                                disdat[19] = 0;
                                disdat[20] = 0;
                                disdat[21] = 0;
                                disdat[22] = 0;
                                disdat[23] = 0;
                            } else if (dataval <= 5) {
                                disdat[0] = 0;
                                disdat[1] = dataval / 3;
                                disdat[2] = dataval / 3;
                                disdat[3] = dataval / 3;
                                disdat[4] = 0;
                                disdat[5] = 0;
                                 disdat[6] = 0;
                                disdat[7] = 0;
                                disdat[8] = 0;
                                disdat[9] = 0;
                                disdat[10] = 0;
                                disdat[11] = 0;
                                disdat[12] = 0;
                                disdat[13] = 0;
                                disdat[14] = 0;
                                disdat[15] = 0;
                                disdat[16] = 0;
                                disdat[17] = 0;
                                disdat[18] = 0;
                                disdat[19] = 0;
                                disdat[20] = 0;
                                disdat[21] = 0;
                                disdat[22] = 0;
                                disdat[23] = 0;

                            } else if (dataval <= 12) {
                                disdat[0] = 0;
                                disdat[1] = dataval * 0.24;
                                disdat[2] = dataval * 0.28;
                                disdat[3] = dataval * 0.24;
                                disdat[4] = dataval * 0.24;
                                disdat[5] = 0;
                                disdat[6] = 0;
                                disdat[7] = 0;
                                disdat[8] = 0;
                                disdat[9] = 0;
                                disdat[10] = 0;
                                disdat[11] = 0;
                                disdat[12] = 0;
                                disdat[13] = 0;
                                disdat[14] = 0;
                                disdat[15] = 0;
                                disdat[16] = 0;
                                disdat[17] = 0;
                                disdat[18] = 0;
                                disdat[19] = 0;
                                disdat[20] = 0;
                                disdat[21] = 0;
                                disdat[22] = 0;
                                disdat[23] = 0;

                            } else if (dataval <= 100) {

                                disdat[0] = dataval * 0.18;
                                disdat[1] = dataval * 0.28;
                                disdat[2] = dataval * 0.18;
                                disdat[3] = dataval * 0.18;
                                disdat[4] = dataval * 0.18;
                                disdat[5] = 0;
                                 disdat[6] = 0;
                                disdat[7] = 0;
                                disdat[8] = 0;
                                disdat[9] = 0;
                                disdat[10] = 0;
                                disdat[11] = 0;
                                disdat[12] = 0;
                                disdat[13] = 0;
                                disdat[14] = 0;
                                disdat[15] = 0;
                                disdat[16] = 0;
                                disdat[17] = 0;
                                disdat[18] = 0;
                                disdat[19] = 0;
                                disdat[20] = 0;
                                disdat[21] = 0;
                                disdat[22] = 0;
                                disdat[23] = 0;

                            } else {

                                disdat[0] = dataval * 0.03;
                                disdat[1] = dataval * 0.31;
                                disdat[2] = dataval * 0.03;
                                disdat[3] = dataval * 0.03;
                                disdat[4] = dataval * 0.03;
                                disdat[5] = dataval * 0.03;
                                disdat[6] = dataval * 0.03;
                                disdat[7] = dataval * 0.03;
                                disdat[8] = dataval * 0.03;
                                disdat[9] = dataval * 0.03;
                                disdat[10] = dataval * 0.03;
                                disdat[11] = dataval * 0.03;
                                disdat[12] = dataval * 0.03;
                                disdat[13] = dataval * 0.03;
                                disdat[14] = dataval * 0.03;
                                disdat[15] = dataval * 0.03;
                                disdat[16] = dataval * 0.03;
                                disdat[17] = dataval * 0.03;
                                disdat[18] = dataval * 0.03;
                                disdat[19] = dataval * 0.03;
                                disdat[20] = dataval * 0.03;
                                disdat[21] = dataval * 0.03;
                                disdat[22] = dataval * 0.03;
                                disdat[23] = dataval * 0.03;
                                

                            }

                            int hourdi = hourd + jj;
                            if (hourdi < 10) {
                                datestr[jj] = year + "-" + mounth + "-" + day + " " + "0" + Integer.toString(hourdi) + ":" + minute;
                            } else if (hourdi < 24) {
                                datestr[jj] = year + "-" + mounth + "-" + day + " " + Integer.toString(hourdi) + ":" + minute;
                            } else {
                                System.out.println("more than 24 hours!");
                                //System.exit(0);
                            }

                            if (i == 2) {
                                valuestr[jj] = Double.toString(disdat[jj]);
                            } else {
                                valuestr[jj] = valuestr[jj] + "\t" + Double.toString(disdat[jj]);
                            }

                        }
                        jj++;
                    }
                    i++;
                }
                int ii = 0;
                while (ii < 24) {
                    writer.write(datestr[ii] + "\t" + valuestr[ii]);
                    System.out.println(ii);
                    valuestr[ii] = null;
                    writer.newLine();
                    ii++;
                }
                j++;
            }

            writer.close();
            reader.close();

        } catch (IOException ee) {
            System.out.println("io fehler");
            ee.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Disaggregate_rain24 Precipi = new Disaggregate_rain24();

        Precipi.convert("C:\\LUCCI\\Delta_t\\Data\\rain_24h.txt", "C:\\LUCCI\\Delta_t\\Data\\rain_1h_24.txt");
    }
}
