package landnutzungmangement;

import lm.controller.MainController;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class LMExplorer {

    private static MainController maincontroller;
    public LMExplorer(){

    }

    public static void main(String[] args) {

        maincontroller = new MainController();
        maincontroller.CreateAndShowGUI();

    }

}
