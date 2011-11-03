/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

/**
 *
 * @author chris
 */
public class CommunicationException extends Exception{
    String s;
    public CommunicationException(String s){
        this.s = s;
    }
    @Override
    public String toString(){
        return s;
    }
}
