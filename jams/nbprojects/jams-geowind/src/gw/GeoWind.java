/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw;

import java.awt.Dimension;
import gw.ui.FancyPanel;
import gw.ui.BottomPanel;
import gw.ui.util.OvalBorder;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.jdesktop.swingx.StackLayout;

/*
 * 0.2 release notes:
 * App
 *   - upgrade to Geotools 2.5.2
 *   - Added USGS Topomaps as default layer / disabled
 *   - SimpleFeatures either following the surface or elevated/shaded
 *   - Table base coloring can be changed
 *   - UI improvements.
 *      - Tree style layer handling
 *      - better full screen mode
 *      - 'Tips and Tricks' panel
 * 
 *
 *
 */
 //TODO  Control for time
// TODO conrtol for needle
/** 
 *  
 *
 * @author od
 */
public class GeoWind {

    public static void main(String[] args) throws Exception {

        // check for 1.6+
        try {
            Class.forName("java.io.Console");
        } catch (ClassNotFoundException E) {
            System.err.println(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_JavaVersionError"));
            return;
        }

        final Timer t = new Timer(10000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Runtime.getRuntime().gc();
            }
        });


        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JFrame df = new JFrame();
                df.setIconImage(Toolkit.getDefaultToolkit().getImage(
                        GeoWind.class.getResource("/gw/resources/Earth-16x16.png")));
                df.setTitle(FancyPanel.TITLE);
                //df.setUndecorated(true);

                FancyPanel p = new FancyPanel(df, null, null);
                BottomPanel bp = new BottomPanel();

                JPanel pane = new JPanel();
                pane.setMinimumSize(new Dimension(300, 300));
                p.setBorder(new OvalBorder(2, 2));
                pane.setLayout(new StackLayout());

                pane.add(bp, StackLayout.TOP);
                pane.add(p, StackLayout.TOP);

                df.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                df.getContentPane().add(pane);
                df.setSize(921, 691);

                // Get the size of the screen
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

                // Determine the new location of the window
                int x = (dim.width - df.getSize().width) / 2;
                int y = (dim.height - df.getSize().height) / 2;

                // Move the window
                df.setLocation(x, y);
                df.setVisible(true);
                t.start();
            }
        });
    }
}
