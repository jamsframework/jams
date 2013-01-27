package optas.optimizer.management;

public class NumericOptimizerParameter extends OptimizerParameter {
    private double value;
    private double lowerBound;
    private double upperBound;

    public NumericOptimizerParameter(){

    }
    public NumericOptimizerParameter(String name, String desc, double value, double lowerBound, double upperBound) {
        setName(name);
        setDescription(desc);
        this.value = value;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the lowerBound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * @param lowerBound the lowerBound to set
     */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * @return the upperBound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * @param upperBound the upperBound to set
     */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }
    
    public String getString(){
        return this.getName() + "=" + this.value;
    }
    
    public boolean setString(String value){
        try{
            this.value = Double.parseDouble(value);
        }catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }
}
