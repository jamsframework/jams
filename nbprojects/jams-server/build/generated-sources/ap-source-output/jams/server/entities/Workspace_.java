package jams.server.entities;

import jams.server.entities.User;
import jams.server.entities.WorkspaceFileAssociation;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.1.v20130918-rNA", date="2014-04-30T15:14:47")
@StaticMetamodel(Workspace.class)
public class Workspace_ { 

    public static volatile SingularAttribute<Workspace, Integer> id;
    public static volatile SingularAttribute<Workspace, Date> creation;
    public static volatile ListAttribute<Workspace, WorkspaceFileAssociation> files;
    public static volatile SingularAttribute<Workspace, Integer> readOnly;
    public static volatile SingularAttribute<Workspace, String> name;
    public static volatile SingularAttribute<Workspace, Long> workspaceSize;
    public static volatile SingularAttribute<Workspace, User> user;

}