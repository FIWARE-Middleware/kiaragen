/* KIARA - Middleware for efficient and QoS/Security-aware invocation of services and exchange of messages
 *
 * Copyright (C) 2014 Proyectos y Sistemas de Mantenimiento S.L. (eProsima)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fiware.kiara.generator.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.antlr.stringtemplate.StringTemplate;

/**
*
* @author Rafael Lara {@literal <rafaellara@eprosima.com>}
*/
public class Utils
{
    
    public static String baseFilename(String fileName)
    {
        return baseFilename(new File(fileName));
    }

    public static String baseFilename(File file) {
        if (file==null) throw new IllegalArgumentException("Null value not allowed");
        
        String filename = file.getName();
        int index;
        if ((index = filename.lastIndexOf('.')) != -1) {
            filename = filename.substring(0,index);
        }
        return filename;
    }
    
    public static String fileExtension(String filename) {
        if (filename==null) throw new IllegalArgumentException("Null value not allowed");
        int index;
        return ((index = filename.lastIndexOf('.')) != -1)? 
                filename.substring(index +1) : ""; 
    }
    
    public static String fileExtension(File file) {
        if (file==null) throw new IllegalArgumentException("Null value not allowed");
        return fileExtension(file.getName());
    }
    
    public static String addFileSeparator(String directory)
    {
        return (directory.endsWith(File.separator) || directory.endsWith("/")) ?
                directory : directory + File.separator;
    }
    
    public static boolean createDir(String outputDir) {
        return createDir(new File(outputDir));
    }
   
    public static boolean createDir(File directory) {
        if (directory == null) throw new IllegalArgumentException("Null value not allowed");
        if (directory.isFile()) {
            System.out.print("The directory could not be created because it is a normal file - ");
            return false;
        } else {
            if (!directory.exists() && !directory.mkdirs()) {
                return directory.exists();
            }
            return true;
        }
    }
    
    public static boolean writeFile(String filename, StringTemplate template, boolean replace)
    {
        return writeFile(new File(filename), template, replace);
    }
    
    public static boolean writeFile(File file, StringTemplate template, boolean replace) {
        boolean success=true;
        
        if(!file.exists() || replace) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(template.toString());
                System.out.println("INFO:  Writing file "+ file +" successfull");
            } catch(IOException e) {
                System.out.println("ERROR: Writing file "+ file + "failed : " + e.getMessage());
                success = false;
            }
        } else {
            System.out.println("INFO: " + file + " exists. Skipping.");
        }
        return success;
    }
    
}
