/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui.MCAT5;


import java.util.ArrayList;
import javax.swing.JPanel;
import reg.hydro.data.DataCollection;
import reg.hydro.data.DataSet;



/**
 *
 * @author chris
 */
public abstract class MCAT5Plot {
    public class SimpleRequest{
        public String name;
        public Class clazz;
        public int min, max;

        public SimpleRequest(String name, Class clazz){
            this.name = name;
            this.clazz = clazz;
            this.min = 1;
            this.max = 1;
        }
        public SimpleRequest(String name, Class clazz, int min, int max){
            this.name = name;
            this.clazz = clazz;
            this.min = min;
            this.max = max;
        }
    }

    public class Result{
        ArrayList<DataSet> data;
        boolean valid;

        public Result(){
            data = new ArrayList<DataSet>();
            data.add(new DataSet());
            valid = false;
        }
        public Result(ArrayList<DataSet> data){
            this.data = data;
            valid = true;
        }
    }

    private ArrayList<SimpleRequest> request = new ArrayList<SimpleRequest>();
    private ArrayList<Result> ensembles = new ArrayList<Result>();
    private DataCollection data = null;
    public abstract void refresh();
    public abstract JPanel getPanel();

    protected void addRequest(SimpleRequest r){
        request.add(r);                        
        ensembles.add(new Result());
    }
    public ArrayList<SimpleRequest> getRequiredData(){
        return request;
    }
    public void setDataSource(DataCollection collection){
        this.data = collection;
    }
    public DataCollection getDataSource(){
        return this.data;
    }

    public void setData(String name, ArrayList<DataSet> data) {
        for (int i = 0; i < request.size(); i++) {
            if (name.equals(request.get(i).name)) {
                ensembles.set(i, new Result(data));
            }
        }
    }
    
    public void setData(String name, DataSet data) {
        for (int i = 0; i < request.size(); i++) {
            if (name.equals(request.get(i).name)) {
                ArrayList<DataSet> a = new ArrayList<DataSet>();
                a.add(data);
                ensembles.set(i, new Result(a));
            }
        }
    }

    protected boolean isRequestFulfilled(){
        for (Result r : ensembles)
            if (!r.valid)
                return false;
        return true;
    }

    protected DataSet getData(int index){
        if (ensembles.get(index).valid)
            return ensembles.get(index).data.get(0);
        return null;
    }

    protected ArrayList<DataSet> getMultipleData(int index){
        if (ensembles.get(index).valid)
            return ensembles.get(index).data;
        return null;
    }
}
