/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public class DataSet implements Serializable{
    static public class MismatchException extends Exception{
        String msg;
        public MismatchException(String msg){
            this.msg = msg;
        }
        @Override
        public String toString(){
            return msg;
        }
    }

    public String name;
    protected DataSet parent = null;

    public DataSet(){}
    public DataSet(DataSet d){
        this.name = d.name;
        this.parent = d.parent;
    }

    @Override
    public String toString(){
        return name;
    }
}
