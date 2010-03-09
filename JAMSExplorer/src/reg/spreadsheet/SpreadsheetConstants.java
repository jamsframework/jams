/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.spreadsheet;

import java.awt.Color;

/**
 *
 * @author robertriedel
 */
public class SpreadsheetConstants {

    /* DAT LOAD TOKENIZER STRINGS */

    public final static String LOAD_DATA = "#data";
    public final static String LOAD_HEADERS = "#headers";
    public final static String LOAD_END = "#end";

    /* DIALOG INFO MESSAGES */

    public final static String INFO_MSG_SAVETEMP = "Please choose a template filename:";
    public final static String INFO_MSG_SAVEDAT = "Please choose a filename:";

    /* DIALOG TITLES */

    public final static String DLG_TITLE_CUSTOMIZE = "Customize Renderer";
    public final static String DLG_TITLE_JTSCONFIGURATOR ="JTS Viewer";
    public final static String DLG_TITLE_JXYSCONFIGURATOR ="XYPlot Viewer";

    /* DIALOG ERROR MESSAGES */

    public final static String STP_ERR_NOTEMPFOUND = "No template files found in the workspace directory! " +
                    "Use the 'Save Template' Option in the Time Plot Configurator!";
    public final static String SPREADSHEET_ERR_TSMISSING = "Time Series missing!";

    public final static String JXY_ERR_NODATATEMPLATE = "No Template for Dataplot. Use at least 2 Columns";

    /* GUI ELEMENT COLORS */

//    public final static Color GUI_COLOR_CLOSETAB = Color.DARK_GRAY;

    /* FILE NAMES */
    public final static String FILE_ENDING_TTP = ".ttp";
    public final static String FILE_ENDING_DAT = ".sdat";
    public final static String FILE_EXPLORER_DIR_NAME = "/explorer";

    /* STP TITLE */
    public final static String STP_TITLE = "StackedTimePlot Configurator";

    /* FONTS */

    /* STRINGS */


    /* DEFAULT ST PLOT PROPERTIES */

    /* DEFAULT JTS PLOT PROPERTIES */

    public final static int JTS_DEFAULT_STROKE = 1;
    public final static int JTS_DEFAULT_SHAPE_SIZE = 3;
    public final static int JTS_DEFAULT_SHAPE = 1;

    /* DEFAULT JXYS PLOT PROPERTIES */

    public final static int JXYS_DEFAULT_STROKE = 0;
    public final static int JXYS_DEFAULT_SHAPE_SIZE = 1;
    public final static int JXYS_DEFAULT_SHAPE = 5;
}
