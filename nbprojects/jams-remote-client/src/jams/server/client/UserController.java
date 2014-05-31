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

import jams.server.entities.User;
import jams.server.entities.Users;

/**
 *
 * @author christian
 */
public class UserController extends Controller{    
    public UserController(HTTPClient client, String serverUrl){
        super(client, serverUrl);
    }
    
    public Users getAllUsers() {
        return (Users) client.httpGet(serverURL + "/user/all", Users.class);
    }

    public Users getUsersInRange(int from, int to) {
        return (Users)client.httpGet(serverURL + "/user/" + from + "/" + to, Users.class);
    }

    public User getUser(int id) {
        return (User) client.httpGet(serverURL + "/user/" + id, User.class);
    }

    public User removeUser(int id) {
        return (User)client.httpPost(serverURL + "/user/" + id, "DELETE", null, User.class);
    }

    public User createUser(User user) {
        return (User)client.httpPost(serverURL + "/user/create", "PUT", user, User.class);        
    }
}
