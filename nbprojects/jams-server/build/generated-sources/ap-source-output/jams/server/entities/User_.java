package jams.server.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.1.v20130918-rNA", date="2014-04-30T15:14:47")
@StaticMetamodel(User.class)
public class User_ { 

    public static volatile SingularAttribute<User, Integer> id;
    public static volatile SingularAttribute<User, String> email;
    public static volatile SingularAttribute<User, Integer> admin;
    public static volatile SingularAttribute<User, String> name;
    public static volatile SingularAttribute<User, String> login;
    public static volatile SingularAttribute<User, String> password;

}