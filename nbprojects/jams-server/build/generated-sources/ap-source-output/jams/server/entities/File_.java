package jams.server.entities;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.1.v20130918-rNA", date="2014-04-23T10:35:01")
@StaticMetamodel(File.class)
public class File_ { 

    public static volatile SingularAttribute<File, Integer> id;
    public static volatile SingularAttribute<File, Date> creation;
    public static volatile SingularAttribute<File, Integer> referenceCounter;
    public static volatile SingularAttribute<File, String> hash;

}