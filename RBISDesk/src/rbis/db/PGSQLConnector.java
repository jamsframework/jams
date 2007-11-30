/*
 * PGSQLConnector.java
 * Created on 30. November 2007, 16:16
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
package rbis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Sven Kralisch
 */
public class PGSQLConnector {

    public PGSQLConnector() {
    }

    public static void main(String[] args) {

        String user = "postgres";
        String passwd = "admin";
        String query = "SELECT * FROM address LIMIT 1";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        try {

            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost/saaleRIS", user, passwd);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            int numberOfColumns = rsmd.getColumnCount();
            int rowCount = 1;
            while (rs.next()) {
                System.out.println("Line " + rowCount + ": ");
                for (int i = 1; i <= numberOfColumns; i++) {
                    System.out.print("\t" + rsmd.getColumnName(i) + ": ");
                    System.out.println(rs.getString(i));
                }
                System.out.println("");
                rowCount++;
            }
            stmt.close();
            con.close();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
}
