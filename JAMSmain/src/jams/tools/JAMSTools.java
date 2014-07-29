/*
 * JAMSTools.java
 * Created on 2. Februar 2007, 21:57
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.tools;

import jams.JAMS;
import jams.SystemProperties;
import jams.data.Attribute;
import jams.model.Component;
import java.awt.Image;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSTools {

    private static List<Image> jamsIcons;

    /**
     * Looks for a field with a given name in a class and all its superclasses
     *
     * @param clazz The class to be searched in
     * @param name The name of the field
     * @return A Field object
     * @throws java.lang.NoSuchFieldException
     */
    static public Field getField(Class clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() == null) {
                throw e;
            }
            return getField(clazz.getSuperclass(), name);
        }
    }

    /**
     * Sets a component's attribute to a value using a setter
     *
     * @param component The component
     * @param attribName The attributes name
     * @param value The value
     * @throws java.lang.SecurityException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.lang.IllegalArgumentException
     */
    public static void setAttribute(Component component, String attribName, Object value) throws SecurityException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException {
        Class<?> componentClazz = component.getClass();
        Class<?> attribClazz = value.getClass();
        String methodName = StringTools.getSetterName(attribName);
        Method m = componentClazz.getDeclaredMethod(methodName, attribClazz);
        // if we got here, the setter is existing
        m.invoke(component, value);
    }

    /**
     * Decides whether the a component's attribute is accessible or not and sets
     * the value of that attribute either directly or by calling a setter
     *
     * @param component The component
     * @param field The field representing the attributes
     * @param value The value
     * @return The new value of the attribute
     * @throws java.lang.SecurityException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.lang.IllegalArgumentException
     */
    public static Object setField(Component component, Field field, Object value) throws SecurityException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException {
        Object data;
        if (field.getModifiers() == Modifier.PUBLIC) {
            // if field is public, set it directly
            field.set(component, value);
            data = field.get(component);
        } else {
            // if field is not public, try to use a setter
            String attribName = field.getName();
            setAttribute(component, attribName, value);
            data = JAMSTools.getAttribute(component, attribName);
        }
        return data;
    }

    /**
     * Gets the value of a component's attribute
     *
     * @param component The component
     * @param attribName The name of the atribute
     * @return
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.SecurityException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.IllegalAccessException
     */
    public static Object getAttribute(Component component, String attribName) throws IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Class<?> componentClazz = component.getClass();
        String methodName = StringTools.getGetterName(attribName);
        Method m = componentClazz.getDeclaredMethod(methodName);
        Object data = m.invoke(component);
        return data;
    }

    /**
     * Exception handling method
     *
     * @param t Throwable to be handled
     */
    public static void handle(Throwable t) {
        handle(t, true);
    }

    /**
     * Exception handling method
     *
     * @param t Throwable to be handled
     * @param proceed Proceed or not?
     */
    public static void handle(Throwable t, boolean proceed) {
        t.printStackTrace();
        if (!proceed) {
            System.exit(-1);
        }
    }

    /**
     * Sets the static fields JAMS.resources to a language specific resource
     * bundle and JAMS.charset to charset defined in a JAMS property file
     *
     * @param properties JAMSProperty object containing the language information
     */
    public static void configureLocaleEncoding(SystemProperties properties) {
        // check if a different locale is forced by the config
        String forcedLocale = properties.getProperty(SystemProperties.LOCALE_IDENTIFIER);
        if (!StringTools.isEmptyString(forcedLocale)) {

            if (forcedLocale.contains("_")) {
                StringTokenizer tok = new StringTokenizer(forcedLocale, "_");
                Locale.setDefault(new Locale(tok.nextToken(), tok.nextToken()));
            } else {
                Locale.setDefault(new Locale(forcedLocale));
            }

            JAMS.setResources(java.util.ResourceBundle.getBundle("resources/i18n/JAMSBundle"));
        }

        String charset = properties.getProperty(SystemProperties.CHARSET_IDENTIFIER);
        if (!StringTools.isEmptyString(charset)) {
            JAMS.setCharset(charset);
        }
    }

    static public int[] convertJAMSArrayToArray(Attribute.Integer[] in) {
        int out[] = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i].getValue();
        }
        return out;
    }

    static public String[] convertJAMSArrayToArray(Attribute.String[] in) {
        String out[] = new String[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i].getValue();
        }
        return out;
    }

    static public double[] convertJAMSArrayToArray(Attribute.Double[] in) {
        double out[] = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i].getValue();
        }
        return out;
    }

    static public boolean cloneInto(Object dst, Object src, Class clazz) {
        if (!clazz.isInstance(dst)
                || !clazz.isInstance(src)) {
            return false;
        }
        if (clazz.getSuperclass() != null) {
            cloneInto(dst, src, clazz.getSuperclass());
        }
        Field fields[] = clazz.getDeclaredFields();
        for (Field f : fields) {
            try {
                f.setAccessible(true);
                if (Modifier.isFinal(f.getModifiers()) || 
                    Modifier.isStatic(f.getModifiers())
                     ) {
                    continue;
                }                
                f.set(dst, f.get(src));
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param dirName
     * @param fileName
     * @return
     * @deprecated Only existing for compatibility reasons. Use
     * {@link jams.tools.FileTools#createAbsoluteFileName FileTools.createAbsoluteFileName(dirName, fileName)}
     * instead!
     */
    @Deprecated
    public static String CreateAbsoluteFileName(String dirName, String fileName) {
        return FileTools.createAbsoluteFileName(dirName, fileName);
    }

    /**
     * Add a new file name to the list of recent files
     *
     * @param p a JAMS properties object
     * @param fileName the file name to be added
     */
    public static void addToRecentFiles(SystemProperties p, String key, String fileName) {

        File f = new File(fileName);
        if (f.exists()) {
            try {
                fileName = f.getCanonicalPath();
            } catch (IOException ioe) {
                fileName = f.getAbsolutePath();
            }
        }

        int maxFiles = Integer.parseInt(p.getProperty(SystemProperties.MAX_RECENT_FILES));

        List<String> recentFileNames = new ArrayList();
        String s = p.getProperty(key);
        if (s != null) {
            String[] recentArray = s.split("\\|");
            recentFileNames.addAll(Arrays.asList(recentArray));
        }

        if (recentFileNames.contains(fileName)) {
            recentFileNames.remove(fileName);
        }

        if (recentFileNames.size() == maxFiles) {
            recentFileNames.remove(maxFiles - 1);
        }

        String resultString = fileName;
        for (String recentFileName : recentFileNames) {
            resultString += "|" + recentFileName;
        }
        p.setProperty(key, resultString);
    }

    /**
     * Get the list of recently opened files
     *
     * @param p a JAMS properties object
     * @return an array containing the names of recently opened files
     */
    public static String[] getRecentFiles(SystemProperties p, String key) {

        String s = p.getProperty(key);

        if (s != null) {
            return s.split("\\|");
        } else {
            return new String[0];
        }
    }

    /**
     * Creates a JAMS icon image
     *
     * @return The icon image
     */
    public static List<Image> getJAMSIcons() {

        if (jamsIcons == null) {
            jamsIcons = new ArrayList();
            jamsIcons.add(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            jamsIcons.add(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon.png")).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            jamsIcons.add(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
            jamsIcons.add(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon.png")).getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH));
        }
        return jamsIcons;
    }

    /**
     * Find out if OS (not VM) is 64 bit
     * @return if OS (not VM) is 64 bit
     */
    public static boolean is64Bit() {
        boolean is64bit = false;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
        }
        return is64bit;
    }        
    
}
