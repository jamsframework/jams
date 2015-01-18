/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.error;

import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author christian
 */
public class DefaultFileUploadErrorHandling implements ErrorHandler<File>{

    @Override
    public boolean handleError(File f, Throwable ex) {
        int result = JOptionPane.showConfirmDialog(
                null,
                "The file " + f + " can't be uploaded. Would you like to continue?",
                "Continue?",
                JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
}
