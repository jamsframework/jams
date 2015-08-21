
package org.jams.j2k.s_n.wq.ProcessRates;

import java.util.HashMap;

public class Table2D {

    private HashMap<String, HashMap<String, Double>> table = new HashMap<String, HashMap<String, Double>>();

    public HashMap<String, Double> addRow(String rowName) {

        if (table.containsKey(rowName)) {
            try {
                return getRow(rowName);
            } catch (NoSuchEntryException ex) {
                // can't happen
            }
        }

        HashMap<String, Double> row = new HashMap<String, Double>();
        table.put(rowName, row);
        return row;

    }

    public HashMap<String, Double> getRow(String rowName) throws NoSuchEntryException {

        if (!table.containsKey(rowName)) {
            throw new NoSuchEntryException("No such row: " + rowName);
        }
        return table.get(rowName);

    }

    public HashMap<String, Double> getCol(String colName) throws NoSuchEntryException {

        if (!table.containsKey(colName)) {
            throw new NoSuchEntryException("No such row: " + colName);
        }
        return table.get(colName);

    }

    public void addCell(String rowName, String colName, double value) {

        HashMap<String, Double> row = null;
        try {
            row = getRow(rowName);
        } catch (NoSuchEntryException nsee) {
            row = addRow(rowName);
        } finally {
            row.put(colName, value);
        }
    }

    public double getCell(String rowName, String colName) throws NoSuchEntryException {

        HashMap<String, Double> row = getRow(rowName);

        if (!row.containsKey(colName)) {
            throw new NoSuchEntryException("No such column: " + colName);
        }

        return row.get(colName);

    }

    public class NoSuchEntryException extends Exception {

        public NoSuchEntryException(String msg) {
            super(msg);
        }
    }
}