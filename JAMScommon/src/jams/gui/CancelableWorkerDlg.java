/*
 * BusyDlg.java
 * Created on 14. Mai 2007, 09:33
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
package jams.gui;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author S. Kralisch
 */
public class CancelableWorkerDlg extends WorkerDlgDecorator {
    
    JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
    
    public CancelableWorkerDlg(WorkerDlg parent) {
        super(parent);        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(getWorkerDlg().getMainPanel(), BorderLayout.CENTER);
        //hmm das ist blöd .. 
        mainPanel.add(cancelButton, BorderLayout.SOUTH);
        getWorkerDlg().setMainPanel(mainPanel);
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CancelableWorkerDlg.this.getWorkerDlg().worker.cancel(true);
            }
        });
        getWorkerDlg().pack();
    }     
    
    public static void main(String[] args) {
        CancelableWorkerDlg dlg = new CancelableWorkerDlg(new WorkerDlg(null, "test", "test"));
        dlg.getWorkerDlg().setTask(new Runnable() {

            @Override
            public void run(){
                try{
                    Thread.sleep(10000);
                }catch(Throwable t){
                    t.printStackTrace();
                }                
            }
        });
        dlg.getWorkerDlg().execute();
    }
}
