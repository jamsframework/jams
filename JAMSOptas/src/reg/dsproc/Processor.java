/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.dsproc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Fischer
 */
public abstract class Processor {

    protected DataStoreProcessor dsdb;
    protected Connection conn;
    protected ArrayList<DataStoreProcessor.ContextData> contexts;
    protected ProcessingProgressObservable processingProgressObservable = new ProcessingProgressObservable();
    protected boolean abortOperation = false;

    /**
     * @return the h2ds
     */
    public DataStoreProcessor getDataStoreProcessor() {
        return dsdb;
    }

    public void close() throws SQLException {
        processingProgressObservable = null;
        conn.close();
        dsdb.close();
    }

    public void addProcessingProgressObserver(Observer o) {
        processingProgressObservable.addObserver(o);
    }

    public void sendAbortOperation() {
        this.abortOperation = true;
        synchronized (this) {
            this.abortOperation = false;
        }
    }

    public static void output(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        for (int i = 1; i <= numberOfColumns; i++) {
            System.out.print(rsmd.getColumnName(i) + "\t");
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }
    }

    protected synchronized boolean isTableExisting(String tableName) throws SQLException {
        String q = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='" + tableName + "'";
        ResultSet rs = customSelectQuery(q);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    protected double[][] calcWeights(double a[][], int weightAttribIndex) {

        double result[][] = new double[a.length][a[0].length];
        double relWeights[] = null;
        double absWeights[] = null;
        int i = -1;

        for (DataStoreProcessor.AttributeData attrib : dsdb.getAttributes()) {

            if (!attrib.isSelected()) {
                continue;
            }

            i++;

            if (attrib.getWeightingType() != DataStoreProcessor.AttributeData.WEIGHTING_TIMES_AREA) {

                double weight = 0;
                if (attrib.getAggregationType() == DataStoreProcessor.AttributeData.AGGREGATION_MEAN) {

                    weight = 1d / a.length;

                } else if (attrib.getAggregationType() == DataStoreProcessor.AttributeData.AGGREGATION_SUM) {

                    weight = 1d;

                }
                for (int j = 0; j < a.length; j++) {
                    result[j][i] = weight;
                }

            } else if (weightAttribIndex >= 0) {

                if (attrib.getAggregationType() == DataStoreProcessor.AttributeData.AGGREGATION_MEAN) {

                    // if the relative weights have not been calculated yet
                    // do so now
                    if (relWeights == null) {
                        relWeights = new double[a.length];
                        double sum = 0;
                        for (int j = 0; j < a.length; j++) {
                            sum += a[j][weightAttribIndex];
                        }
                        for (int j = 0; j < a.length; j++) {
                            relWeights[j] = a[j][weightAttribIndex] / sum;
                        }
                    }

                    for (int j = 0; j < a.length; j++) {
                        result[j][i] = relWeights[j];
                    }

                } else if (attrib.getAggregationType() == DataStoreProcessor.AttributeData.AGGREGATION_SUM) {

                    // if the absolute weights have not been calculated yet
                    // do so now
                    if (absWeights == null) {
                        absWeights = new double[a.length];
                        for (int j = 0; j < a.length; j++) {
                            absWeights[j] = a[j][weightAttribIndex];
                        }
                    }

                    for (int j = 0; j < a.length; j++) {
                        result[j][i] = absWeights[j];
                    }

                }

            } else {
                Logger.getLogger(Processor.class.getName()).log(Level.INFO, "No area attribute has been chosen! Applying unweighted sum aggregation.");
                for (int j = 0; j < a.length; j++) {
                    result[j][i] = 1;
                }
            }

        }

        return result;
    }

    private static double[] arrayMulti(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    protected synchronized double[] getWeightedSum(double[][] a, double[][] weights) {

        double[] result = arrayMulti(a[0], weights[0]);
        double[] b;
        for (int i = 1; i < a.length; i++) {
            b = arrayMulti(a[i], weights[i]);
            for (int c = 0; c < result.length; c++) {
                result[c] += b[c];
            }
        }
//        for (int c = 0; c < result.length; c++) {
//            result[c] /= a.size();
//        }

        return result;
    }

    protected synchronized double[] getAvg(ArrayList<double[]> a) {

        double[] result = a.get(0);
        double[] b;
        for (int i = 1; i < a.size(); i++) {
            b = a.get(i);
            for (int c = 0; c < result.length; c++) {
                result[c] += b[c] / a.size();
            }
        }
        return result;
    }

    /**
     * Send a custom query to the database
     * @param query The query string
     * @return true, if the query was sent successfully, false otherwise
     * @throws java.sql.SQLException
     */
    public synchronized boolean customQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        boolean result = stmt.execute(query);
        return result;
    }

    /**
     * Send a custom select-query to the database
     * @param query The query string
     * @return A JDBC result set
     * @throws java.sql.SQLException
     */
    public synchronized ResultSet customSelectQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

    protected class ProcessingProgressObservable extends Observable {

        private int progress;

        protected void setProgress(int progress) {
            this.progress = progress;
            this.setChanged();
            this.notifyObservers(progress);
        }
    }
}
