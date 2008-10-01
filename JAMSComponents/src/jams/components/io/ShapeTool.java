package jams.components.io;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeature;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import jams.data.JAMSString;

import com.vividsolutions.jts.geom.MultiPolygon;

public class ShapeTool extends JPanel {
	
	public JButton export; 

	private File fileToSave;

	private File dbfFile;

	private ShapefileDataStore store;

	private java.net.URL baseShapeUrl;

	private String shpIdColName;

	private String addAttr;

	private Object[][] tabledata;

	private Set<String> list;

	private JTable table;

	public JTextField FileNameField;

	private CollectionDataStore f;

	class AttributeSelectList extends JPanel {
		class ColoredTableCellRenderer extends DefaultTableCellRenderer {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				super.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);
				if (isSelected)
					setForeground(java.awt.Color.black);
				if (row == 0) {
					setBackground(Color.lightGray);
				} else {
					setBackground(table.getBackground());
				}
				return this;
			}
		}

		public AttributeSelectList() throws IOException {
			String[] columnNames = { " ", "Attribute" };
			AttributeType[] shpAtts = store.getSchema(store.getTypeNames()[0])
					.getAttributeTypes();
			tabledata = new Object[shpAtts.length][2];
			list = new TreeSet<String>();
			for (int i = 0; i < shpAtts.length; i++) {
				list.add(shpAtts[i].getName());
			}
			list.remove("the_geom");
			tabledata[0][0] = Boolean.TRUE;
			tabledata[0][1] = addAttr.toUpperCase();
			int i = 1;
			for (Iterator iterate = list.iterator(); iterate.hasNext();) {
				tabledata[i][0] = Boolean.FALSE;
				tabledata[i][1] = new String(iterate.next().toString());
				i++;
			}
			table = new JTable(tabledata, columnNames) {
				public Class getColumnClass(int column) {
					return getValueAt(0, column).getClass();
				}
			};
			DefaultTableCellRenderer ren = new ColoredTableCellRenderer();
			table.setDefaultRenderer(String.class, ren);
			TableColumn col = table.getColumnModel().getColumn(0);
			col.setPreferredWidth(1);
			table.setPreferredScrollableViewportSize(new Dimension(130, 250));
			JScrollPane scrollPane = new JScrollPane(table);
			this.add(scrollPane);
		}
	}

	class OpenFile implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			fc.setFileFilter(new FileFilter() {
				public String getDescription() {
					return "Shapefile (*.shp)";
				}

				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".shp");
				}
			});
			fc.showSaveDialog(null);
			fileToSave = fc.getSelectedFile();
			FileNameField.setText(fileToSave.getName()+".shp");
			export.setEnabled(true);
		}
	}

	class GenerateShape implements ActionListener {
		
		JSplitPane panel;
		JScrollPane tree;
		
		GenerateShape(ShapeTool s, JSplitPane p, JScrollPane t) {
			panel = p;
			tree = t;
		}

		public void actionPerformed(ActionEvent arg0) {
			ArrayList<String> attlist = new ArrayList<String>();
			for (int i = 0; i <= list.size(); i++) {
				if ((Boolean) table.getValueAt(i, 0)) {
					attlist.add(table.getValueAt(i, 1).toString());
				}
			}
			try {
				java.net.URL saveUrl = fileToSave.toURI().toURL();
				ShapefileDataStore newdatastore = new ShapefileDataStore(
						saveUrl);
				DbaseFileHeader dbfh = new DbaseFileHeader();
				dbfh.readHeader(new FileInputStream(dbfFile).getChannel());
				HashMap<String, Class> types = new HashMap<String, Class>();
				for (int i = 0; i < dbfh.getNumFields(); i++) {
					if (attlist.contains(dbfh.getFieldName(i))) {
						types.put(dbfh.getFieldName(i), dbfh.getFieldClass(i));
					}
				}
				
				
				FeatureTypeBuilder userFType = FeatureTypeBuilder
						.newInstance("userFType");
				GeometryAttributeType geo = (GeometryAttributeType) AttributeTypeFactory
						.newAttributeType("geo", MultiPolygon.class);
				userFType.addType(geo);
				for (int i = 0; i < attlist.size(); i++) {
					AttributeType newAt = AttributeTypeFactory
							.newAttributeType(attlist.get(i), types.get(attlist.get(i)));
					userFType.addType(newAt);
				}
				newdatastore.createSchema(userFType.getFeatureType());

				FeatureWriter fw = newdatastore.getFeatureWriter("userFType",
						Transaction.AUTO_COMMIT);
				HashMap<Long, Object> h = new HashMap<Long, Object>();
				FeatureIterator fIter = f.getCollection().features();
				while (fIter.hasNext()) {
					Feature q = fIter.next();
					h.put(Long.valueOf(q.getID()), q.getAttribute("newAt"));
				}
				ShapefileDataStore store = new ShapefileDataStore(baseShapeUrl);

				Iterator reader = store.getFeatureSource(
						store.getTypeNames()[0]).getFeatures().iterator();

				while (reader.hasNext()) {
					Feature f = (Feature) reader.next();
					DefaultFeature a = (DefaultFeature) fw.next();
					a.setAttribute("geo", f.getDefaultGeometry());
					a.setAttribute(attlist.get(0), h.get((Long) f
							.getAttribute(shpIdColName)));
					if (attlist.size() > 1) {
						for (int j = 1; j < attlist.size(); j++) {
							a.setAttribute(attlist.get(j), f
									.getAttribute(attlist.get(j)));
						}
					}
					fw.write();
				}
				fw.close();
				panel.setDividerLocation(0.80);
				panel.setTopComponent(tree);

			} catch (MalformedURLException e) {} 
			  catch (IOException e) {} 
			  //catch (ClassNotFoundException e) {} 
			  catch (FactoryRegistryException e) {} 
			  catch (SchemaException e) {}
			  catch (NoSuchElementException e) {} 
			  catch (IllegalAttributeException e) {}
			  catch (Exception e) {}
		}
	}

	public ShapeTool(CollectionDataStore features, JAMSString Shapefile,
			String newAttr, final JSplitPane panel, final JScrollPane tree)
			throws Exception {

		baseShapeUrl = (new java.io.File(Shapefile.toString().split(";")[0])
				.toURI().toURL());
		dbfFile = (new java.io.File(Shapefile.toString().split(";")[0]
				.substring(0, Shapefile.toString().split(";")[0].length() - 4)
				+ ".dbf"));
		shpIdColName = Shapefile.toString().split(";")[1];
		addAttr = newAttr;
		store = new ShapefileDataStore(baseShapeUrl);
		f = features;

		this.setLayout(new BorderLayout());
		this.add(new AttributeSelectList(), BorderLayout.CENTER);

		JPanel l = new JPanel();
		FileNameField = new JTextField("", 10);
		FileNameField.setEditable(false);
		l.add(FileNameField);

		URL url = this.getClass().getResource("resources/ordner.gif");
		ImageIcon icon = new ImageIcon(url);
		JButton open = new JButton(icon);

		open.addActionListener(new OpenFile());
		l.add(open);

		this.add(l, BorderLayout.NORTH);

		JPanel b = new JPanel();
		JButton cancel = new JButton("Cancel");
		export = new JButton("Export");
		export.setEnabled(false);
		b.add(cancel);
		b.add(export);
		export.addActionListener(new GenerateShape(this, panel, tree));
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				panel.setDividerLocation(0.80);
				panel.setTopComponent(tree);
			}

		});
		this.add(b, BorderLayout.SOUTH);
		this.setVisible(true);
	}
}
