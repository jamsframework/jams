package jams.worldwind;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import gov.nasa.worldwind.Configuration;
import jams.worldwind.ui.model.GlobeModel;
import jams.worldwind.ui.view.GlobeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bigr
 */
public class Starter {

    final static String appName = "JAMS WorldWind";
    final static Logger logger = LoggerFactory.getLogger(Starter.class);

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (Configuration.isWindowsOS()) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static void main(String[] args) {
        logger.info("Entering Starter application.");
        if (Configuration.isMacOS()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }
        //LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        //StatusPrinter.print(lc);
        //logger.info(lc.toString());

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GlobeView view = new GlobeView(new GlobeModel());
                view.show();
            }
        });
        logger.info("Exiting Starter application.");
    }
}
