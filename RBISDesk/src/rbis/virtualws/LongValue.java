package rbis.virtualws;

public class LongValue implements DataValue {

    private Long value;

    public LongValue(long value) {
        this.value = new Long(value);
    }

    public double getDouble() {
        return new Double(value);
    }

    public long getLong() {
        return value;
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = new Long((long)value);
    }

    public void setLong(long value) {
        this.value = new Long(value);
    }

    public void setString(String value) throws NumberFormatException {
        this.value = new Long(value);
    }

    public void setObject(Object value) {
        if (value instanceof Long) {
            this.value = (Long) value;
        }
    }
}

