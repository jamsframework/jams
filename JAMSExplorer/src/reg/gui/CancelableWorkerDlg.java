/*
 * CancelableWorkerDlg.java
 * Created on 13. Februar 2009, 22:43
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package reg.gui;

import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class CancelableWorkerDlg extends WorkerDlg {

    private CancelableSwingWorker task;

    public CancelableWorkerDlg(Frame owner, String title) {
        super(owner, title);

        JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CANCEL"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                task.cancel();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    public synchronized void setTask(CancelableSwingWorker task) {
        this.task = task;
        super.setTask(task);
    }
}
