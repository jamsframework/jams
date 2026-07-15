/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.implementation;

import jams.aggregators.Aggregator;
import jams.aggregators.DoubleAggregator;
import jams.data.ArrayDataSupplier;
import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import jams.data.NamedDataSupplier;
import jams.explorer.ensembles.api.Ensemble;
import jams.explorer.ensembles.api.Model;
import jams.io.ShapeFileOutputDataStore;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author christian
 */
@XmlRootElement(name = "ClimateEnsemble")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClimateEnsemble extends AbstractEnsemble<ClimateModel> {

    @XmlTransient
    DefaultTreeModel treeModel = null;

    String relativePathToShapeFileTemplate;

    @XmlTransient
    public File basePath = new File("").getAbsoluteFile();

    public class ClimateDataSupplier<T> extends ArrayDataSupplier<T> implements NamedDataSupplier<T> {

        String name;
        long entityIDs[];
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

        ClimateDataSupplier(ClimateDataSupplier<T> parent) {
            super(parent.input);
            this.name = parent.name;
            this.indexMap = parent.indexMap;
            this.entityIDs = parent.entityIDs;
        }

        ClimateDataSupplier(String name, long entityIDs[], T values[]) {
            super(values);
            this.name = name;
            this.entityIDs = entityIDs;

            for (int i = 0; i < entityIDs.length; i++) {
                indexMap.put((int) entityIDs[i], i);
            }
        }

        public ClimateDataSupplier copy(String name, T values[]) {
            ClimateDataSupplier<T> copy = new ClimateDataSupplier<T>(this);
            copy.name = name;
            copy.input = values;
            return copy;
        }

        @Override
        public String getName() {
            return ClimateDataSupplier.this.name;
        }

        public long[] getEntityIDs() {
            return entityIDs;
        }

        @Override
        public T get(int id) {
            Integer i = indexMap.get(id);
            if (i == null) {
                return null;
            }
            return this.input[i];
        }
    }

    public ClimateEnsemble() {
        super("");
    }

    public ClimateEnsemble(String name) {
        super(name);
    }

    public void setShapeFileTemplate(File f) {
        if (f == null) {
            relativePathToShapeFileTemplate = null;
            return;
        }
        Path p_shape = f.toPath().toAbsolutePath();
        Path p_base = basePath.toPath().toAbsolutePath();;

        String relativePath = p_base.relativize(p_shape).toString();
        this.relativePathToShapeFileTemplate = relativePath;
    }

    public void setRelativePathToShapeFileTemplate(String f) {
        this.relativePathToShapeFileTemplate = f;
    }

    public String getRelativePathToShapeFileTemplate() {
        return relativePathToShapeFileTemplate;
    }

    public File getShapeFileTemplate() {
        if (relativePathToShapeFileTemplate == null) {
            return null;
        }

        return new File(basePath.getAbsoluteFile(), relativePathToShapeFileTemplate);
    }

    public void save() {
        for (ClimateModel model : modelSet) {
            model.relocate(basePath);
            model.save();
        }
    }

    //set base path and adjust relative paths
    public void relocate(File newBasePath) {
        File shapeFileTemplate = getShapeFileTemplate();
        basePath = newBasePath;
        setShapeFileTemplate(shapeFileTemplate);

        for (ClimateModel model : modelSet) {
            model.relocate(newBasePath);
        }
    }

    //set base path and do NOT adjust relative paths
    public void setBasePath(File newBasePath) {
        this.basePath = newBasePath;

        for (ClimateModel model : modelSet) {
            model.setBasePath(newBasePath);
        }
    }

    public void aggregateEnsembleToFile(File target, String output,
            Aggregator.AggregationMode mode, Double modeParameter, TimeInterval refPeriod) throws IOException {

        ClimateDataSupplier<Double>[] result = aggregateEnsemble(output, mode, modeParameter, refPeriod);

        String modeString = mode.toString();
        if (modeParameter != null && mode == Aggregator.AggregationMode.MEDIAN) {
            modeString = "Q" + String.format("%.0f", modeParameter * 100);
        }
        File targetDir = new File(target, "/ensemble/" + modeString + "/" + output);
        targetDir.mkdirs();

        ShapeFileOutputDataStore shpStore = new ShapeFileOutputDataStore(
                new File(getShapeFileTemplate().getAbsolutePath()), targetDir);

        List<String> fieldNames = Arrays.asList(shpStore.getFieldNames());
        if (fieldNames.contains("ID")) {
            shpStore.addDataToShpFiles(result, "ID");
        } else if (fieldNames.contains("OBJECTID")) {
            shpStore.addDataToShpFiles(result, "OBJECTID");
        } else {
            throw new IOException("Unknown ID field name!");
        }
    }

    private String date(){
        return "[" + (new Date()).getTime() + "]";
    }
    
    public ClimateDataSupplier<Double>[] aggregateEnsemble(String output, Aggregator.AggregationMode mode, Double modeParameter, TimeInterval refPeriod) {
        System.out.println(date() + ": Start");
        ClimateEnsembleProcessor proc = new ClimateEnsembleProcessor(this, output);

        try {
            System.out.println(date() + ": Init");
            proc.init();
            System.out.println(date() + ": getTimeDomain");
            Attribute.Calendar dates[] = proc.getTimeDomain();
            System.out.println(date() + ": getEntityIDs");
            long entityIDs[] = proc.getEntityIDs();

            int K = entityIDs.length;
            int T = dates.length;
            int m = this.getModelSet().size();

            ClimateDataSupplier[] result = new ClimateDataSupplier[T];

            double refMean[][] = new double[m][K];

            
            if (refPeriod != null) {
                System.out.println(date() + ": Working on reference period!");
                int k=0;
                for (ClimateModel model : proc.procs.keySet()) {
                    System.out.println(date() + ":Working on model " + model.toString());
                    double modelSlice[][] = proc.getModelSlice(model);
                    System.out.println("Working on entities!");
                    for (int j = 0; j < K; j++) {                        
                        DoubleAggregator aggregator = DoubleAggregator.create(Aggregator.AggregationMode.AVERAGE);
                        aggregator.init();
                        for (int i = 0; i < T; i++) {
                            if (dates[i].after(refPeriod.getStart()) && dates[i].before(refPeriod.getEnd()))                            
                                aggregator.consider(modelSlice[i][j]);
                        }
                        aggregator.finish();
                        refMean[k][j] = aggregator.get();
                    }
                    k++;
                }                
            }

            System.out.println("Working on full time period!");
            for (int i = 0; i < T; i++) {
                System.out.println("Working on timestep " + i + " of " + T);
                double timeSlice[][] = proc.getTimeSlice(entityIDs, dates[i].toString());
                DoubleAggregator aggregator = DoubleAggregator.create(mode);

                if (modeParameter != null && aggregator instanceof DoubleAggregator.IndexAggregator) {
                    ((DoubleAggregator.IndexAggregator)aggregator).setSelectionIndex(modeParameter.intValue());
                }
                Double tmp[] = new Double[K];

                for (int j = 0; j < K; j++) {
                    aggregator.init();
                    for (int k=0;k<timeSlice.length;k++) { //iteration of all models
                        aggregator.consider(timeSlice[k][j] - refMean[k][j]);
                    }
                    aggregator.finish();
                    double v = 0;
                    if (modeParameter != null && aggregator instanceof DoubleAggregator.MedianAggregator) {
                        v = ((DoubleAggregator.MedianAggregator) aggregator).getQuantile(modeParameter);
                    } else {
                        v = aggregator.get();
                    }
                    tmp[j] = v;
                }
                if (i == 0) {
                    result[i] = new ClimateDataSupplier<Double>(dates[i].toString(), entityIDs, tmp);
                } else {
                    result[i] = result[i - 1].copy(dates[i].toString(), tmp);
                }
            }

            return result;
        } catch (Throwable e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Sorry, I failed to perform the aggregation!", e);
        } finally {
            try {
                proc.close();
            } catch (SQLException sqle) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Sorry, I failed to perform the aggregation!", sqle);
            }
        }
        return null;
    }

    /*public ClimateDataSupplier<Double>[] aggregateEnsemble(String output, Aggregator.AggregationMode mode, Double modeParameter) {
        return aggregateEnsemble(output, mode, modeParameter, null);
    }*/
        
    public TreeModel getTreeModel() {
        DefaultMutableTreeNode root = new EnsembleTreeNode(this);

        for (ClimateModel model : this.modelSet) {
            ModelTreeNode modelNode = new ModelTreeNode(model);
            root.add(modelNode);
            for (String outputDir : model.getOutputs()) {
                OutputDirectoryTreeNode dirNode = new OutputDirectoryTreeNode(model, outputDir);
                modelNode.add(dirNode);
            }
        }

        treeModel = new DefaultTreeModel(root);
        return treeModel;
    }

    public class ModelTreeNode extends DefaultMutableTreeNode {

        ModelTreeNode(ClimateModel model) {
            super(model);
            model.removeAllModelDataChangeListener();
            model.addModelDataChangeListener(new Model.ModelDataChangeListener() {

                @Override
                public void changed(Model model, String key) {
                    ModelTreeNode.this.setUserObject(model);
                    treeModel.nodeChanged((TreeNode) treeModel.getRoot());
                }
            });
        }

        public ClimateModel getModel() {
            return (ClimateModel) ModelTreeNode.this.getUserObject();
        }
    }

    public class EnsembleTreeNode extends DefaultMutableTreeNode {

        EnsembleTreeNode(Ensemble e) {
            super(e);
        }

        public ClimateEnsemble getEnsemble() {
            return (ClimateEnsemble) getUserObject();
        }
    }

    public class OutputDirectoryTreeNode extends DefaultMutableTreeNode {

        ClimateModel model;

        OutputDirectoryTreeNode(ClimateModel model, String o) {
            super(o);
            this.model = model;
        }

        public ClimateModel getModel() {
            return model;
        }

        public String getOutputDirectory() {
            return (String) getUserObject();
        }
    }

    public class OutputTreeNode extends DefaultMutableTreeNode {

        ClimateModel model;

        OutputTreeNode(ClimateModel model, File o) {
            super(o);
            this.model = model;
        }

        public ClimateModel getModel() {
            return model;
        }

        public File getOutput() {
            return (File) getUserObject();
        }

        @Override
        public String toString() {
            return ((File) getUserObject()).getName();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
