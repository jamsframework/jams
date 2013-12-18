package jams.worldwind.ui;

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class SummaryStatisticsPanel extends JPanel {

    private DescriptiveStatistics stats;
    private JLabel[] statisticLabels;

    private void SummaryStatisticsPanel() {
        stats = new DescriptiveStatistics();
        this.createGUI();
    }

    public SummaryStatisticsPanel(double[] values) {
        this.SummaryStatisticsPanel();
        this.calculateStatistics(values);
    }

    public SummaryStatisticsPanel(List values) {
        this.SummaryStatisticsPanel();
        this.calculateStatistics(values);
    }

    private void createGUI() {
        this.setBorder(new TitledBorder("CLASSIFICATION STATISTICS"));
        this.setLayout(new GridLayout(8, 2));

        this.statisticLabels = new JLabel[16];
        for (int i = 0; i < this.statisticLabels.length; i++) {
            this.statisticLabels[i] = new JLabel();
        }

        this.statisticLabels[0].setText("COUNT:          ");
        this.statisticLabels[2].setText("MINIMUM:        ");
        this.statisticLabels[4].setText("MAXIMUM:        ");
        this.statisticLabels[6].setText("SUM:            ");
        this.statisticLabels[8].setText("MEAN:           ");
        this.statisticLabels[10].setText("MEDIAN:         ");
        this.statisticLabels[12].setText("STD. DEVIATION: ");
        this.statisticLabels[14].setText("VARIANCE:       ");

        for (JLabel statisticLabel : this.statisticLabels) {
            this.add(statisticLabel);
        }

    }

    private void calculateStatistics(double[] values) {
        for (int i = 0; i < values.length; i++) {
            stats.addValue(values[i]);
        }
        this.printStatistics();
    }

    private void calculateStatistics(List values) {
        for (int i = 0; i < values.size(); i++) {
            stats.addValue((double) values.get(i));
        }
        this.printStatistics();
    }

    private void printStatistics() {
        NumberFormat nf = NumberFormat.getInstance();
        this.statisticLabels[1].setText(nf.format(stats.getN()));
        this.statisticLabels[3].setText(nf.format(stats.getMin()));
        this.statisticLabels[5].setText(nf.format(stats.getMax()));
        this.statisticLabels[7].setText(nf.format(stats.getSum()));
        this.statisticLabels[9].setText(nf.format(stats.getMean()));
        this.statisticLabels[11].setText(nf.format(stats.getPercentile(50)));
        this.statisticLabels[13].setText(nf.format(stats.getStandardDeviation()));
        this.statisticLabels[15].setText(nf.format(stats.getVariance()));
        stats.clear();
    }

    /*
    public void updateStatistics(List values) {
        this.calculateStatistics(values);
    }
    */
}
