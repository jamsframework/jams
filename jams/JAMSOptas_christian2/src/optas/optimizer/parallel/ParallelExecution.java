/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.parallel;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author chris
 */
public class ParallelExecution<X,Y> {
    byte[] myWorkspace;
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(64);

    ThreadPoolExecutor threadPool = null;

    private byte[] zipDirectory(String dir,String exclude) throws IOException {
        ByteArrayOutputStream zipAsByteArray = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(zipAsByteArray);

        File dirZip = new File(jams.tools.FileTools.createAbsoluteFileName(dir,""));

        zipFiles(dirZip, dirZip, zipOut, exclude);
        zipOut.close();

        return zipAsByteArray.toByteArray();
    }

    private void zipFiles(File rootDir,File dirZip, ZipOutputStream zipOut, String exclude) throws IOException {
        byte[] buf = new byte[4096];
        File[] fileArray = dirZip.listFiles();
        String fileName = "";

        for (int i = 0; i < fileArray.length; i++)  {
            fileName = fileArray[i].getAbsolutePath().replace(rootDir.getAbsolutePath(), "");
            System.out.print("old filename:" + fileArray[i].getAbsolutePath() + " new filename:" + fileName + " dirZip: " + dirZip.getAbsolutePath());
            if (fileName.matches(exclude))
                continue;
            if (fileArray[i].isDirectory()){
                zipFiles(rootDir,fileArray[i], zipOut,exclude);
            }
            else{
                FileInputStream inFile = new FileInputStream(fileArray[i].getAbsolutePath()/*jams.tools.FileTools.createAbsoluteFileName(currentDir,fileName)*/);
                zipOut.putNextEntry(new ZipEntry(fileName));

                int len;
                // Der Inhalt der Datei wird in die Zip-Datei kopiert.
                while ((len = inFile.read(buf)) > 0){
                    zipOut.write(buf, 0, len);
                }
                inFile.close();
            }
        }
    }

    private File buildDirectoryHierarchyFor(String entryName, File destDir) {
        entryName = entryName.replace("\\", "/");
        int lastIndex = entryName.lastIndexOf('/');
        //String entryFileName = entryName.substring(lastIndex + 1);
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        //System.out.println("extract to_:" + internalPathToEntry + " file:" + entryFileName);
        return new File(destDir, internalPathToEntry);
    }

    private void unzipDirectory(byte[] zip, String destDirPath) throws IOException {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(zip));
        byte[] buffer = new byte[16384];
        int len;
        ZipEntry entry = null;
        while ((entry = zipFile.getNextEntry()) != null) {
            String entryFileName = entry.getName();
            entryFileName = entryFileName.replace("\\", "/");
            File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(new File(destDir, entryFileName)));

                while ((len = zipFile.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();
            }
        }
        zipFile.close();
    }

    public ParallelExecution(File workspace, String excludeFiles){
        try {
            myWorkspace = zipDirectory(workspace.getAbsolutePath(), excludeFiles);
        } catch (Exception e) {
            log("cant zip directory: " + e.toString());
            myWorkspace = null;
        }
    }

    private byte[] toByteArrayStream(Serializable s){
        try{
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            oos.close();
            fos.close();

            return fos.toByteArray();
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.toString());
        }
        return null;
    }

    private static Serializable createFromByteArray(byte[] array){
        try{
            ByteArrayInputStream fis = new ByteArrayInputStream(array);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Serializable o = (Serializable)ois.readObject();
            ois.close();
            return o;
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.toString());
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            System.out.println(cnfe.toString());
        }
        return null;
    }

    private Serializable clone (Serializable s){
        return createFromByteArray(toByteArrayStream(s));
    }

    public static abstract class ParamRunnable<Y> implements Runnable{
        ParallelJob job;
        final ArrayList<Y> sharedResultList;

        ParamRunnable(ParallelJob job, ArrayList<Y> sharedResultList){
            this.job = job;
            this.sharedResultList = sharedResultList;
        }
    }

    static long instanceTime = System.nanoTime();
    
    public Y execute(X arg, ParallelTask<X,Y> task, int gridSize){

        ArrayList<ParallelJob> jobs = task.split(arg, gridSize);
        ArrayList<Y> results = new ArrayList<Y>();

        threadPool = new ThreadPoolExecutor(gridSize, gridSize, 30, TimeUnit.SECONDS, workQueue);
        
        String dstDirectory = System.getProperty("java.io.tmpdir") + "/ram/" + instanceTime + "/"; //hope this avoids conflicts

        for (int i=0;i<jobs.size();i++){
            ParallelJob clonedJob = (ParallelJob)clone(jobs.get(i));

            String workingPath = dstDirectory + System.nanoTime() + "/";
            File workingDirectory = new File(workingPath);
            
            try {
                unzipDirectory(myWorkspace, workingPath);
            } catch (IOException e) {
                e.printStackTrace();
                log("cant extract zip: " + e.toString());
                return null;
            }

            File outputDirectory = new File(workingPath + "/output/");
            outputDirectory.mkdir();

            clonedJob.moveWorkspace(workingDirectory);

            Runnable r = new ParamRunnable<Y>(clonedJob, results) {

                public void run() {
                    Y result = (Y)this.job.execute();
                    synchronized(sharedResultList){
                        if (result!=null)
                            sharedResultList.add(result);
                    }
                }
            };
            threadPool.execute(r);
            //log("Starting new task! -- There are " + workQueue.size() + " tasks in workQueue!");
        }
        threadPool.shutdown();
        
        try{
            threadPool.awaitTermination(100, TimeUnit.HOURS);
        }catch(InterruptedException ie){
            System.out.println("Serious problem with thread pool .. was interrupted");
            ie.printStackTrace();
        }

        Y result = task.reduce(results);

        File dstDir = new File(dstDirectory);
        for (File f : dstDir.listFiles()){
            if (f.isDirectory()){
                deleteDir(f);
            }
        }

        return result;
    }

    private void log(String msg){
        System.out.println(msg);
    }

    public void deleteDir(File dir) {

        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDir(files[i]); // Verzeichnis leeren und anschließend löschen
                } else {
                    files[i].delete(); // Datei löschen
                }
            }
            dir.delete(); // Ordner löschen
        }
    }

    public static void addJarsToClassPath(ClassLoader classLoader, File folder) {
        if (classLoader instanceof URLClassLoader) {
            try {
                if (folder.exists()) {
                    if (folder.isFile()) {
                        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                        addUrlMethod.setAccessible(true);
                        if (null != addUrlMethod) {
                            System.out.println(folder.toString());
                            addUrlMethod.invoke((Object) classLoader, new Object[]{folder.toURI().toURL()});
                        }
                    } else {
                        File[] jarFiles = folder.listFiles(new FileFilter() {

                            public boolean accept(File arg0) {
                                if (arg0.toString().lastIndexOf(".jar") != -1) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                        addUrlMethod.setAccessible(true);
                        if (null != addUrlMethod) {
                            for (int i = 0; i < jarFiles.length; i++) {
                                System.out.println(jarFiles[i].toString());
                                addUrlMethod.invoke((Object) classLoader, new Object[]{jarFiles[i].toURI().toURL()});
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
