/*
 * JRIConsole.java
 * Created on 20.05.2019, 16:49:41
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.indices;

import jams.data.*;
import jams.model.*;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
    title="JRIConsole",
    author="Sven Kralisch",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class JRIConsole extends JAMSComponent {

    @Override
    public void init() {

            Rengine re = new Rengine(null, false, new TextConsole());
            if (!re.waitForR()) {
                System.out.println("Cannot load R");
                return;
            }

            if (true) {
                System.out.println("Now the console is yours ... have fun");
                re.startMainLoop();
            } else {
                re.end();
                System.out.println("end");
            }   

    }

}

class TextConsole implements RMainLoopCallbacks {

    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }

    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy(" + which + ")");
    }

    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            return (s == null || s.length() == 0) ? s : s + "\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: " + e.getMessage());
        }
        return null;
    }

    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \"" + message + "\"");
    }

    public String rChooseFile(Rengine re, int newFile) {
        FileDialog fd = new FileDialog(new Frame(), (newFile == 0) ? "Select a file" : "Select a new file", (newFile == 0) ? FileDialog.LOAD : FileDialog.SAVE);
        fd.show();
        String res = null;
        if (fd.getDirectory() != null) {
            res = fd.getDirectory();
        }
        if (fd.getFile() != null) {
            res = (res == null) ? fd.getFile() : (res + fd.getFile());
        }
        return res;
    }

    public void rFlushConsole(Rengine re) {
    }

    public void rLoadHistory(Rengine re, String filename) {
    }

    public void rSaveHistory(Rengine re, String filename) {
    }
}