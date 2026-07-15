/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import optas.data.SimpleEnsemble;

/**
 *
 * @author chris
 */
public class ParameterGroup implements Identifiable {

    int stack[] = null;
    //boolean parameterMap[] = null;
    double w[] = null;
    int size = 0;
    long key = 0;
    static int counter = 0;
    int id;

    SimpleEnsemble parameterIDs[];
    public int getID() {
        return id;
    }

    private void init(int n) {
        if (stack == null || stack.length != n) {
            //parameterMap == null || parameterMap.length != n){
            stack = new int[n];
            //parameterMap = new boolean[n];
        }
        //Arrays.fill(parameterMap, false);
        this.key = 0;
        this.w = null;
        this.size = 0;
    }

    private void reset() {
        for (int i = 0; i < stack.length; i++) {
            this.add(i);
        }
    }

    private void init(ParameterGroup p, boolean reset) {
        init(p.stack.length);        
        size = 0;
        this.parameterIDs = p.parameterIDs;
        w = p.w;
        if (!reset) {
            for (int i = 0; i < p.size; i++) {
                this.add(p.get(i));
            }
        }
    }

    public ParameterGroup(SimpleEnsemble[] p,int n) {
        this.parameterIDs = p;
        id = counter++;
        init(n);
        reset();
    }

    ParameterGroup(SimpleEnsemble[] p,int n, double[] w) {
        this.parameterIDs = p;
        id = counter++;
        init(n);
        this.w = w;
        reset();
    }

    private void setMap(int index, boolean value) {
        long b = 1L << index;
        if (value) {
            this.key |= b;
        } else {
            this.key |= b;
            this.key ^= b;
        }
        //this.parameterMap[index] = value;
    }

    public boolean getMap(int index) {
        long b = 1L << index;
        return (this.key & b) != 0;
    }

    public void add(int p) {
        if (getMap(p) != true) {
            setMap(p, true);
            stack[size++] = p;
        }
    }

    public void remove(int index) {
        setMap(stack[index], false);
        stack[index] = stack[--size];
    }

    public void add(ParameterGroup p) {
        for (int i = 0; i < this.stack.length; i++) {
            if (p.getMap(i) & !this.getMap(i)) {
                setMap(i, true);
                this.stack[size++] = i;
            }
        }
    }

    public void sub(ParameterGroup p) {
        this.size = 0;
        for (int i = 0; i < this.stack.length; i++) {
            if (p.getMap(i) & this.getMap(i)) {
                setMap(i, false);
            } else if (getMap(i)) {
                this.stack[size++] = i;
            }
        }
    }

    public int get(int index) {
        return stack[index];
    }

    public ParameterGroup createEmptyGroup() {
        return createEmptyGroup(new ParameterGroup(this.parameterIDs,this.stack.length));
    }

    public ParameterGroup createEmptyGroup(ParameterGroup p) {
        p.init(this, true);
        return p;
    }

    public ParameterGroup copy() {
        return copy(new ParameterGroup(this.parameterIDs,this.stack.length));
    }

    public ParameterGroup copy(ParameterGroup p) {
        p.init(this, false);
        return p;
    }

    public int getIndex(int index) {
        for (int i = 0; i < size; i++) {
            if (stack[i] == index) {
                return i;
            }
        }
        return -1;
    }

    public double weight(int index) {
        return w[index];
    }

    public double[] normalizeWeight() {        
        double norm = calcNorm();
        for (int j = 0; j < stack.length; j++) {
            if (norm != 0) {
                w[j] = w[j] / norm;
            } else {
                w[j] = 1.0 / size;
            }
        }
        return w;
    }

    public double calcNorm(){
        double sum = 0;
        for (int i=0;i<this.size;i++){
            sum += w[stack[i]];
        }
        return sum;
    }

    public int getSize() {
        return size;
    }

    public void sortByWeight() {
        boolean flip = true;
        while (flip) {
            flip = false;
            for (int i = 0; i < size - 1; i++) {
                if (this.w[stack[i]] < this.w[stack[i + 1]]) {
                    int tmp = stack[i + 1];
                    stack[i + 1] = stack[i];
                    stack[i] = tmp;
                    flip = true;
                }
            }
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "Parameter: ";
        for (int i = 0; i < this.size; i++) {
            result += parameterIDs[stack[i]] + "  ";
        }

        //result += "\nDomination:" + dominationWeight + "\n";
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterGroup) {
            ParameterGroup p = (ParameterGroup) o;
            return p.key == key;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (int) (this.key ^ (this.key >>> 32));
        return hash;
    }
}
