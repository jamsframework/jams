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
import jams.data.NamedDataSupplier;
import jams.explorer.ensembles.api.Ensemble;
import jams.explorer.ensembles.api.Model;
import jams.io.ShapeFileOutputDataStore;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
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
    public File basePath = new File("");
            
    public class ClimateDataSupplier<T> extends ArrayDataSupplier<T> implements NamedDataSupplier<T> {
        String name;
        long entityIDs[];
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

        ClimateDataSupplier(String name, long entityIDs[], T values[]) {
            super(values);
            this.name = name;
            this.entityIDs = entityIDs;

            for (int i = 0; i < entityIDs.length; i++) {
                indexMap.put((int) entityIDs[i], i);
            }
        }

        @Override
        public String getName() {
            return ClimateDataSupplier.this.name;
        }

        public long[] getEntityIDs() {
            return entityIDs;
        }

        public T get(int id) {
            return this.input[indexMap.get(id)];
        }
    }

    public ClimateEnsemble() {
        super("");
    }

    public ClimateEnsemble(String name) {
        super(name);
    }

    public void setShapeFileTemplate(File f) {
        if (f == null){
            relativePathToShapeFileTemplate = null;
            return;
        }
        Path p_shape = f.toPath();
        Path p_base  = basePath.toPath();
        
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
        if (relativePathToShapeFileTemplate == null)
            return null;
        
        return new File(basePath, relativePathToShapeFileTemplate);
    }

    public void save(){
        for (ClimateModel model : modelSet){
            model.relocate(basePath);
            model.save();
        }
    }
    
    //set base path and adjust relative paths
    public void relocate(File newBasePath){               
        File shapeFileTemplate = getShapeFileTemplate();
        basePath = newBasePath;
        setShapeFileTemplate(shapeFileTemplate);
        
        for (ClimateModel model : modelSet){
            model.relocate(newBasePath);
        }
    }
    
    //set base path and do NOT adjust relative paths
    public void setBasePath(File newBasePath){
        this.basePath = newBasePath;
        
        for (ClimateModel model : modelSet){
            model.setBasePath(newBasePath);
        }
    }
    
    public void aggregateEnsembleToFile(File target, String output, 
            Aggregator.AggregationMode mode, Double modeParameter) throws IOException{  
        
        ClimateDataSupplier<Double>[] result = aggregateEnsemble(output, mode, modeParameter);
        
        String modeString = mode.toString();
        if (modeParameter != null && mode == Aggregator.AggregationMode.MEDIAN){
            modeString = "Q" + String.format("%.0f",modeParameter*100);
        }
        File targetDir = new File(target, "/ensemble/" + modeString + "/" + output);
        targetDir.mkdirs();
        
        ShapeFileOutputDataStore shpStore = new ShapeFileOutputDataStore(
                        new File(getShapeFileTemplate().getAbsolutePath()), targetDir);

        shpStore.addDataToShpFiles(result, "ID");        
    }
    
    public ClimateDataSupplier<Double>[] aggregateEnsemble(String output, Aggregator.AggregationMode mode, Double modeParameter) {        
        ClimateEnsembleProcessor proc = new ClimateEnsembleProcessor(this, output);
        
        try {
            proc.init();
            Attribute.Calendar dates[] = proc.getTimeDomain();
            long entityIDs[] = proc.getEntityIDs();

            int K = entityIDs.length;            
            int T = dates.length;

            ClimateDataSupplier[] result = new ClimateDataSupplier[T];

            for (int i = 0; i < T; i++) {
                double timeSlice[][] = proc.getTimeSlice(entityIDs, dates[i].toString());
                DoubleAggregator aggregator = DoubleAggregator.create(mode);

                Double tmp[] = new Double[K];

                for (int j = 0; j < K; j++) {
                    aggregator.init();
                    for (double[] timeSlice1 : timeSlice) {
                        aggregator.consider(timeSlice1[j]);
                    }
                    aggregator.finish();
                    double v = aggregator.get();
                    if (modeParameter != null && aggregator instanceof DoubleAggregator.MedianAggregator) {
                        v = ((DoubleAggregator.MedianAggregator) aggregator).getQuantile(modeParameter);
                    }
                    tmp[j] = v;
                }
                result[i] = new ClimateDataSupplier<Double>(dates[i].toString(), entityIDs, tmp);
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
