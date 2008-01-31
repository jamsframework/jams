package rbis.virtualws;

public class DoubleValue implements DataValue {

    private Double value;

    public double getDouble() {
        return value.doubleValue();
    }

    public long getLong() {
        return value.longValue();
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = new Double(value);
    }

    public void setLong(long value) {
        this.value = new Double(value);
    }

    public void setString(String value) {
        try {
            this.value = new Double(value);
        } catch (NumberFormatException nfe) {

        }
    }

    public void setObject(Object value) {
        if (value instanceof Double) {
            this.value = (Double) value;
        }
    }
}

