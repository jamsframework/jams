/*
 * UserController.java
 * Created on 20.04.2014, 14:46:13
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.server.client;

import jams.JAMS;
import jams.server.entities.User;
import jams.server.entities.Users;
import static jams.tools.LogTools.log;
import java.util.logging.Level;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class UserController {
    private final HTTPClient client;
    private final String urlStr;

    /**
     * ensures the construction of a working JobController
     * @param ctrl parent controller
     */
    public UserController(Controller ctrl){
        this.client = ctrl.getClient();
        this.urlStr = ctrl.getServerURL();
    }
    
    /**
     * retrieves a list of all users
     * admin priveledges required
     * @return a list of all users
     */
    public Users findAll() {
        log(getClass(), Level.FINE, JAMS.i18n("Retrieving_list_of_users"));
        return client.httpGet(urlStr + "/user/all", Users.class);
    }

    /**
     * retrieves a list of all users in the range from "from" to "to" 
     * admin priveledges required
     * @param from lowest index (including) that should be retrieved
     * @param to highest index (including) that should be retrieved
     * @return a list of all users in the range from "from" to "to" 
     */
    public Users findInRange(int from, int to) {
        log(getClass(), Level.FINE, JAMS.i18n("Retrieving_list_of_users_from_{0}_to_{1}"), from, to);
        return client.httpGet(urlStr + "/user/" + from + "/" + to, Users.class);
    }

    /**
     * finds a user with a specific id
     * @param id to be searched for
     * @return the user with the specific id
     */
    public User find(int id) {
        log(getClass(), Level.FINE, JAMS.i18n("Retrieving_user_with_id_{0}"), id);
        return client.httpGet(urlStr + "/user/" + id, User.class);
    }

    /**
     * deletes a user
     * @param id of the user to be deleted
     * @return the deleted user
     */
    public User delete(int id) {
        log(getClass(), Level.FINE, JAMS.i18n("Deleting_user_with_id_{0}"), id);
        return client.httpPost(urlStr + "/user/" + id, "DELETE", null, User.class);
    }

    /**
     * creates a new user
     * @param user object, id of this object is not valid
     * @return the new user with updated id
     */
    public User createUser(User user) {
        log(getClass(), Level.FINE, JAMS.i18n("Creating_new_user"));
        return client.httpPost(urlStr + "/user/create", "PUT", user, User.class);        
    }
}
