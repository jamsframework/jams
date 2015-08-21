/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package staging;

import groundwater.J2KProcessGroundwater;
import climate.CalcRelativeHumidity;
import climate.RainCorrectionRichter;
import interception.J2KProcessInterception;
import java.io.File;
import java.io.PrintStream;
import oms3.util.Components;
import potet.PenmanMonteith;
import potet.RefET;
import radiation.CalcDailySolarRadiation;
import routing.J2KProcessRouting;
import snow.CalcRainSnowParts;
import snow.J2KProcessSnow;
import soilWater.J2KProcessLumpedSoilWater;

/**
 *
 * @author Olaf David
 */
//TODO simplify mapin.
//TODO feedback loop
public class GeneratorHRU {

    static Object[] c = {
        new CalcRelativeHumidity(),
        new CalcDailySolarRadiation(),
        new RefET(),
        new PenmanMonteith(),
        new CalcRainSnowParts(),
        new J2KProcessInterception(),
        new J2KProcessSnow(),
        new J2KProcessLumpedSoilWater(),
        new J2KProcessGroundwater(),
        new J2KProcessRouting()

    };

    public void generateModel() throws Exception {

        File f = new File(GeneratorHRU.class.getResource("").toURI().toURL().getFile()).getParentFile().getParentFile().getParentFile();
        File f1 = new File(f.toString() + "/src/ceap/HRU.java");
        System.out.println(f1);
        PrintStream w = new PrintStream(f1);

        w.println("package ceap;\n\n" +
//                "import java.io.File;\n" +
                "import oms3.annotations.*;\n" +
                "import static oms3.annotations.Role.*;\n" +
                "import oms3.control.*;\n\n" +
                "public class HRU extends Iteration {\n\n");
        //
        Components.figureOutParamDeclarations(w, c);
        Components.declare(w, c);

        w.println("  @Initialize");
        w.println("  public void init() {");
//        w.println("   conditional(obs, \"moreData\");");
        Components.figureOutMapIn(w, c);
        Components.figureOutConnect(w, c);
        w.println("  }");
        w.println("}\n");
        w.close();
    }

    public static void main(String[] args) throws Exception {
        GeneratorHRU g = new GeneratorHRU();
        g.generateModel();
    }
}
