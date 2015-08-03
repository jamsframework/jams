/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.io;

import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import java.io.File;
import java.text.DecimalFormat;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.object.h4.H4File;
import ncsa.hdf.object.h4.H4SDS;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(title = "JAMS spatial context",
        author = "Christian Fischer",
        date = "2014-01-17",
        version = "1.1_0",
        description = "This component reads data from MOD16 HDF files")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Fixed 0.1 bug, i.e. all "
            + "pixel values are multiplied with 0.1 now "
            + "(http://www.ntsg.umt.edu/project/mod16#documentation)")
})
public class HDFReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual time step"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "scheme for naming of hdf files e.g. MOD16A2.A%year%M%month%.*"
    )
    public Attribute.String namingScheme;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "value of missing data",
            defaultValue = "-9999"
    )
    public Attribute.Double missingDataValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "col of pixel"
    )
    public Attribute.DoubleArray cols;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "row of pixel"
    )
    public Attribute.DoubleArray rows;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "row of pixel"
    )
    public Attribute.DoubleArray weights;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of dataset to read from"
    )
    public Attribute.String datafield;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "value of pixel"
    )
    public Attribute.Double value;

    short actData[] = null;
    int width = -1;
    Attribute.Calendar lastTimeStep = null;

    DecimalFormat format = new DecimalFormat("00");

    private void openNextFile(String name, String dataset, Attribute.Calendar time) {
        name = name.replace("%year%", Integer.toString(time.get(Attribute.Calendar.YEAR)));
        name = name.replace("%month%", Integer.toString(time.get(Attribute.Calendar.MONTH) + 1));
        name = name.replace("%Month%", format.format(time.get(Attribute.Calendar.MONTH) + 1));
        name = name.replace("%day%", Integer.toString(time.get(Attribute.Calendar.DAY_OF_MONTH)));
        name = name.replace("%Day%", format.format(time.get(Attribute.Calendar.DAY_OF_MONTH)));
        name = name.replace("%hour%", Integer.toString(time.get(Attribute.Calendar.HOUR_OF_DAY)));
        name = name.replace("%Hour%", format.format(time.get(Attribute.Calendar.HOUR_OF_DAY)));

        int i1 = name.lastIndexOf("/");
        int i2 = name.lastIndexOf("\\");

        i1 = Math.max(i1, i2);
        File dir = this.getModel().getWorkspaceDirectory();
        String regex = name;
        if (i1 != -1) {
            String subDir = name.substring(0, i1);
            dir = new File(dir, subDir);
            regex = name.substring(i1 + 1);
        }
        File candidate = null;
        for (File f : dir.listFiles()) {
            if (f.getName().matches(regex)) {
                if (candidate != null) {
                    getModel().getRuntime().sendInfoMsg("Error in HDFReader: " + "There are multiple files for time step " + time.toString() + "\nPossible candidates are:" + candidate.getName() + "\nand\n" + f.getName());
                }
                candidate = f;
            }
        }
        if (candidate == null) {
            getModel().getRuntime().sendInfoMsg("Error in HDFReader: " + "There is no matching file for time step " + time.toString());
            actData = null;
            return;
        }

        readDataset(candidate, dataset);
    }

    public void run() {
        if (lastTimeStep == null || lastTimeStep.getTimeInMillis() != time.getTimeInMillis()) {
            openNextFile(this.namingScheme.getValue(), datafield.getValue(), time);
            lastTimeStep = time.clone();
        }
        double colArray[] = cols.getValue();
        double rowArray[] = rows.getValue();
        double wArray[] = weights.getValue();
        int v = 0;
        for (int i = 0; i < colArray.length; i++) {
            /*
             The users should multiply 0.1 to get the real ET/PET values in mm/8day or mm/month, or mm/yr, and 1.0e4 to get LE/PLE in J/m2/day.
             (http://www.ntsg.umt.edu/project/mod16#documentation)
             */
            v += wArray[i] * getValue((int) colArray[i], (int) rowArray[i]) * 0.1;
        }
        value.setValue(v);
    }

    public static void main(String[] args) {
        HDFReader reader = new HDFReader();
        reader.readDataset(new File("Y:/TFO/data/Spatial_Data/Modis_MOD16/408/h19v10_teil1/Y2001/M02/MOD16A2.A2001M02.h19v10.105.2013120140547.hdf"), "ET_1km");
    }

    // print out the data object recusively
    // return true if id was found
    private boolean getData(javax.swing.tree.TreeNode node, String id) throws HDFException {
        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) node.getChildAt(i);
            if (node2.getUserObject() instanceof H4SDS) {
                H4SDS sds = (H4SDS) node2.getUserObject();
                if (sds.getName().equals(id)) {
                    actData = (short[]) sds.read();
                    width = sds.getWidth();
                    return true;
                }
            }
            if (getData(node2, id)) {
                return true;
            }
        }
        return false;
    }

    private double getValue(int x, int y) {
        if (actData == null) {
            return jams.JAMS.getMissingDataValue();
        }
        double v = actData[y * width + x];
        if (v >= 20000) {
            return 0;
        }
        return v;
    }

    private void readDataset(File f, String dataset) {
        // Open an existing file.
        H4File h4file = new H4File(f.getAbsolutePath(), HDF5Constants.H5F_ACC_RDONLY);

        try {
            // open file and retrieve the file structure
            h4file.open();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TreeNode root = h4file.getRootNode();

        if (root != null) {
            try {
                boolean success = getData(root, dataset);//"ET_1km");
                if (!success) {
                    getModel().getRuntime().sendInfoMsg("Error in HDFReader. Could not find dataset " + dataset + " in file: " + f.getName());
                    actData = null;
                }
            } catch (HDFException ex) {
                ex.printStackTrace();
                getModel().getRuntime().sendInfoMsg("Error in HDFReader during read of file " + f.getName());
                actData = null;
            }
        }

        //System.out.println(value(478, 126));
        try {
            h4file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
