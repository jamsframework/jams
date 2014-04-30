package jams.server.entities;

import jams.server.entities.File;
import jams.server.entities.Workspace;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.1.v20130918-rNA", date="2014-04-30T15:14:47")
@StaticMetamodel(WorkspaceFileAssociation.class)
public class WorkspaceFileAssociation_ { 

    public static volatile SingularAttribute<WorkspaceFileAssociation, Integer> id;
    public static volatile SingularAttribute<WorkspaceFileAssociation, File> file;
    public static volatile SingularAttribute<WorkspaceFileAssociation, Integer> file_id;
    public static volatile SingularAttribute<WorkspaceFileAssociation, String> path;
    public static volatile SingularAttribute<WorkspaceFileAssociation, Integer> role;
    public static volatile SingularAttribute<WorkspaceFileAssociation, Integer> ws_id;
    public static volatile SingularAttribute<WorkspaceFileAssociation, Workspace> ws;

}