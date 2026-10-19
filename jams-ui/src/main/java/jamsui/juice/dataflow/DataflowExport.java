/*
 * DataflowExport.java
 * Created on 19.10.2026
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
package jamsui.juice.dataflow;

import jams.JAMS;
import jams.JAMSProperties;
import jams.meta.ModelDescriptor;
import jams.meta.ModelIO;
import jams.runtime.JAMSClassLoader;
import jams.runtime.RuntimeLogger;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Export of data flow diagrams as SVG or PNG. Can also be used from the
 * command line:
 * <pre>
 * java [-Ddataflow.hide=regex] -cp ... jamsui.juice.dataflow.DataflowExport model.jam diagram.svg|diagram.png [jams.jap]
 * </pre>
 *
 * @author Sven Kralisch
 */
public class DataflowExport {

    public static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    public static String title(DataflowGraph graph) {
        return graph.getModelName() + " – " + JAMS.i18n("Data_Flow");
    }

    public static void exportSVG(DataflowLayout layout, String title, File file) throws IOException {
        DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(doc);
        ctx.setComment("JAMS data flow: " + title);
        SVGGraphics2D g = new SVGGraphics2D(ctx, false);
        g.setSVGCanvasSize(new Dimension((int) Math.ceil(layout.getWidth()), (int) Math.ceil(layout.getHeight())));
        new DataflowRenderer(layout).paint(g, title);
        try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            g.stream(out, true);
        }
    }

    public static void exportPNG(DataflowLayout layout, String title, File file, double scale) throws IOException {
        int w = (int) Math.ceil(layout.getWidth() * scale);
        int h = (int) Math.ceil(layout.getHeight() * scale);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.scale(scale, scale);
        new DataflowRenderer(layout).paint(g, title);
        g.dispose();
        ImageIO.write(img, "png", file);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: DataflowExport <model.jam> <out.svg|out.png> [jams properties file]");
            System.exit(1);
        }
        JAMSProperties props = JAMSProperties.createProperties();
        if (args.length > 2) {
            props.load(args[2]);
        } else if (new File("default.jap").exists()) {
            props.load("default.jap");
        }
        String[] libs = StringTools.toArray(props.getProperty(JAMSProperties.LIBS_IDENTIFIER), ";");
        ClassLoader loader = JAMSClassLoader.createClassLoader(libs, new RuntimeLogger());

        ModelDescriptor md = ModelIO.getStandardModelIO().loadModelDescriptor(XMLTools.getDocument(args[0]), loader, false);
        DataflowGraph.Options options = new DataflowGraph.Options();
        options.hidePattern = System.getProperty("dataflow.hide", "");
        DataflowGraph graph = DataflowGraph.build(md, options);
        DataflowLayout layout = new DataflowLayout(graph, FRC);

        File out = new File(args[1]);
        if (out.getName().toLowerCase().endsWith(".png")) {
            exportPNG(layout, title(graph), out, 2);
        } else {
            exportSVG(layout, title(graph), out);
        }
        System.out.println("written " + out.getAbsolutePath() + " (" + graph.getNodes().size() + " nodes, " + graph.getEdges().size() + " edges)");
        System.exit(0);
    }
}
