/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.MCAT5;


import jams.JAMS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.data.Ensemble;



/**
 *
 * @author chris
 */
public abstract class MCAT5Plot {
    final int MAXIMUM_WIDTH = 2000;
    final int MAXIMUM_HEIGHT = 2000;
    
    public static class NoDataException extends Exception{
        public SimpleRequest r;
        public String error;

        public NoDataException(SimpleRequest r) {
            this.r = r;
        }
        public NoDataException(String r) {
            this.error = r;
        }

        @Override
        public String toString(){
            if (r!=null)
                return "no data for " + r.clazz;
            else
                return error;
        }
    }

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

    public abstract void refresh() throws NoDataException;
    public void redraw(){
        try {
            refresh();
        } catch (NoDataException e) {
            JOptionPane.showMessageDialog(getPanel(), JAMS.i18n("Failed_to_show_dataset_The_data_is_incommensurate!"));
        }
    }
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

    public ArrayList<DataSet>[] compile(int[] list) throws NoDataException{
        Set<Integer> commonIds = new HashSet<Integer>();
        if (!isRequestFulfilled())
            return null;

        boolean firstIteration = true;
        for (int i=0;i<list.length;i++){
            Result r = this.ensembles.get(list[i]);

            for (DataSet d : r.data){
                Set<Integer> ids = new HashSet<Integer>();

                if (d instanceof Ensemble){
                    Ensemble ensemble = (Ensemble)d;
                    ids.addAll(Arrays.asList(ensemble.getIds()));

                    if (firstIteration) {
                        commonIds.addAll(ids);
                        firstIteration = false;
                    } else {
                        commonIds.retainAll(ids);
                    }
                }
            }
        }

        ArrayList<DataSet> set[] = new ArrayList[list.length];

        for (int i=0;i<list.length;i++){
            Result r = this.ensembles.get(list[i]);
            set[i] = new ArrayList<DataSet>();

            for (DataSet d : r.data){
                if (d instanceof Ensemble){
                    Ensemble ensemble = ((Ensemble)d).clone();
                    Integer ids[] = ensemble.getIds();
                    for (Integer id : ids){
                        if (!commonIds.contains(id)){
                            ensemble.removeId(id);
                        }
                    }
                    if (ensemble.getSize()<=0){
                        throw new NoDataException("the selected datasets are incommensurate");
                    }
                    set[i].add(ensemble);
                }else{
                    set[i].add(d);
                }
            }
        }
        return set;
    }

    protected ArrayList<DataSet>[] getData(int index[]) throws NoDataException{
        return compile(index);        
    }    
}
