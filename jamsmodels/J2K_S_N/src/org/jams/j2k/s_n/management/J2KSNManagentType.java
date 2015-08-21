/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.management;

import java.util.ArrayList;
import org.jams.j2k.s_n.crop.J2KSNLMArable_gui;

/**
 *
 * @author c8fima
 */
public class J2KSNManagentType implements ManagementOption {

   

    private Integer id;
    private ArrayList<J2KSNLMArable_gui> mList;

    public J2KSNManagentType(Integer id, ArrayList<J2KSNLMArable_gui> mList) {
        this.id = id;
        this.mList = mList;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the mList
     */
    public ArrayList<J2KSNLMArable_gui> getmList() {
        return mList;
    }
        
}
