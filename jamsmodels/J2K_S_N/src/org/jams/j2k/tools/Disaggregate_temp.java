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
public class Disaggregate_temp {

    BufferedWriter writer;

    public void convert(String inputFileName, String inputFileName1, String inputFileName2, String outputFileName) {

        int j = 0;
        int i = 0;
        BufferedReader reader;
        BufferedReader reader1;
        BufferedReader reader2;

        String linetmax;
        String linetmean;
        String linetmin;

        int f = 0;
        int e = 0;
        int ncoltmax = 0;
        StringTokenizer toktmax;
        int ncoltmean = 0;
        StringTokenizer toktmean;
        int ncoltmin = 0;
        StringTokenizer toktmin;
        StringTokenizer tokdate;
        StringTokenizer toktime;

        String value, day, mounth, year, hour, minute;
        double datavaltmax = 0;
        double datavaltmean = 0;
        double datavaltmin = 0;
        double[] disdat = null;
        String[][] tmax_string = null;
        String[][] tmean_string = null;
        String[][] tmin_string = null;

        day = null;
        mounth = null;
        year = null;
        hour = null;
        minute = null;
        String datestr[] = new String[24];
        String Headerpr = null;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            reader1 = new BufferedReader(new FileReader(inputFileName1));
            reader2 = new BufferedReader(new FileReader(inputFileName2));
            writer = new BufferedWriter(new FileWriter(outputFileName));

            linetmax = reader.readLine();
            toktmax = new StringTokenizer(linetmax);
            ncoltmax = toktmax.countTokens();
            String[] Headertmax = new String[ncoltmax];
            linetmean = reader1.readLine();
            toktmean = new StringTokenizer(linetmean);
            ncoltmean = toktmean.countTokens();
            String[] Headertmean = new String[ncoltmean];
            linetmin = reader2.readLine();
            toktmin = new StringTokenizer(linetmin);
            ncoltmin = toktmin.countTokens();
            String[] Headertmin = new String[ncoltmin];

            e = 0;

            while (!linetmax.equals("@end")) {

                if (f == 0) {

                    while (toktmax.hasMoreTokens()) {
                        value = toktmax.nextToken();
                        Headertmax[e] = value;
                        e++;
                    }
                    tmax_string = new String[e][100000];

                    linetmax = reader.readLine();

                } else {

                    toktmax = new StringTokenizer(linetmax);

                    int eee = 0;

                    while (eee < e) {
                        value = toktmax.nextToken();
                        tmax_string[eee][f - 1] = value;
                        eee++;
                    }

                    linetmax = reader.readLine();
                }

                f++;
            }

            f = 0;
            e = 0;
            while (!linetmean.equals("@end")) {

                if (f == 0) {

                    while (toktmean.hasMoreTokens()) {
                        value = toktmean.nextToken();
                        Headertmean[e] = value;
                        e++;
                    }
                    tmean_string = new String[e][100000];
                    linetmean = reader1.readLine();

                } else {

                    toktmean = new StringTokenizer(linetmean);

                    int eee = 0;

                    while (eee < e) {
                        value = toktmean.nextToken();
                        tmean_string[eee][f - 1] = value;
                        eee++;
                    }

                    linetmean = reader1.readLine();
                }

                f++;
            }

            f = 0;
            e = 0;
            while (!linetmin.equals("@end")) {

                if (f == 0) {

                    while (toktmin.hasMoreTokens()) {
                        value = toktmin.nextToken();
                        Headertmin[e] = value;
                        e++;
                    }
                    tmin_string = new String[e][100000];
                    int iii = 0;
                    while (iii < e) {

                        if (iii == 0) {
                            Headerpr = Headertmin[iii];
                        } else if (iii == 1) {
                            Headerpr = Headerpr + " " + Headertmin[iii];
                        } else {
                            Headerpr = Headerpr + "\t" + Headertmin[iii];
                        }
                        iii++;
                    }
                    writer.write(Headerpr);
                    writer.newLine();
                    linetmin = reader2.readLine();

                } else {

                    toktmin = new StringTokenizer(linetmin);

                    int eee = 0;

                    while (eee < e) {
                        value = toktmin.nextToken();
                        tmin_string[eee][f - 1] = value;
                        eee++;
                    }

                    linetmin = reader2.readLine();
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
                    double angle1 = 240;
                    double increment1 = 10;
                    double angle2 = 0;
                    double increment2 = 18;
                    double angle3 = 180;

                    while (jj < 24) {
                        if (i == 0) {
                            tokdate = new StringTokenizer(tmax_string[i][j], "-");
                            year = tokdate.nextToken();
                            mounth = tokdate.nextToken();
                            day = tokdate.nextToken();
                        } else if (i == 1) {
                            toktime = new StringTokenizer(tmax_string[i][j], ":");
                            hour = toktime.nextToken();
                            hourd = Integer.parseInt(hour);
                            minute = toktime.nextToken();
                        } else {
                            datavaltmax = Double.parseDouble(tmax_string[i][j]);
                            datavaltmean = Double.parseDouble(tmean_string[i][j]);
                            datavaltmin = Double.parseDouble(tmin_string[i][j]);

                            if (jj < 8) {

                                disdat[jj] = part2(angle1, datavaltmax, datavaltmean, datavaltmin);
                                angle1 = angle1 + increment1;

                            } else if (jj < 18) {

                                disdat[jj] = part1(angle2, datavaltmax, datavaltmean, datavaltmin);
                                angle2 = angle2 + increment2;

                            } else {

                                disdat[jj] = part2(angle3, datavaltmax, datavaltmean, datavaltmin);
                                angle3 = angle3 + increment1;

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

    private double part1(double angle, double tmax, double tmean, double tmin) {
        double part1 = tmean + ((tmax - tmean) * Math.sin(angle * Math.PI / 180));
        return part1;
    }

    private double part2(double angle, double tmax, double tmean, double tmin) {
        double part2 = tmean + ((tmean - tmin) * Math.sin(angle * Math.PI / 180));
        return part2;
    }

    public static void main(String[] args) {
        Disaggregate_temp Precipi = new Disaggregate_temp();

        Precipi.convert("C:\\LUCCI\\Delta_t\\Data\\tmax_24h.txt","C:\\LUCCI\\Delta_t\\Data\\tmean_24h.txt","C:\\LUCCI\\Delta_t\\Data\\tmin_24h.txt", "C:\\LUCCI\\Delta_t\\Data\\temp_1h.txt");
    }
}
