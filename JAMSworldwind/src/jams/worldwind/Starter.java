package jams.worldwind;

import gov.nasa.worldwind.Configuration;
import jams.worldwind.ui.view.GlobeView;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        logger.info("Entering Starter application.");
        if (Configuration.isMacOS()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        if (args[0].equals("run_from_netbeans")) {
            System.out.println("Run from Netbeans...");
            System.setProperty("gov.nasa.worldwind.app.config.document", System.getProperty("user.dir") + "/dist/config/worldwind.xml");
        } else {
            System.out.println("Normal start...");
            String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = path;
            try {
                decodedPath = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.toString());
            }
            String absolutePath = decodedPath.substring(0, decodedPath.lastIndexOf("/"));
            //load own configuration        
            System.setProperty("gov.nasa.worldwind.app.config.document", absolutePath + "/config/worldwind.xml");
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GlobeView view = GlobeView.getInstance();
                view.readFromDisk("../../JAMSworldwind/src/jams/worldwind/test/DataTransfer3DTestData.ser");
                view.show();
            }
        });
        logger.info("Exiting Starter application.");
    }
}
