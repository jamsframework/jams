/*
 * J2KInputDataStore.java
 * Created on 13. Oktober 2008, 17:22
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.workspace.stores;

import jams.workspace.DataSet;
import jams.workspace.TSDumpProcessor;
import jams.workspace.VirtualWorkspace;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.StringValue;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class J2KInputDataStore extends StandardInputDataStore {

    private String cache = null;
    private int columnCount = 0;

    public J2KInputDataStore(VirtualWorkspace ws, File file) throws IOException {

        super(file);
        this.columnCount = this.getDataSetDefinition().getColumnCount();
    /*
    if (ws.getRuntime().getRunState() != JAMS.RUNSTATE_RUN) {
    return;
    }
     */
    }

    @Override
    public boolean hasNext() {
        if (cache != null) {
            return true;
        } else {
            try {
                cache = dumpReader.readLine();
                return ((cache != null) && (!cache.startsWith("#")));
            } catch (IOException ioe) {
                return false;
            }
        }
    }

    @Override
    public DataSet getNext() {

        if (!hasNext()) {
            return null;
        }

        DataSet result = new DataSet(columnCount + 1);
        StringTokenizer tok = new StringTokenizer(cache, "\t");

        String timeString = tok.nextToken();
        tok.nextToken();
        result.setData(0, new StringValue(timeString));

        int i = 1;
        while (tok.hasMoreTokens()) {
            double d = Double.parseDouble(tok.nextToken());
            DoubleValue dValue = new DoubleValue(d);
            result.setData(i, dValue);
            i++;
        }

        cache = null;
        return result;
    }

    @Override
    public void close() throws IOException {
        dumpReader.close();
    }

    public static void main(String[] args) throws Exception {
        J2KInputDataStore ds = new J2KInputDataStore(null, new File("D:/jamsapplication/JAMS-Gehlberg/data/rain.dat"));
        //System.out.println(ds.getDataSetDefinition().toASCIIString());

        TSDumpProcessor writer = new TSDumpProcessor(ds);
        System.out.println(writer.toASCIIString());
        /*
        DataSet data = ds.getNext();
        while (data != null) {
            //System.out.println(data.toString());
            data = ds.getNext();
        }
         */
    }
}
