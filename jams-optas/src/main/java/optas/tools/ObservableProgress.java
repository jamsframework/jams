/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.tools;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 *
 * @author chris
 */
public class ObservableProgress extends Observable implements Serializable{
    double progress;
    boolean debugLog = true;

    Set<Observer> myObservers = new HashSet<Observer>();

    @Override
    public void addObserver(Observer o){
        myObservers.add(o);
    }
    @Override
    public void deleteObserver(Observer o){
        myObservers.remove(o);
    }
    @Override
    public void deleteObservers(){
        myObservers.clear();
    }

    public double getProgress(){        
        return progress;
    }
    protected void setProgress(double progress){
        this.progress = progress;
        this.notifyObservers();
    }

    public void setDebugLog(boolean debugLog){
        this.debugLog = debugLog;
    }

    public boolean getDebugLog(){
        return debugLog;
    }

    protected void log(String msg){
        setChanged();
        if (debugLog)
            System.out.println(msg);
        this.notifyObservers(msg);
    }

    protected Set<Observer> getObservers(){
        return myObservers;
    }
}
