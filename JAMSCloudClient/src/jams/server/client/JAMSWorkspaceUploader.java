/*
 * WorkspaceHandler.java
 * Created on 23.04.2014, 13:31:28
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
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import static jams.tools.LogTools.log;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 *
 * @author christian
 */
public class JAMSWorkspaceUploader {

    private final WorkspaceController workspaceController;
    private final FileController fileController;

    public JAMSWorkspaceUploader(Controller ctrl) {
        workspaceController = ctrl.workspaces();
        fileController = ctrl.files();
    }

    /**
     * returns the classpath attribute of a manifest file
     *
     * @param f, jar file
     * @return the classpath attribute of a manifest file from the jar file f
     * @throws IOException
     */
    public static Collection<File> getClassPathFromManifest(File f) throws IOException {
        List<File> files = new ArrayList<>();
        try (JarFile jar = new JarFile(f)) {
            Manifest mf = jar.getManifest();
            Attributes a = mf.getMainAttributes();
            if (a == null) {
                return files;
            }
            String classPath = a.getValue(Attributes.Name.CLASS_PATH);
            if (classPath != null) {
                String paths[] = classPath.split(" ");
                for (String path : paths) {
                    files.add(new File(f.getParent(), path));
                }
            }
        }
        return files;
    }

    /**
     * determines the classpath of some jar files including all sub-dependencies
     *
     * @param libs
     * @return all jar files this file is depending on
     */
    public Map<File, Collection<File>> findDependendLibraries(File[] libs) {
        Map<File, Collection<File>> map = new HashMap<>();
        for (File lib : libs) {
            map.put(lib, findDependendLibraries(lib));
        }
        return map;
    }

    /**
     * determines the classpath of a jar file including all sub-dependencies
     *
     * @param f the jar file in question
     * @return all jar files this file is depending on
     */
    public Collection<File> findDependendLibraries(File f) {
        TreeSet<File> doneList = new TreeSet<>();
        TreeSet<File> libList = new TreeSet<>();
        TreeSet<File> todoList = new TreeSet<>();
        todoList.add(f);

        while (!todoList.isEmpty()) {
            File nextFile = todoList.pollFirst();

            if (doneList.contains(nextFile)) {
                continue;
            }
            if (!nextFile.exists()) {
                continue;
            }
            if (nextFile.isDirectory()) {
                File[] list = nextFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getAbsolutePath().endsWith(".jar");
                    }
                });
                todoList.addAll(Arrays.asList(list));
            } else {
                try {
                    todoList.addAll(getClassPathFromManifest(nextFile));
                } catch (IOException ioe) {
                    log(JAMSWorkspaceUploader.class, Level.WARNING, ioe.toString(), ioe);
                }   
                libList.add(nextFile);
            }    
            doneList.add(nextFile);
        }
        return libList;
    }

    private int determineRole(File f, File workspace) {
        Path workspacePath = Paths.get(workspace.getPath());
        Path filePath = Paths.get(f.getPath());
        Path relativePath = workspacePath.relativize(filePath);

        int role = jams.server.entities.WorkspaceFileAssociation.ROLE_OTHER;

        if (relativePath.startsWith("input/")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_INPUT;
        } else if (relativePath.startsWith("output/")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_OUTPUT;
        } else if (relativePath.endsWith("config.txt")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_CONFIG;
        } else if (FileTools.validateModelFile(f)) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_MODEL;
        }

        return role;
    }

    private Workspace attachFilesToWorkspace(
            Workspace ws, File baseDirectory,
            Map<File, jams.server.entities.File> mapping, int role
    ) {
        TreeSet<WorkspaceFileAssociation> wfas = new TreeSet<>();

        Path basePath = Paths.get(baseDirectory.toString());
        for (File localFile : mapping.keySet()) {
            Path path = Paths.get(localFile.getAbsolutePath());
            String relativePath = basePath.relativize(path).toString();
            jams.server.entities.File serverFile = mapping.get(localFile);
            if (serverFile == null){
                log(getClass(),Level.WARNING, JAMS.i18n("Unable_to_upload_{0}"), localFile);
                continue;
            }
            if (relativePath.startsWith("..")) {
                log(getClass(), Level.WARNING, "relative path detected, which cannot be processed {0}", localFile);
                continue;
            }
            //try to determine role automatically
            if (role < 0) {
                role = determineRole(localFile, baseDirectory);
            }
            wfas.add(new WorkspaceFileAssociation(ws, serverFile, role, relativePath));
        }
        return workspaceController.attachFiles(ws, wfas);
    }

    public Workspace getWorkspace(jams.workspace.Workspace jamsWorkspace) {
        int id = jamsWorkspace.getID();
        String title = jamsWorkspace.getTitle();

        Workspace ws = workspaceController.ensureExistance(id, title);

        return ws;
    }

    private Workspace uploadWorkspaceFiles(Workspace ws, File wsDirectory, String fileExclusion) throws IOException {
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Workspace_Files"), JAMS.i18n("Collecting"));
        Collection<File> workspaceFiles
                = FileTools.getFilesByRegEx(wsDirectory, fileExclusion, false);

        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Workspace_Files"), JAMS.i18n("Uploading"));
        Map<File, jams.server.entities.File> wsFileMapping
                = fileController.uploadFile(workspaceFiles);

        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Workspace_Files"), JAMS.i18n("Attaching"));
        ws = attachFilesToWorkspace(ws, wsDirectory, wsFileMapping, -1);
        return ws;
    }

    private Workspace uploadRuntimeLibs(Workspace ws, File jamsExecutable) throws IOException {
        //collect all files
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Runtime_Libraries"), JAMS.i18n("Collecting"));
        Collection<File> jamsRuntimeLibs
                = findDependendLibraries(jamsExecutable);

        //upload all files to server
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Runtime_Libraries"), JAMS.i18n("Uploading"));
        Map<File, jams.server.entities.File> runtimeLibMapping
                = fileController.uploadFile(jamsRuntimeLibs);

        //now do the mapping
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Runtime_Libraries"), JAMS.i18n("Attaching"));
        ws = attachFilesToWorkspace(ws, jamsExecutable.getParentFile(),
                runtimeLibMapping, WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY);

        WorkspaceFileAssociation wfa = new WorkspaceFileAssociation(ws,
                runtimeLibMapping.get(jamsExecutable),
                WorkspaceFileAssociation.ROLE_EXECUTABLE, jamsExecutable.getName());
        ws = workspaceController.attachFile(ws, wfa);
        return ws;
    }

    private Workspace uploadComponentLibs(Workspace ws, File componentLibaries[]) throws IOException {
        //collect all files
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Component_Libraries"), JAMS.i18n("Collecting"));
        Map<File, Collection<File>> librarySet = findDependendLibraries(componentLibaries);
        //upload files
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Component_Libraries"), JAMS.i18n("Uploading"));
        Map<File, Map<File, jams.server.entities.File>> fileMapping = new HashMap<>();
        for (File file : componentLibaries) {
            Collection<File> fileList = librarySet.get(file);
            fileMapping.put(file, fileController.uploadFile(fileList));
        }
        //attach files
        log(getClass(), Level.FINE, JAMS.i18n("Processing_{0}_:{1}"), JAMS.i18n("Component_Libraries"), JAMS.i18n("Attaching"));
        for (File componentLibrary : componentLibaries) {
            Map<File, jams.server.entities.File> list = fileMapping.get(componentLibrary);

            ws = attachFilesToWorkspace(ws, componentLibrary.getAbsoluteFile().getParentFile(),
                    list, WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY);
        }

        return ws;
    }

    /**
     *
     * @param jamsWorkspace
     * @param componentLibaries
     * @param jamsExecutable
     * @param fileExclusion
     * @return
     * @throws IOException
     */
    public Workspace uploadWorkspace(
            jams.workspace.Workspace jamsWorkspace,
            File componentLibaries[],
            File jamsExecutable, String fileExclusion) throws IOException {

        Workspace ws = getWorkspace(jamsWorkspace);
        File wsDirectory = jamsWorkspace.getDirectory();

        ws = workspaceController.detachAll(ws);
        
        ws = uploadWorkspaceFiles(ws, wsDirectory, fileExclusion);
        ws = uploadComponentLibs(ws, componentLibaries);
        ws = uploadRuntimeLibs(ws, jamsExecutable);

        //potentielle probleme die noch abfangen werden müssten
        //a) doppelte bibliotheken, sortierung nach neuester bibo??
        //b) was wenn das executable fehlt?
        return ws;
    }
}
