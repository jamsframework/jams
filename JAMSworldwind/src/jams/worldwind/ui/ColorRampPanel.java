package jams.worldwind.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class ColorRampPanel extends JPanel {

    private class ColorPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            if (numberOfColors > 0 && startColor != null && endColor != null) {
                colorRamp = new ColorRamp(startColor, endColor, numberOfColors);
                int startx, xsteps;
                xsteps = this.getWidth() / colorRamp.getSteps();
                startx = 0;
                for (int i = 0; i < colorRamp.getSteps(); i++) {
                    g.setColor(colorRamp.getColorRamp().get(i));
                    g.fillRect(startx, 0, xsteps, colorPanel.getHeight());
                    g.setColor(Color.black);
                    g.drawLine(startx, 0, startx, colorPanel.getHeight());
                    startx += xsteps;
                }
            }
        }
    }

    private final JButton startColorButton;
    private final JButton endColorButton;
    private Color startColor, endColor;
    private final JColorChooser colorChooser;
    private JDialog dialog;
    private final ColorPanel colorPanel;
    private ColorRamp colorRamp;
    private int numberOfColors;

    private final String startColorAction = "Start Color";
    private final String endColorAction = "End Color";

    public ColorRampPanel() {
        this.colorRamp = null;
        colorChooser = new JColorChooser();
        this.startColorButton = new JButton(startColorAction);
        this.startColorButton.setActionCommand(startColorAction);
        this.startColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonActionListener(e);
            }
        });
        this.startColor = Color.WHITE;
        this.startColorButton.setBackground(this.startColor);
        this.startColorButton.setEnabled(false);
        this.endColorButton = new JButton(endColorAction);
        this.endColorButton.setActionCommand(endColorAction);
        this.endColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonActionListener(e);
            }
        });
        this.endColor = Color.BLUE;
        this.endColorButton.setBackground(this.endColor);
        this.endColorButton.setEnabled(false);
        
        this.colorPanel = new ColorPanel();
        this.setOpaque(true);

        GridBagLayout gbl = new GridBagLayout();
        this.setLayout(gbl);

        this.addComponent(this, gbl, startColorButton, 0, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, endColorButton, 1, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, colorPanel, 0, 1, 2, 1, 1.0, 0);
    }

    public void setColorRamp(ColorRamp colorRamp) {
        this.colorRamp = colorRamp;
        this.numberOfColors = colorRamp.getSteps()-1;
        this.colorPanel.repaint();
    }
    
    public void setNumberOfColors(int numberOfColors) {
        this.numberOfColors = numberOfColors;
        this.setButtonsEnabled(true);
    }

    public void setButtonsEnabled(boolean enabled) {
        this.startColorButton.setEnabled(enabled);
        this.endColorButton.setEnabled(enabled);
        if (!enabled) {
            this.colorPanel.setBackground(this.getBackground());
        }
    }

    public ColorRamp getColorRamp() {
        return this.colorRamp;
    }
    
    private void addComponent(Container container,
            GridBagLayout gbl,
            Component c,
            int x, int y,
            int width, int height,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        container.add(c);
    }

    private void buttonActionListener(ActionEvent e) {
        
        switch (e.getActionCommand()) {
            case startColorAction:
                this.startColor = JColorChooser.showDialog(this, "Pick a Color", startColor);
                this.startColorButton.setBackground(this.startColor);
                break;
            case endColorAction:
                this.endColor = JColorChooser.showDialog(this, "Pick a Color", endColor);
                this.endColorButton.setBackground(this.endColor);
                break;
        }
        if (this.startColor != null && this.endColor != null) {
            this.colorPanel.repaint();
        }
    }
}
