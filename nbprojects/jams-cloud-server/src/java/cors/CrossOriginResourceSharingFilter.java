/*
 * CrossOriginResourceSharingFilter.java
 * Created on Feb 8, 2017, 1:11:55 AM
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
package cors;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * @author Christian Siegert
 */
@Provider
public class CrossOriginResourceSharingFilter implements ContainerResponseFilter {
    private static final Pattern allowedOrigin = Pattern.compile("^https?://[0-9a-zA-Z]+\\.geogr\\.uni\\-jena\\.de");

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response) {
        String origin = requestContext.getHeaderString("Origin");

        if (origin == null) {
            return;
        }

        Matcher matcher = allowedOrigin.matcher(origin);

        if (!matcher.find() && !Objects.equals(origin, "http://localhost:38081")) {
            return;
        }

        response.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "DELETE, GET, POST, PUT");
        response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
    }
}
