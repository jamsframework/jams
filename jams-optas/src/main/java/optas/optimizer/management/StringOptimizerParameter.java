package optas.optimizer.management;

public class StringOptimizerParameter extends OptimizerParameter {

    private String value;

    public StringOptimizerParameter(){
        
    }
    public StringOptimizerParameter(String name, String desc, String value) {
        setName(name);
        this.setDescription(desc);
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    
      public String getString() {
        return this.getName() + "=" + value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public boolean setString(String value){
        this.value = value;        
        return true;
    }
}
