/*
 * Regionalizer.java
 * Created on 18. November 2008, 21:37
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package reg;

import jams.JAMS;
import jams.gui.LHelper;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import java.util.Observable;
import java.util.Observer;
import javax.swing.UIManager;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class Regionalizer {

    public static final String APP_TITLE = "DataReg";
    public static final int SCREEN_WIDTH = 1200,  SCREEN_HEIGHT = 850;
    private static RegionalizerFrame regFrame;
    private static JAMSRuntime runtime;

    /**
     * @return the runtime
     */
    public static JAMSRuntime getRuntime() {
        return runtime;
    }

    public Regionalizer() {
    }

    public static void main(String[] args) {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception evt) {
        }

        runtime = new StandardRuntime();
        runtime.setDebugLevel(JAMS.VERBOSE);
        runtime.addErrorLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                LHelper.showErrorDlg(regFrame, arg.toString(), JAMS.resources.getString("Error"));
            }
        });
        runtime.addInfoLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                //LHelper.showInfoDlg(regFrame, arg.toString(), JAMS.resources.getString("Info"));
            }
        });


        regFrame = new RegionalizerFrame();
        regFrame.setVisible(true);
    }
}
