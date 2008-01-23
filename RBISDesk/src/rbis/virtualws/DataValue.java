package rbis.virtualws;

public interface DataValue {

    public double getDouble();

    public long getLong();

    public Object getObject();

    public void setDouble(double value);

    public void setLong(long value);

    public void setObject(Object value);
}

