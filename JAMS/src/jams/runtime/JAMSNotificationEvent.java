/*
 * JAMSNotificationEvent.java
 * Created on 14. November 2005, 09:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.runtime;

import java.util.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSNotificationEvent extends EventObject {

    protected Component source;
    protected String message;

    /** Creates a new instance of JAMSNotificationEvent */
    public JAMSNotificationEvent(Component source, String message) {
        super(source);
        this.source = source;
        this.message = message;
    }

    public Component getComponent() {
        return source;
    }
}
