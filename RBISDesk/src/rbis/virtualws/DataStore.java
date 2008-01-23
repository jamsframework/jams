package rbis.virtualws;


public interface DataStore {

    public String getTitle ();

    public boolean hasNext ();

    public DataSet getNext ();

    public String getDescription ();

}

