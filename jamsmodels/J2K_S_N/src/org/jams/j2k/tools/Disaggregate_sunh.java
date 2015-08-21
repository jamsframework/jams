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
public class Disaggregate_sunh {

    BufferedWriter writer;

    public void convert(String inputFileName, String outputFileName) {

        int j = 0;
        int i = 0;
        BufferedReader reader;

        String linesunh;

        int f = 0;
        int e = 0;

        int ncolsunh = 0;
        StringTokenizer toksunh;
        StringTokenizer tokdate;
        StringTokenizer toktime;

        String value, day, mounth, year, hour, minute;

        double datavalsunh = 0;
        double[] disdat = null;

        String[][] sunh_string = null;

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

            linesunh = reader.readLine();
            toksunh = new StringTokenizer(linesunh);
            ncolsunh = toksunh.countTokens();
            String[] Headersunh = new String[ncolsunh];

            e = 0;

            while (!linesunh.equals("@end")) {

                if (f == 0) {

                    while (toksunh.hasMoreTokens()) {
                        value = toksunh.nextToken();
                        Headersunh[e] = value;
                        e++;
                    }
                    sunh_string = new String[e][100000];
                    int iii = 0;
                    while (iii < e) {

                        if (iii == 0) {
                            Headerpr = Headersunh[iii];
                        } else if (iii == 1) {
                            Headerpr = Headerpr + " " + Headersunh[iii];
                        } else {
                            Headerpr = Headerpr + "\t" + Headersunh[iii];
                        }
                        iii++;
                    }
                    writer.write(Headerpr);
                    writer.newLine();
                    linesunh = reader.readLine();

                } else {

                    toksunh = new StringTokenizer(linesunh);

                    int eee = 0;

                    while (eee < e) {
                        value = toksunh.nextToken();
                        sunh_string[eee][f - 1] = value;
                        eee++;
                    }

                    linesunh = reader.readLine();
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

                    double increment1 = 11.25;
                    double angle2 = 0;
                    double increment2 = 10;
                    double angle3 = 90;

                    while (jj < 24) {
                        if (i == 0) {
                            tokdate = new StringTokenizer(sunh_string[i][j], "-");
                            year = tokdate.nextToken();
                            mounth = tokdate.nextToken();
                            day = tokdate.nextToken();
                        } else if (i == 1) {
                            toktime = new StringTokenizer(sunh_string[i][j], ":");
                            hour = toktime.nextToken();
                            hourd = Integer.parseInt(hour);
                            minute = toktime.nextToken();
                        } else {

                            datavalsunh = Double.parseDouble(sunh_string[i][j]);

                            

                        if (jj < 19 && jj > 5){    
                            
                            disdat[jj] = datavalsunh/12;
                            disdat[jj] = Math.min(disdat[jj], 1.0);

                        }else{
                            disdat[jj] = 0;
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
        Disaggregate_sunh Precipi = new Disaggregate_sunh();

        Precipi.convert("C:\\LUCCI\\Delta_t\\Data\\sunh_24h.txt", "C:\\LUCCI\\Delta_t\\Data\\sunh_1h.txt");
    }
}
