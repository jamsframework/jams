/*
 * ViewList.java
 * Created on 17. Mai 2006, 17:05
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jamsui.juice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import javax.swing.JInternalFrame;
import jamsui.juice.gui.ModelView;

/**
 *
 * @author S. Kralisch
 */
public class ViewList extends Observable {
    
    private HashMap<JInternalFrame, ModelView> mViews = new HashMap<JInternalFrame, ModelView>();
    private ArrayList<ModelView> viewList = new ArrayList<ModelView>();

    public void addView(JInternalFrame modelFrame, ModelView mView) {
        getMViews().put(modelFrame, mView);
        getViewList().add(mView);
        setChanged();
        notifyObservers();
    }
    
    public void removeView(JInternalFrame modelFrame) {
        getViewList().remove(getMViews().get(modelFrame));
        getMViews().remove(modelFrame);
        setChanged();
        notifyObservers();
    }
    
    public ArrayList<ModelView> getViewList() {
        return viewList;
    }

    public HashMap<JInternalFrame, ModelView> getMViews() {
        return mViews;
    }
    
}
