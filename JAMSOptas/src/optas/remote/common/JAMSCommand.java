/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public class JAMSCommand implements Serializable {
    enum COMMAND{CONNECT, DISCONNECT, GET_JOBLIST, GET_FILELIST, GET_FILE};

    private String name;
    private long id;

    private static long idCounter=0;

    JAMSCommand(){
        id = idCounter++;
    }

    public long getId(){
        return id;
    }
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return getName();
    }
}
