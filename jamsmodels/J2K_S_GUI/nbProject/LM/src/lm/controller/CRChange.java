package lm.controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMCropRotationElement;
import lm.Componet.Vector.LMCropRotationVector;
import lm.model.CRSaveModel;
import lm.model.SaveModel;
import lm.view.ChangeCRView;
import lm.view.changeCR.CRPanel;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRChange {

    private MainController mainController;
    private ChangeCRView changeCRView;
    private CRPanel crPanel;
    private SaveModel saveModel;
    private CRSaveModel crSaveModel;
    private TreeMap<Integer, LMCropRotationVector> treeMap;

    public CRChange(MainController p) {
        this.mainController = p;


    }

    public void addMenubarListener() {
        mainController.getMainView().getMainMenuBar().setChangeCR(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    mainController.getMainModel().createCRSaveModel();
                    crSaveModel = mainController.getMainModel().getCRSaveModel();
                    mainController.getMainView().initCRChange();
                    changeCRView = mainController.getMainView().getChangeCR();
                    changeCRView.ShowAndBuildGui();
                    crPanel = changeCRView.getCRMainPanel().getPanel();
                    saveModel = mainController.getMainModel().getSaveModel();
                    treeMap = crSaveModel.getDefaultModel().getLMCropRotation();
                    addSaveAndCancelListener();
                    modelsAndListenerForHead();
                    addListenerToCrPopup();
                    addListenersToCRInsertArableIDFrame();
                    giveViewContent();
                } catch (CloneNotSupportedException ex) {
                }
            }
        });

    }

    public void giveViewContent() {
        int zoom=changeCRView.getCRMainPanel().getHead().getZoomBoxPosition()+1;
        changeCRView.removeContent();
        if(!treeMap.isEmpty()){
            Integer key = (Integer) treeMap.firstKey();
            TreeMap<Integer,LMCropRotationElement> a;
            while (key != null) {
                crPanel.addRow(key);
                a = treeMap.get(key).getCRE();
                if(a.isEmpty()){
                    crPanel.setLast(key);
                }
                if(!a.isEmpty()){
                Integer key2=a.firstKey();
                    while(key2!=null) {
                            Integer p = (a.get(key2)).getArableID();
                            Color color;
                            if (p == 0) {
                                color = Color.WHITE;
                            } else {
                                color = saveModel.getMultiModel().getLMArable().get(p).getColor();
                            }
                            if(a.get(key2).getDuration()!=0){
                                crPanel.addElementsOfARow(key, a.get(key2), color,zoom);
                            }
                            key2=a.higherKey(key2);
                        }
                }
                key = (Integer) treeMap.higherKey(key);

            }
        }
        changeCRView.createAndShowGUI();
        addListenerToViewButtons();
        addListenerToCropID();

    }

    public void addSaveAndCancelListener(){
        changeCRView.getCRMainPanel().addActionListenerToSave(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                try {
                    changeCRView.setVisible(false);
                    mainController.getMainModel().saveCRSaveModel();
                } catch (CloneNotSupportedException ex) {
                }
            }
        });
        changeCRView.getCRMainPanel().addActionListenerToCancel(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                try {
                    changeCRView.setVisible(false);
                    mainController.getMainModel().cancelCRSaveModel();
                } catch (CloneNotSupportedException ex) {
                }
            }
        });
    }

    public void modelsAndListenerForHead(){
        changeCRView.getCRMainPanel().getHead().addZoomBoxModel(crSaveModel.getZoomModel());
        changeCRView.getCRMainPanel().getHead().addZoomListener(new ZoomListener());
        changeCRView.getCRMainPanel().getHead().addActionListenerToNewRow(new ActionListener(){
            public void actionPerformed (ActionEvent event){
                crSaveModel.addRow();
                giveViewContent();
            }
        });
    }

    public void addListenerToCropID(){
        changeCRView.addCropIDListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                Integer iD= Integer.parseInt(event.getActionCommand());
                changeCRView.getCropIDFrame().setActionCommandToDelete(event.getActionCommand());
                changeCRView.getCropIDFrame().addActionListenerToDelete(new ActionListener(){
                    public void actionPerformed (ActionEvent event){
                        crSaveModel.deleteRow(Integer.parseInt(event.getActionCommand()));
                        giveViewContent();
                        changeCRView.getCropIDFrame().setVisible(false);
                    }
                });
                changeCRView.setAndShowCropIDFrame(saveModel.getMultiModel().getLMCropRotation().get(iD));
            }
        });
    }

    public void addListenerToViewButtons() {
        changeCRView.addDetailListener(new PopupListener());      
    }

    public void addListenerToCrPopup(){
        changeCRView.getCrPopup().addActionListenerToEigenschaften(new DetailListener());
        changeCRView.getCrPopup().addActionListenerToDelete(new DeleteListener());
        changeCRView.getCrPopup().addActionListenerToSetFallow(new SetFallowListener());
        changeCRView.getCrPopup().addActionListenerToInsertArableID(new InsertArableIDListener());
        changeCRView.getCrPopup().addActionListenerToReduceFallow(new ReduceFallowListener());
        changeCRView.getCrPopup().addActionListenerToMaximizeFallow(new MaximizeFallowListener());
        changeCRView.getCrPopup().addActionListenerToInsertArabeLeft(new InsertArableIDLeft());
        changeCRView.getCrPopup().addActionListenerToInsertArableRight(new InsertArableIDRight());
    }
    public void addListenersToAddFrame() {
    }

    public void addListenersToCRInsertArableIDFrame(){
        changeCRView.getCrInsertArableIDFrame().clear();
        changeCRView.getCrInsertArableIDFrame().addInsertActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                String[] s =ae.getActionCommand().split("/");
                Integer crID=Integer.parseInt(s[0]);
                Integer postion=Integer.parseInt(s[1]);
                Integer key =Integer.parseInt((changeCRView.getCrInsertArableIDFrame().getArableID()).split(" ")[0]);
                crSaveModel.addElementAtPostion(crID, postion, key);
                changeCRView.getCrInsertArableIDFrame().hide();
                giveViewContent();
            }
        });
        changeCRView.getCrInsertArableIDFrame().addCancelActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){

            }
        });
    }


    public class ZoomListener implements ActionListener{
        public void actionPerformed(ActionEvent ae){
            giveViewContent();
        }
    }

    public class PopupListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            String[] s = ae.getActionCommand().split("/");
            if (s.length <= 1) {
                changeCRView.setAndShowAddFrame();
                changeCRView.addModelsToAddFrame(crSaveModel.getArableIDListModel(), crSaveModel.getCropIDListModel());
                changeCRView.getCRAddFrame().addButtonListener(new AddFrameListener(), ae.getActionCommand());
            }else{
                if((Integer.parseInt(s[1]))%2!=0){
                    changeCRView.getCrPopup().createFallowMenu(ae.getActionCommand());
                }else{
                    changeCRView.getCrPopup().createArableIDMenu(ae.getActionCommand());
                    
                }
//            changeCRView.getCrPopup().setActionCommand(ae.getActionCommand());
//            changeCRView.getCrPopup().show();
            }

        }
    }

    public class DetailListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            changeCRView.getCrPopup().Hide();
            String[] s = ae.getActionCommand().split("/");
                Integer CropID = Integer.valueOf(s[0]);
                Integer row = Integer.valueOf(s[1]);
                changeCRView.setAndShowDetailFrame(treeMap.get(CropID).getLMCropRotationElement(row));
        }
    }
    public class DeleteListener implements ActionListener{

        public void actionPerformed(ActionEvent ae){
            changeCRView.getCrPopup().Hide();
            String[] s = ae.getActionCommand().split("/");
            Integer crID = Integer.valueOf(s[0]);
            Integer row = Integer.valueOf(s[1]);
            crSaveModel.deleteElement(crID, row);
            giveViewContent();
        }
    }

    public class SetFallowListener implements ActionListener{

        public void actionPerformed(ActionEvent ae){
            changeCRView.getCrPopup().Hide();
            String[] s=ae.getActionCommand().split("/");
            crSaveModel.setAsFallow(Integer.valueOf(s[0]),Integer.valueOf(s[1]));
            giveViewContent();
        }
    }

    public class InsertArableIDListener implements ActionListener{

        public void actionPerformed(ActionEvent ae){
            changeCRView.getCrPopup().Hide();
            addListenersToCRInsertArableIDFrame();
            String[] s = ae.getActionCommand().split("/");
            Integer crID = Integer.valueOf(s[0]);
            Integer row = Integer.valueOf(s[1]);
            if(crSaveModel.createAndGetInsertArableIDModel(crID, row).isEmpty()){
                mainController.getMainView().getDialogs().showAbortDialog("Keine passende ArableID gefunden");
            }else{
            changeCRView.getCrInsertArableIDFrame().setListModel(crSaveModel.createAndGetInsertArableIDModel(crID, row));
            changeCRView.getCrInsertArableIDFrame().setActionCommandOK(ae.getActionCommand());
            changeCRView.setAndShowCRInsertArableIDFrame();
            }
        }
    }

    public class AddFrameListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Integer cropID = Integer.parseInt(event.getActionCommand());
            int multiplikator = Integer.parseInt(changeCRView.getCRAddFrame().getMultiplikator());
            int arableIDToGo = changeCRView.getCRAddFrame().getArableIDListElement();
            int cropIDToGo = changeCRView.getCRAddFrame().getCropIDListElement();
            if (arableIDToGo != 0) {
                crSaveModel.addElement(cropID, saveModel.getMultiModel().getLMArable().get(arableIDToGo), multiplikator);
            }
            if (cropIDToGo != 0) {
                crSaveModel.addVector(cropID, saveModel.getMultiModel().getLMCropRotation().get(cropIDToGo), multiplikator);
            }
            giveViewContent();
        }
    }

    public class ReduceFallowListener implements ActionListener {

            public void actionPerformed(ActionEvent ae) {
                changeCRView.getCrPopup().Hide();
                String[] s = ae.getActionCommand().split("/");
                Integer crID = Integer.valueOf(s[0]);
                Integer row = Integer.valueOf(s[1]);
                crSaveModel.reduceFallow(crID, row);

                giveViewContent();
            }
    }

    public class MaximizeFallowListener implements ActionListener {

            public void actionPerformed(ActionEvent ae) {
                changeCRView.getCrPopup().Hide();
                String[] s = ae.getActionCommand().split("/");
                Integer crID = Integer.valueOf(s[0]);
                Integer row = Integer.valueOf(s[1]);
                crSaveModel.maximizeFallow(crID, row,1);

                giveViewContent();
            }
    }

    public class InsertArableIDLeft implements ActionListener {
    
            public void actionPerformed(ActionEvent ae){
                changeCRView.getCrPopup().Hide();
                changeCRView.getCrInsertArableIDFrame().clear();
                changeCRView.getCrInsertArableIDFrame().setListModel(crSaveModel.getCompleteArableIDModel());
                changeCRView.getCrInsertArableIDFrame().setActionCommandOK(ae.getActionCommand());
                
                changeCRView.getCrInsertArableIDFrame().addInsertActionListener(new ActionListener(){

                            public void actionPerformed(ActionEvent ae){
                            int key=Integer.parseInt(ae.getActionCommand().split("/")[0]);
                            int position=Integer.parseInt(ae.getActionCommand().split("/")[1]);
                            crSaveModel.insertArableIDLeft(key, position, changeCRView.getCrInsertArableIDFrame().getArableID());
                            changeCRView.getCrInsertArableIDFrame().setVisible(false);
                            giveViewContent();
                    }

                });
                changeCRView.getCrInsertArableIDFrame().addCancelActionListener(new ActionListener(){

                            public void actionPerformed(ActionEvent ae){
                            changeCRView.getCrInsertArableIDFrame().setVisible(false);
                }

                });
                changeCRView.setAndShowCRInsertArableIDFrame();
            }
    }

    public class InsertArableIDRight implements ActionListener {

            public void actionPerformed(ActionEvent ae){
                changeCRView.getCrPopup().Hide();
                changeCRView.getCrInsertArableIDFrame().clear();
                changeCRView.getCrInsertArableIDFrame().setListModel(crSaveModel.getCompleteArableIDModel());
                changeCRView.getCrInsertArableIDFrame().setActionCommandOK(ae.getActionCommand());

                changeCRView.getCrInsertArableIDFrame().addInsertActionListener(new ActionListener(){

                            public void actionPerformed(ActionEvent ae){
                            int key=Integer.parseInt(ae.getActionCommand().split("/")[0]);
                            int position=Integer.parseInt(ae.getActionCommand().split("/")[1]);
                            crSaveModel.insertArableIDRight(key, position, changeCRView.getCrInsertArableIDFrame().getArableID());
                            changeCRView.getCrInsertArableIDFrame().setVisible(false);
                            giveViewContent();
                    }

                });
                changeCRView.getCrInsertArableIDFrame().addCancelActionListener(new ActionListener(){

                            public void actionPerformed(ActionEvent ae){
                            changeCRView.getCrInsertArableIDFrame().setVisible(false);
                }

                });
                changeCRView.setAndShowCRInsertArableIDFrame();

            }
    }

}
