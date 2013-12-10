package jams.worldwind.data;

import jams.worldwind.test.RandomNumbers;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class MyTimeSeriesData {

    private HashMap<Date, Object> data;
    
    public MyTimeSeriesData(int length) {
        this.data = new HashMap<>(length);
        this.fillWithTestData();
    }
    
    private void fillWithTestData() {
        Date d;
        for(int i=0;i<this.data.size();i++) {
            d = new Date(1960+i, 1, 1);
            RandomNumbers rn = new RandomNumbers(0, 100, this.data.size());
            data.put(d, rn.getDoubleValues().get(i));
            System.out.println(data.get(d));
        }
    }
    
    public Object getDataForYear(Date year) {
        return this.data.get(year);
    }
}
