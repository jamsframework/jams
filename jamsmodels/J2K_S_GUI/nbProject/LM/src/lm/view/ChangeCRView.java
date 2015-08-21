package lm.view;

import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import lm.Componet.Vector.LMCropRotationElement;
import lm.Componet.Vector.LMCropRotationVector;
import lm.view.changeCR.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ChangeCRView extends JFrame {

    private CRMainPanel panel;
    private CRDetailFrame detailFrame = new CRDetailFrame();
    private CRAddFrame addFrame = new CRAddFrame();
    private CRPopup crPopup = new CRPopup();
    private CropIDFrame cropIDFrame = new CropIDFrame();
    private CRInsertArableIDFrame crInsertArableIDFrame = new CRInsertArableIDFrame();

    public ChangeCRView() {
    }
    public void close(){
        this.setVisible(false);
    }

    public void removeContent() {
        panel.removeAll();
        panel.clearRowList();
    }

    public void ShowAndBuildGui() {
        panel = new CRMainPanel();
        this.setContentPane(panel);
        this.setResizable(false);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.pack();
    }

    public CRMainPanel getCRMainPanel() {
        return this.panel;
    }

    public void setAndShowAddFrame() {
        addFrame.createAndShowGUI();
    }

    public void setAndShowDetailFrame(LMCropRotationElement cropRotationElement) {
        detailFrame.createAndShowGUI(cropRotationElement);
    }

    public void setAndShowCropIDFrame(LMCropRotationVector cropRotationVector) {
        getCropIDFrame().createAndShowGUI(cropRotationVector);
    }

    public void setAndShowCRInsertArableIDFrame(){
        crInsertArableIDFrame.createAndShowGUI();
    }

    public void addDetailListener(ActionListener l) {
        panel.getPanel().addDetailListener(l);
    }

    public void addCropIDListener(ActionListener l) {
        panel.getPanel().addCropIDListener(l);
    }

    public void createAndShowGUI() {
        panel.createAndShowGUI();
        this.setVisible(true);
        this.pack();
    }

    public void addModelsToAddFrame(DefaultListModel arableIDListModel, DefaultListModel cropIDListModel) {
        addFrame.setCropRotationIDList(cropIDListModel);
        addFrame.setArableIDList(arableIDListModel);
    }

    public CRAddFrame getCRAddFrame() {
        return this.addFrame;
    }

    /**
     * @return the crPopup
     */
    public CRPopup getCrPopup() {
        return crPopup;
    }

    /**
     * @return the crInsertArableIDFrame
     */
    public CRInsertArableIDFrame getCrInsertArableIDFrame() {
        return crInsertArableIDFrame;
    }

    /**
     * @return the cropIDFrame
     */
    public CropIDFrame getCropIDFrame() {
        return cropIDFrame;
    }
}
