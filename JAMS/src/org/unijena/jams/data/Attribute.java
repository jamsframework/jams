package org.unijena.jams.data;

public interface Attribute {

	public interface Boolean extends JAMSData {
		public boolean getValue();

		public void setValue(boolean value);
	}

	public interface BooleanArray extends JAMSData {
		public boolean[] getValue();

		public void setValue(boolean[] value);
	}

	public interface Double extends JAMSData {
		public double getValue();

		public void setValue(double value);
	}

	public interface DoubleArray extends JAMSData {
		public double[] getValue();

		public void setValue(double[] value);
	}

	public interface Float extends JAMSData {
		public float getValue();

		public void setValue(float value);
	}

	public interface FloatArray extends JAMSData {
		public float[] getValue();

		public void setValue(float[] value);
	}

	public interface Integer extends JAMSData {
		public int getValue();

		public void setValue(int value);
	}

	public interface IntegerArray extends JAMSData {
		public int[] getValue();

		public void setValue(int[] value);
	}

	public interface Long extends JAMSData {
		public long getValue();

		public void setValue(long value);
	}

	public interface LongArray extends JAMSData {
		public long[] getValue();

		public void setValue(long[] value);
	}

	public interface String extends JAMSData {
		public String getValue();

		public void setValue(String value);
	}

	public interface StringArray extends JAMSData {
		public String[] getValue();

		public void setValue(String[] value);
	}
}
