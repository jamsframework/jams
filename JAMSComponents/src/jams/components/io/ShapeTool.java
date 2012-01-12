/*
 * ShapeTool.java
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.components.io;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.feature.FeatureIterator;
import jams.data.JAMSString;

import com.vividsolutions.jts.geom.MultiPolygon;
import jams.components.gui.MapCollection;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Class used by MapCreator to save maps (MapContext) as shape files.
 *
 * @author C. Schwartze
 */
public class ShapeTool extends JPanel {
	
    private File fileToSave, dbfFile;
    private ShapefileDataStore store;
    private java.net.URL baseShapeUrl;
    private String shpIdColName, addAttr;
    private Set<String> list;
    private CollectionDataStore feat;
    private JPanel boxes;

    private JPanel getShpAtts() throws IOException {

        boxes = new JPanel();
        boxes.setLayout(new BoxLayout(boxes, BoxLayout.Y_AXIS));

        List<AttributeDescriptor> shpAtts = store.getSchema().getAttributeDescriptors();
        int sz = shpAtts.size();

        list = new TreeSet<String>();
        for (int i = 0; i < sz; i++) {
            list.add(shpAtts.get(i).getName().toString());
        }
        String geoAtt = store.getSchema().getGeometryDescriptor().getName().toString();
        list.remove(geoAtt);
        JCheckBox addAttrCB = new JCheckBox(addAttr, true);
        addAttrCB.setEnabled(false);
        boxes.add(addAttrCB);
        int i = 1;
        for (Iterator iterate = list.iterator(); iterate.hasNext();) {
            boxes.add(new JCheckBox(iterate.next().toString()));
            i++;
        }
        return boxes;
    }

	public ShapeTool(MapCollection features, JAMSString Shapefile, final JSplitPane panel)
			throws Exception {

		baseShapeUrl = (new java.io.File(Shapefile.toString().split(";")[0])
				.toURI().toURL());
		dbfFile = (new java.io.File(Shapefile.toString().split(";")[0]
				.substring(0, Shapefile.toString().split(";")[0].length() - 4)
				+ ".dbf"));
		shpIdColName = Shapefile.toString().split(";")[1];
		store = new ShapefileDataStore(baseShapeUrl);
        addAttr = features.getDesc();
		feat = features.asCollectionDataStore();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel t = new JPanel();
        JLabel title = new JLabel("Export to *.SHP:");
		t.add(title);
        this.add(t);

        JScrollPane sc = new JScrollPane(getShpAtts());
        this.add(sc);

		JPanel b = new JPanel();
		JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int div = ((JSplitPane) ShapeTool.this.getParent()).getDividerLocation();
                ((JSplitPane) ShapeTool.this.getParent()).setRightComponent(panel);
                ((JSplitPane) panel.getParent()).setDividerLocation(div);
            }
        });
        
		JButton export = new JButton("Save");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogType(JFileChooser.SAVE_DIALOG);
                    fc.setFileFilter(new FileFilter() {

                        @Override
                        public String getDescription() {
                            return "Shapefile (*.shp)";
                        }

                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory() || f.getName().toLowerCase().endsWith(".shp");
                        }
                    });
                    fc.showSaveDialog(null);
                    if (!fc.getSelectedFile().getAbsolutePath().endsWith(".shp")) {
                        fileToSave = new File(fc.getSelectedFile().getAbsolutePath()+".shp");
                    }
                    else fileToSave = fc.getSelectedFile();

                    CoordinateReferenceSystem crs = store.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();

                    DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
                    Map<String, Serializable> create = new HashMap<String, Serializable>();
                    create.put("url", fileToSave.toURI().toURL());
                    create.put("create spatial index", Boolean.TRUE);
                    DataStore ds = factory.createNewDataStore(create);

                    DbaseFileHeader header = new DbaseFileHeader();
                    header.readHeader(new FileInputStream(dbfFile).getChannel());

                    SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
                    sftb.setName("mapFType");
                    sftb.setCRS(crs);
                    sftb.add("geo", MultiPolygon.class);

                    Component[] comps = boxes.getComponents();
                    List attList = new ArrayList();
                    for (int i = 0; i < comps.length; i++) {
                        final Component comp = comps[i];
                        if (((JCheckBox) comp).isSelected()) {
                            attList.add(comp);
                        }
                    }
                    HashMap<String, Class> types = new HashMap<String, Class>();
                    types.put(addAttr, Double.class);
                    for (int i = 0; i < header.getNumFields(); i++) {
                        types.put(header.getFieldName(i), header.getFieldClass(i));
                    }

                    for (Iterator iterate = attList.iterator(); iterate.hasNext();) {
                        JCheckBox cb = (JCheckBox) iterate.next();
                        sftb.add(cb.getText(), types.get(cb.getText()));
                    }
                    
                    SimpleFeatureType mapFType = sftb.buildFeatureType();
                    ds.createSchema(mapFType);

                    FeatureWriter fw = ds.getFeatureWriter("mapFType", Transaction.AUTO_COMMIT);
                    FeatureIterator<SimpleFeature> reader = store.getFeatureSource().getFeatures().features();

                    HashMap<Long, Object> h = new HashMap<Long, Object>();
                    FeatureIterator<SimpleFeature> it = feat.getCollection().features();
                    while (it.hasNext()) {
                        SimpleFeature q = it.next();
                        h.put(Long.valueOf(q.getID()), q.getAttribute("newAt"));
                    }

                    while (reader.hasNext()) {
                        SimpleFeature f = reader.next();
                        SimpleFeature w = (SimpleFeature) fw.next();
                        w.setAttribute("geo", f.getDefaultGeometry());
                        Iterator iterate = attList.iterator();
                        w.setAttribute(((JCheckBox)iterate.next()).getText(), h.get(Long.valueOf(f.getAttribute(shpIdColName).toString())));
                        while (iterate.hasNext()) {
                            String s = ((JCheckBox)iterate.next()).getText();
                            w.setAttribute(s, f.getAttribute(s));
                        } 
                        fw.write();
                    }
                    fw.close();

                    JOptionPane.showMessageDialog(null, "Shapefile "+fileToSave.getName()+" created!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
                int div = ((JSplitPane)ShapeTool.this.getParent()).getDividerLocation();
				((JSplitPane)ShapeTool.this.getParent()).setRightComponent(panel);
                ((JSplitPane)panel.getParent()).setDividerLocation(div);
            }
        });

        b.add(cancel);
        b.add(export);
		this.add(b);
		this.setVisible(true);
	}
}
