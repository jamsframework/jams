/*
 * TSDataStore.java
 * Created on 23. Januar 2008, 15:53
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package rbis.virtualws.stores;

import org.unijena.jams.data.JAMSTimeInterval;
import org.unijena.jams.data.JAMSCalendar;
import rbis.virtualws.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rbis.virtualws.datatypes.CalendarValue;
import rbis.virtualws.datatypes.DataValue;

/**
 *
 * @author Sven Kralisch
 */
public class TSDataStore extends TableDataStore {

    private JAMSTimeInterval ti;
    private CalendarValue calendar;
    private JAMSCalendar currentDate;

    public TSDataStore(VirtualWorkspace ws, Document doc) {
        super(ws, doc);

        Element tiNode = (Element) doc.getElementsByTagName("timeinterval").item(0);
        Element startElement = (Element) tiNode.getElementsByTagName("start").item(0);
        Element endElement = (Element) tiNode.getElementsByTagName("end").item(0);
        Element stepsizeElement = (Element) tiNode.getElementsByTagName("stepsize").item(0);

        JAMSCalendar start = new JAMSCalendar();
        start.setValue(startElement.getAttribute("value"));

        JAMSCalendar end = new JAMSCalendar();
        end.setValue(endElement.getAttribute("value"));

        int timeUnit = Integer.parseInt(stepsizeElement.getAttribute("unit"));
        int timeUnitCount = Integer.parseInt(stepsizeElement.getAttribute("count"));

        ti = new JAMSTimeInterval(start, end, timeUnit, timeUnitCount);

        currentDate = ti.getStart();
        currentDate.add(timeUnit, -1*timeUnitCount);
        calendar = new CalendarValue(currentDate);
    }

    @Override
    public DataSet getNext() {

        DataSet result = new DataSet(positionArray.length + 1);

        currentDate.add(ti.getTimeUnit(), ti.getTimeUnitCount());
        result.setData(0, calendar);

        for (int i = 0; i < positionArray.length; i++) {

            DataSet ds = dataIOArray[i].getValues()[currentPosition];
            DataValue[] values = ds.getData();
            result.setData(i + 1, values[positionArray[i]]);

        }

        currentPosition++;

        return result;
    }
}

