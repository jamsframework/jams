package jams.server.entities;

import jams.server.entities.User;
import jams.server.entities.Workspace;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.1.v20130918-rNA", date="2014-04-30T15:14:47")
@StaticMetamodel(Job.class)
public class Job_ { 

    public static volatile SingularAttribute<Job, Integer> id;
    public static volatile SingularAttribute<Job, Date> startTime;
    public static volatile SingularAttribute<Job, Workspace> workspace;
    public static volatile SingularAttribute<Job, Integer> PID;
    public static volatile SingularAttribute<Job, User> owner;
    public static volatile SingularAttribute<Job, String> server;

}