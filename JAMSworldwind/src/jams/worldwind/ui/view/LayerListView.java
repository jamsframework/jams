package jams.worldwind.ui.view;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygons;
import jams.worldwind.handler.LayerListItemTransferHandler;
import jams.worldwind.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.UIEvents;
import jams.worldwind.ui.model.GlobeModel;
import jams.worldwind.ui.model.LayerListModel;
import jams.worldwind.ui.renderer.LayerListRenderer;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class LayerListView implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(LayerListView.class);
    private JFrame theFrame;
    private JList layers;
    private LayerListModel layerModel;
    private GlobeModel globeModel;

    public LayerListView(GlobeModel gm) {
        this.globeModel = gm;
        this.globeModel.getWorldWindow().addSelectListener(new SelectListener() {
            protected Object lastObject;

            @Override
            public void selected(SelectEvent event) {
                Object o = event.getTopObject();
                if (lastObject != o && o!=null) {
                    lastObject = o;
                    if (o instanceof SurfacePolygons) {
                        SurfacePolygons s = (SurfacePolygons)o;
                        
                        System.out.println("AREA: " + s.getArea(globeModel.getModel().getGlobe()));
                        
                        JamsShapeAttributes bs = (JamsShapeAttributes)s.getAttributes();
                        System.out.println(bs.getShapefileRecord().getAttributes().getEntries());
                        Material material = new Material(Color.RED);
                        bs.setOutlineMaterial(material);
                    }
                }
            }
        });
        this.layerModel = new LayerListModel(globeModel);
        theFrame = new JFrame("Layers");
        layers = new JList(layerModel);

        layers.setDragEnabled(
                true);
        layers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        layers.setDropMode(DropMode.INSERT);

        layers.setTransferHandler(
                new LayerListItemTransferHandler(layerModel));

        theFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        theFrame.setLayout(
                new GridLayout(1, 1));

        JScrollPane scrollPane = new JScrollPane(layers);

        scrollPane.setBorder(
                new TitledBorder("Available Layers"));


        layers.setCellRenderer(
                new LayerListRenderer());
        layers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a mouse listener to handle changing selection
        layers.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();

                // Get index of item clicked
                int index = list.locationToIndex(event.getPoint());
                Layer item = (Layer) list.getModel().getElementAt(index);

                // Toggle selected state
                item.setEnabled(!item.isEnabled());
                Layer l = globeModel.getWorldWindow().getModel().getLayers().getLayerByName(item.getName());
                System.out.println(l.getEntries());
                if (l != null) {
                    l.setEnabled(item.isEnabled());
                } else {
                    logger.error("Clicked layer not found at WorldWind model!");
                }
                globeModel.getWorldWindow().redraw();
                // Repaint cell
                list.repaint(list.getCellBounds(index, index));
            }
        });

        theFrame.add(scrollPane);

        theFrame.setSize(
                200, 600);
        this.globeModel.addPropertyChangeListener(
                this);
    }

    public void updateLayerListView() {
        layerModel.update();
    }

    public void updateModel() {
        layerModel.updateWorldWind();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.info("Recieving Event: " + evt.getPropertyName());
        if (evt.getPropertyName().equals(UIEvents.LAYER_CHANGE)) {
            this.layerModel.update();
            this.layers.revalidate();
            this.layers.repaint();
        }
    }

    void show(boolean b) {
        theFrame.setVisible(b);
    }
}