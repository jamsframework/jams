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

            Rengine re = new Rengine(null, false, new RTextConsole());
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

