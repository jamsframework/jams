package rbis.virtualws;

public class StringValue implements DataValue {

    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    public double getDouble() throws NumberFormatException {
        return new Double(value);
    }

    public long getLong() throws NumberFormatException {
        return new Long(value);
    }

    public String getString() {
        return value;
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = "" + value;
    }

    public void setLong(long value) {
        this.value = "" + value;
    }

    public void setString(String value) {
        this.value = value;
    }

    public void setObject(Object value) {
        if (value instanceof String) {
            this.value = (String) value;
        }
    }
}

