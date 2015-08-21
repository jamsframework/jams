/*
 * JAMSExecInfo.java
 * Created on 1. Dezember 2005, 19:46
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
 *
 */
package jams.components.gui;

import jams.model.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import jams.JAMS;
import jams.data.Attribute;
import jams.runtime.JAMSRuntime;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title = "JAMS execution info frame",
        author = "Sven Kralisch",
        date = "17. June 2006",
        description = "This visual component creates a panel with progress bar and log information",
        version = "1.1_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "- Added output of estimated / recent runtime\n"
            + "- Uses getModel().getProgress() now, allowing for a more precise runtime estimation\n"
            + "- Can be used outside iteration context, causing smaller runtime footprint")
})
public class JAMSExecInfo extends JAMSGUIComponent implements Serializable {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Update inteval",
            defaultValue = "10"
    )
    public Attribute.Integer milliSeconds;

    private JProgressBar jamsProgressBar;
    private JPanel progressPanel;
    transient private Runnable updatePBar;
    transient private Observer infoLogObserver;
    transient private Observer errorLogObserver;
    private JScrollPane scrollPanel;
    private JTextArea logArea;

    private JPanel panel;
    private long startTime;
    private transient ScheduledExecutorService exec;
    private boolean runonce = false;
    private JAMSRuntime rt;
    private double divisor = 1;
    public JPanel getPanel() {
        if (this.panel == null) {
            createPanel();
        }
        return panel;
    }

    @Override
    public void init() {
        if (panel != null) {
            long max = this.getContext().getNumberOfIterations();
            divisor = Math.max(0, max / (long)Integer.MAX_VALUE)+1;               // account for max int
            jamsProgressBar.setMaximum((int) (max/divisor) );
        } else {
            updatePBar = new Runnable() {
                public void run() {
                }
            };
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    public void initAll() {
        if (!runonce && panel != null) {
            long max = this.getModel().getProgress()[1];
            divisor = Math.max(0, max / (long)Integer.MAX_VALUE)+1;    // account for max int        
            jamsProgressBar.setMaximum((int) (max/divisor));
            rt = getModel().getRuntime();
            runonce = true;
        }
    }

    private void createObserver() {
        if (infoLogObserver == null) {
            infoLogObserver = new Observer() {
                public void update(Observable obs, Object obj) {
                    logArea.append(obj.toString());
                    //logArea.setCaretPosition(logArea.getText().length());
                }
            };
        }
        if (errorLogObserver == null) {
            errorLogObserver = new Observer() {
                public void update(Observable obs, Object obj) {
                    logArea.append(obj.toString());
                    //logArea.setCaretPosition(logArea.getText().length());
                }
            };
        }
    }

    private void createPanel() {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());

        jamsProgressBar = new JProgressBar();
        jamsProgressBar.setMinimum(0);
        jamsProgressBar.setString("Execution progress");
        jamsProgressBar.setStringPainted(true);
        jamsProgressBar.setIndeterminate(false);
        jamsProgressBar.setFont(Font.decode("Monospaced"));
        if (milliSeconds.getValue() == 101) {
            milliSeconds.setValue(20);
            jamsProgressBar.setUI(new CatProgressUI());
        }
        if (milliSeconds.getValue() == 7) {
            jamsProgressBar.setBackground(Color.darkGray);
            milliSeconds.setValue(20);
            jamsProgressBar.setUI(new PacmanProgressUI());
        }
        if (milliSeconds.getValue() == 42) {
            jamsProgressBar.setBackground(Color.darkGray);
            jamsProgressBar.setForeground(Color.lightGray);
            milliSeconds.setValue(20);
            jamsProgressBar.setUI(new GhostProgressUI());
        }
        updatePBar = new Runnable() {
            public void run() {
                if (runonce && rt.getState() != JAMSRuntime.STATE_RUN) {
                    exec.shutdown();
                    return;
                }
                jamsProgressBar.setValue((int) (getModel().getProgress()[0] / divisor));
                long eTime = System.currentTimeMillis() - startTime;

                double progress = jamsProgressBar.getPercentComplete();
                long rTime;
                if (progress > 0) {
                    rTime = Math.round(eTime / progress) - eTime;
                } else {
                    rTime = 0;
                }
                String s = getTimeString(rTime, eTime);
                jamsProgressBar.setString(s);
            }
        };

        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(updatePBar);
            }
        }, 0, milliSeconds.getValue(), TimeUnit.MILLISECONDS);
//        f.cancel(true);

        progressPanel.add(jamsProgressBar, BorderLayout.CENTER);

        progressPanel.setPreferredSize(new Dimension(0, 40));
        panel.add(progressPanel, BorderLayout.NORTH);

        scrollPanel = new JScrollPane();
        logArea = new JTextArea();
        logArea.setColumns(20);
        logArea.setRows(5);
        logArea.setLineWrap(false);
        logArea.setEditable(false);
        logArea.setFont(JAMS.STANDARD_FONT);
        scrollPanel.setViewportView(logArea);

        panel.add(scrollPanel, BorderLayout.CENTER);

        logArea.append(this.getModel().getRuntime().getInfoLog());
        logArea.append(this.getModel().getRuntime().getErrorLog());

        createObserver();
        this.getModel().getRuntime().addInfoLogObserver(infoLogObserver);
        this.getModel().getRuntime().addErrorLogObserver(errorLogObserver);
    }

    private String getTimeString(long rTime, long eTime) {
        return String.format("%3.0f%% [%02d:%02d:%02d]/[%02d:%02d:%02d]", jamsProgressBar.getPercentComplete() * 100,
                        eTime / 3600000, (eTime % 3600000) / 60000, (eTime % 60000) / 1000,
                        rTime / 3600000, (rTime % 3600000) / 60000, (rTime % 60000) / 1000);
    }
    
    @Override
    public void run() {

    }

    @Override
    public void cleanup() {
        updatePBar.run();
//        SwingUtilities.invokeLater(updatePBar);
    }

    @Override
    public void restore() {

        updatePBar = new Runnable() {
            public void run() {
                if (runonce && rt.getState() != JAMSRuntime.STATE_RUN) {
                    exec.shutdown();
                    return;
                }
                jamsProgressBar.setValue((int) (getModel().getProgress()[0]/divisor));
                long eTime = System.currentTimeMillis() - startTime;

                double progress = jamsProgressBar.getPercentComplete();
                long rTime;
                if (progress > 0) {
                    rTime = Math.round(eTime / progress) - eTime;
                } else {
                    rTime = 0;
                }
                String s = getTimeString(rTime, eTime);
                jamsProgressBar.setString(s);
            }
        };

        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(updatePBar);
            }
        }, 0, milliSeconds.getValue(), TimeUnit.MILLISECONDS);

        if (this.logArea != null) {
            createObserver();
            this.getModel().getRuntime().addInfoLogObserver(infoLogObserver);
            this.getModel().getRuntime().addErrorLogObserver(errorLogObserver);
        }
    }

    private static class CatProgressUI extends BasicProgressBarUI {

        String cat;

        public CatProgressUI() {
            super();
            if (Math.random() >= 0.5) {
                cat = "1";
            } else {
                cat = "2";
            }
        }

        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {

            super.paintDeterminate(g, c);
            if (!(g instanceof Graphics2D)) {
                return;
            }

            Insets b = progressBar.getInsets(); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }

            int cellLength = getCellLength();
            int cellSpacing = getCellSpacing();
            // amount of progress to draw
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(progressBar.getForeground());

            // draw the cells
            if (cellSpacing == 0 && amountFull > 0) {
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                // draw each individual cell
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0.f, new float[]{cellLength, cellSpacing}, 0.f));
            }

            g2.drawLine(b.left, (barRectHeight / 2) + b.top,
                    amountFull + b.left, (barRectHeight / 2) + b.top);
            if (progressBar.getPercentComplete() == 1) {
                try {
                    InputStream is = getClass().getClassLoader().getResourceAsStream("jams/components/gui/resources/cat" + cat + ".png");
                    Image image = ImageIO.read(is);
                    g2.drawImage(image, amountFull + b.left - 60, 15, null);
                } catch (IOException ex) {
                    Logger.getLogger(CatProgressUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    InputStream is = getClass().getClassLoader().getResourceAsStream("jams/components/gui/resources/cat.png");
                    Image image = ImageIO.read(is);
                    g2.drawImage(image, amountFull + b.left - 30, 15, null);
                } catch (IOException ex) {
                    Logger.getLogger(CatProgressUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // Deal with possible text painting 
            if (progressBar.isStringPainted()) {
                paintString(g, b.left, b.top,
                        barRectWidth, barRectHeight,
                        amountFull, b);
            }
        }
    }

    private class PacmanProgressUI extends BasicProgressBarUI {

        private String[] pacman = {"0", "1"};
        private int c1 = 0, c2 = 0;
        private long max;

        public PacmanProgressUI() {
            super();
            max = Math.max(1, Math.round(100 / milliSeconds.getValue()));
        }

        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {

            super.paintDeterminate(g, c);
            if (!(g instanceof Graphics2D)) {
                return;
            }

            Insets b = progressBar.getInsets(); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }

            int cellLength = getCellLength();
            int cellSpacing = getCellSpacing();
            // amount of progress to draw
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(progressBar.getForeground());

            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("jams/components/gui/resources/fruits.png");
                Image image = ImageIO.read(is);
                g2.drawImage(image, b.left, b.top + 8, null);
            } catch (IOException ex) {
                Logger.getLogger(CatProgressUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            // draw the cells
            if (cellSpacing == 0 && amountFull > 0) {
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                // draw each individual cell
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0.f, new float[]{cellLength, cellSpacing}, 0.f));
            }

            g2.drawLine(b.left, (barRectHeight / 2) + b.top,
                    amountFull + 18 + b.left, (barRectHeight / 2) + b.top);

            c1++;
            if (c1 > max) {
                c2++;
                c1 = 0;
            }

            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("jams/components/gui/resources/pacman" + pacman[c2 % 2] + ".png");
                Image image = ImageIO.read(is);
                g2.drawImage(image, amountFull + b.left + 1, b.top, null);
            } catch (IOException ex) {
                Logger.getLogger(CatProgressUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Deal with possible text painting 
            if (progressBar.isStringPainted()) {
                paintString(g, b.left, b.top,
                        barRectWidth, barRectHeight,
                        amountFull, b);
            }
        }
    }

    private class GhostProgressUI extends BasicProgressBarUI {

        private int range = -20, count = 0, i = 1;
        private long max;

        public GhostProgressUI() {
            super();
            max = Math.max(1, Math.round(100 / milliSeconds.getValue()));
        }

        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {

            super.paintDeterminate(g, c);
            if (!(g instanceof Graphics2D)) {
                return;
            }

            Insets b = progressBar.getInsets(); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }

            int cellLength = getCellLength();
            int cellSpacing = getCellSpacing();
            // amount of progress to draw
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(progressBar.getForeground());

            // draw the cells
            if (cellSpacing == 0 && amountFull > 0) {
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                // draw each individual cell
                g2.setStroke(new BasicStroke((float) barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0.f, new float[]{cellLength, cellSpacing}, 0.f));
            }

            g2.drawLine(b.left, (barRectHeight / 2) + b.top,
                    amountFull + b.left, (barRectHeight / 2) + b.top);

            if (count == range) {
                i = 1;
            }
            if (count == 0) {
                i = -1;
            }
            count += i;

            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("jams/components/gui/resources/ghosts.png");
                Image image = ImageIO.read(is);
                g2.drawImage(image, b.left + count, b.top + 3, null);
            } catch (IOException ex) {
                Logger.getLogger(CatProgressUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Deal with possible text painting 
            if (progressBar.isStringPainted()) {
                paintString(g, b.left, b.top,
                        barRectWidth, barRectHeight,
                        amountFull, b);
            }
        }

        @Override
        protected Color getSelectionForeground() {
            return Color.BLACK;
        }

        @Override
        protected Color getSelectionBackground() {
            return Color.WHITE;
        }
    }
}
