/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.sample;

import reg.DataTransfer;
import reg.viewer.Viewer;

/**
 *
 * @author hbusch
 */
public class DataregSample {

    private DataregSample() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Viewer viewer = Viewer.getViewer();

        // add some data
        DataTransfer sampleData = getSampleData();
        boolean done = false;
        int max = 30;
        int act = 1;
        while (!done)
        {
            if (act>max)
            {
                System.out.println("giving up.");
                done = true;
            } else {
                if (!viewer.isInitialized())
                {
                    System.out.println("waiting...");
                    Thread.currentThread().sleep(3000);
                    act++;
                } else {
                    viewer.addData(sampleData);
                    done = true;
                }
            }
        }
    }

    private static DataTransfer getSampleData()
    {
        int max = 614;
        DataTransfer dataTransfer = new DataTransfer();
        String shapeKeyColumn = "POLY_ID";
        dataTransfer.setParentName("hrus.shp");
        dataTransfer.setTargetKeyName(shapeKeyColumn);
        String[] names = {"sample1", "sample2"};
        dataTransfer.setNames(names);
        double[] data1 = new double[max];
        double[] data2 = new double[max];
        double[] ids = new double[max];
        int temp;
        for (int i = 0; i<max; i++)
        {
            data1[i] = i + 1;
            temp = i/10;
            data2[i] = temp;
            ids[i] = max - i;
        }
        dataTransfer.setIds(ids);
        double[][] data = {data1, data2};
        dataTransfer.setData(data);

        return dataTransfer;
    }
}
