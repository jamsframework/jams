/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/** Program for the extraction of soilmoisture time series out of the nod_inf.dat file of Hydrus1d
 *
 * @author nsk & c8fima
 */
public class HydrusnodeoutConverter {

    public static void convert(String inputFileName, String outputFileName) {

        BufferedReader reader;
        BufferedWriter writer;
        String block;

        try {

            reader = new BufferedReader(new FileReader(inputFileName));
            writer = new BufferedWriter(new FileWriter(outputFileName));

            block = readBlock(reader, true);

            while (!block.isEmpty()) {
                writer.write(block);
                writer.newLine();
                block = readBlock(reader, false);
            }

            writer.close();
            reader.close();

        } catch (IOException e) {
            System.out.println("io fehler");
            e.printStackTrace();
        }

    }

    private static String readBlock(BufferedReader reader, boolean firstBlock) throws IOException {

        String result, line, time, nodeID, value;
        StringTokenizer tok;
        int column = 3;

        line = reader.readLine();
        if (line == null) {
            return "";
        } else {
            line = line.trim();
        }

        while (!line.startsWith("Time")) {
            line = reader.readLine().trim();
        }

        tok = new StringTokenizer(line);
        tok.nextToken();
        time = tok.nextToken();

        result = time;

        for (int i = 0; i < 5; i++) {
            reader.readLine();
        }

        int counter = 0;
        line = reader.readLine().trim();
        while (!line.startsWith("end")) {

            tok = new StringTokenizer(line);
            nodeID = tok.nextToken();

            for (int i = 1; i < column; i++) {
                tok.nextToken();
            }
            value = tok.nextToken();
            result = result + "\t" + value;
            counter++;

            line = reader.readLine().trim();
        }

        if (firstBlock) {
            String header = "time";
            for (int i = 1; i <= counter; i++) {
                header = header + "\tnodeID_" + i;
            }
            result = header + "\n" + result;
        }

        return result;

    }

    public static void main(String[] args) {
        HydrusnodeoutConverter.convert("C:/Programme/UCR/HYDRUS-1D/Projects/Direct/Tt_4_8/NOD_INF.OUT", "C:/Programme/UCR/HYDRUS-1D/Projects/Direct/Tt_4_8/NOD_INF.TXT");
    }
}