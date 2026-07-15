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

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;

/**
 *
 * @author S. Kralisch
 */
public class ObserverWorkerDlg extends WorkerDlgDecorator implements Observer{    
    JLabel progressBarLabel = new JLabel();
    
    public ObserverWorkerDlg(WorkerDlg parent) {
        super(parent);
        
        getWorkerDlg().progressBar.setLayout(new BorderLayout(5, 5));
        getWorkerDlg().progressBar.add(progressBarLabel, BorderLayout.CENTER);        
        //progressBarLabel.setStringPainted(true);
        
        getWorkerDlg().pack();
    }
            
    @Override
    public void update(Observable observable, Object o){   
        //getWorkerDlg().progressBarLabel.setStringPainted(true);
        progressBarLabel.setText(o.toString()); 
    }    
    
    static private class XXX extends Observable{
        public void change(){
            setChanged();
        }
    }
    
    public static void main(String[] args) {
        ObserverWorkerDlg dlg = new ObserverWorkerDlg(new CancelableWorkerDlg(new WorkerDlg(null, "test", "test")).getWorkerDlg());
        
        final XXX o = new XXX();
        o.addObserver(dlg);
        
        dlg.getWorkerDlg().setTask(new Runnable() {

            @Override
            public void run(){
                try{
                    long i=0;
                    while(i>=0){
                        o.change();
                        o.notifyObservers(i++);
                    }
                }catch(Throwable t){
                    t.printStackTrace();
                }                
            }
        });
                                
        dlg.getWorkerDlg().execute();
    }
}
