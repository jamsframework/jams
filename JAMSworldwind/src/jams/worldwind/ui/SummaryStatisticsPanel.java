package jams.worldwind.ui;

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class SummaryStatisticsPanel extends JPanel {

    private DescriptiveStatistics statistics;
    private JLabel[] statisticLabels;

    public SummaryStatisticsPanel() {
        statistics = new DescriptiveStatistics();
        this.createGUI();
    }

    private void createGUI() {
        this.setLayout(new GridLayout(0,2,1,1));
        this.setBorder(new TitledBorder("Classification Statistics"));
        this.statisticLabels = new JLabel[16];
        for (int i = 0; i < this.statisticLabels.length; i++) {
            this.statisticLabels[i] = new JLabel();
            if (i % 2 == 1) {
                this.statisticLabels[i].setHorizontalAlignment(JLabel.RIGHT);
            }
        }
        this.statisticLabels[0].setText("count:");
        this.statisticLabels[2].setText("minimum:");
        this.statisticLabels[4].setText("maximum:");
        this.statisticLabels[6].setText("sum:");
        this.statisticLabels[8].setText("mean:");
        this.statisticLabels[10].setText("median:");
        this.statisticLabels[12].setText("std. deviation:");
        this.statisticLabels[14].setText("variance:");
        for (JLabel statisticLabel : this.statisticLabels) {
            this.add(statisticLabel);
        }
    }

    public void calculateStatistics(double[] values) {
        statistics.clear();
        for (int i = 0; i < values.length; i++) {
            statistics.addValue(values[i]);
        }
        this.updateLabels();
    }

    /*
    public void calculateStatistics(List values) {
        statistics.clear();
        for (int i = 0; i < values.size(); i++) {
            statistics.addValue((double) values.get(i));
        }
        this.updateLabels();
    }
    */

    private void updateLabels() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(3);
        this.statisticLabels[1].setText(nf.format(statistics.getN()));
        this.statisticLabels[3].setText(nf.format(statistics.getMin()));
        this.statisticLabels[5].setText(nf.format(statistics.getMax()));
        this.statisticLabels[7].setText(nf.format(statistics.getSum()));
        this.statisticLabels[9].setText(nf.format(statistics.getMean()));
        this.statisticLabels[11].setText(nf.format(statistics.getPercentile(50)));
        this.statisticLabels[13].setText(nf.format(statistics.getStandardDeviation()));
        this.statisticLabels[15].setText(nf.format(statistics.getVariance()));
    }

    public DescriptiveStatistics getStatistics() {
        return this.statistics;
    }
}
