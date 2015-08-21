/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import optas.data.DataSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import optas.data.Efficiency;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

/**
 *
 * @author Christian Fischer
 */
public class DottyPlot3D extends MCAT5Plot {

    protected XYPlot plot = new XYPlot();
    protected PatchedChartPanel chartPanel = null;
    XYBlockRenderer bg_renderer = new XYBlockRenderer();

    private void init() {
        LookupPaintScale paintScale = new LookupPaintScale(0,1,new Color(0,0,0));
        for (int i=0;i<=255;i++){
            Color interpolated = new Color(i,0,255-i);
            paintScale.add((double)i / 255.0, interpolated);
        }
        //setup renderer
        bg_renderer.setPaintScale(paintScale);
        bg_renderer.setBlockWidth(1.00);
        bg_renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        //setup plot
        plot.setRenderer(bg_renderer);
        //setup chart
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("DOTTY_PLOT"));
        chartPanel = new PatchedChartPanel(chart, true);

        redraw();
    }

    public DottyPlot3D() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER")+"1", Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER")+"2", Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    public JPanel getPanel() {
        return this.chartPanel;
    }

    @Override
    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1,2});
        SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
        SimpleEnsemble p2 = (SimpleEnsemble) p[1].get(0);
        SimpleEnsemble p3 = (SimpleEnsemble) p[2].get(0);

        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(p2.getName()));

        

        DefaultXYZDataset xyz_dataset = new DefaultXYZDataset();

        int n = p1.getSize();

        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double zMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double zMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            xMin = Math.min(p1.getValue(p1.getId(i)),xMin);
            yMin = Math.min(p2.getValue(p2.getId(i)),yMin);
            zMin = Math.min(p3.getValue(p3.getId(i)),zMin);

            xMax = Math.max(p1.getValue(p1.getId(i)),xMax);
            yMax = Math.max(p2.getValue(p2.getId(i)),yMax);
            zMax = Math.max(p3.getValue(p3.getId(i)),zMax);
        }

        double dotMap[][] = new double[3][n];
        for (int i = 0; i < n; i++) {
            dotMap[0][i] = p1.getValue(p1.getId(i));
            dotMap[1][i] = p2.getValue(p2.getId(i));
            dotMap[2][i] = (p3.getValue(p3.getId(i)) - zMin)/(zMax-zMin);
        }

        xyz_dataset.addSeries(0, dotMap);

        bg_renderer.setBlockHeight((yMax - yMin) / 200.0);
        bg_renderer.setBlockWidth((xMax - xMin) / 200.0);

        plot.setDataset(0, xyz_dataset);

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setRange(p2.getMin(), p2.getMax());
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setRange(p1.getMin(), p1.getMax());
        }

    }
}
