/*
 * DBInit.java
 * Created on 19.08.2015, 23:42:43
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
package jams.server.service;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

/**
 *
 * @author christian
 */
@Singleton
@Startup
public class DBInit {

    static private final Logger log = Logger.getLogger(DBInit.class.getName());

    @Resource(lookup = "jdbc/jamsserver__pm")
    private DataSource dataSource;

    public static String DATABASE_URL = null;//"e:/test_server/uploaded/";
    public static String DATABASE_USER = null;//"E:/test_server/tmp/";
    public static String DATABASE_PW = null;//"E:/test_server/exec/";
    
    static Properties p = new Properties() {
        {                  
            try {
//                File f = new File("settings.properties");
//                System.out.println(">>>>>>>>>>>>>> " + f.getAbsolutePath());
                load(new FileReader("settings.properties"));
                DATABASE_URL = getProperty("databaseUrl");
                DATABASE_USER = getProperty("databaseUser");
                DATABASE_PW = getProperty("databasePw");
            } catch (Throwable ioe) {
                log.log(Level.SEVERE, ioe.getMessage(), ioe);
                ioe.printStackTrace();
            }
        }
    }; 
    
    @PostConstruct
    private void onStartup() {
        if (dataSource == null) {
            log.severe("no datasource found to execute the db migrations!");
            throw new EJBException(
                    "no datasource found to execute the db migrations!");
        }
        
        
        Flyway flyway = new Flyway();
        flyway.setInitOnMigrate(true);
        flyway.setDataSource(DATABASE_URL, DATABASE_USER, DATABASE_PW);
        //flyway.setDataSource(dataSource);
        for (MigrationInfo i : flyway.info().all()) {
            log.info("migrate task: " + i.getVersion() + " : "
                    + i.getDescription() + " from file: " + i.getScript());
        }
        flyway.migrate();
                
    }
}
