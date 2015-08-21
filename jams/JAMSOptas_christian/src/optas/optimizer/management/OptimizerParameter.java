package optas.optimizer.management;

import java.io.Serializable;

public abstract class OptimizerParameter implements Serializable{
    private String description;
    private String name;

    public OptimizerParameter(){
        
    }

    public OptimizerParameter(String name, String description){
        this.description = description;
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    abstract public String getString();
    abstract public boolean setString(String value);
}
