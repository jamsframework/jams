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
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;

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

    /**
     * updates an existing user (server endpoint PUT /user/{id}). Fields are taken
     * from the given object, whose id must be set; an empty/null password keeps
     * the current one.
     * @param user the user with modified fields and a valid id
     * @return the updated user
     */
    public User update(User user) {
        log(getClass(), Level.FINE, JAMS.i18n("Updating_user_with_id_{0}"), user.getId());
        return client.httpPost(urlStr + "/user/" + user.getId(), "PUT", user, User.class);
    }

    /**
     * Reads a {@code <users>} XML file and creates every user in it, in a
     * repeatable way: logins that already exist on the server are skipped, so the
     * same file can be applied again without error. The {@code <id>} values in the
     * file are ignored (the server assigns ids); passwords are hashed server-side.
     * @param xmlFile a file containing a {@code <users>} element
     * @return the number of users actually created
     * @throws Exception if the file cannot be read or parsed
     */
    public int createUsers(File xmlFile) throws Exception {
        Users users = (Users) JAXBContext.newInstance(Users.class)
                .createUnmarshaller().unmarshal(xmlFile);
        return createUsers(users);
    }

    /**
     * Creates every user in the given list, skipping logins that already exist
     * (repeatable). Passwords are hashed server-side.
     * @param users the users to create
     * @return the number of users actually created
     */
    public int createUsers(Users users) {
        Set<String> existing = new HashSet<>();
        for (User u : findAll().getUsers()) {
            existing.add(u.getLogin());
        }
        int created = 0;
        for (User u : users.getUsers()) {
            if (existing.contains(u.getLogin())) {
                log(getClass(), Level.INFO, JAMS.i18n("User_{0}_already_exists,_skipping"), u.getLogin());
                continue;
            }
            createUser(u);
            existing.add(u.getLogin());
            created++;
            log(getClass(), Level.INFO, JAMS.i18n("Created_user_{0}"), u.getLogin());
        }
        return created;
    }
}
