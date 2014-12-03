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
package com.kiara.generator.util;

import org.antlr.stringtemplate.StringTemplate;

import java.io.*;

/**
*
* @author Rafael Lara <rafaellara@eprosima.com>
*/
public class Utils
{
    
    public static String getFileNameOnly(String fileName)
    {
        int index = -1;
        String auxString = fileName, returnedValue = null;
        
        index = fileName.lastIndexOf(File.separator);
        
        if(index == -1)
            index = fileName.lastIndexOf('/');
        
        if(index != -1)
            auxString = fileName.substring(index + 1);
        
        // Remove extension
        index = auxString.lastIndexOf('.');
        if(index != -1)
        	auxString = auxString.substring(0, index);
        	
       	returnedValue = auxString;
        
        return returnedValue;
    }
    
    public static String addFileSeparator(String directory)
    {
        String returnedValue = directory;
        
        if(directory.charAt(directory.length() - 1) != File.separatorChar ||
                directory.charAt(directory.length() - 1) != '/')
            returnedValue = directory + File.separator;
        
        return returnedValue;
    }
    
    public static boolean createSrcDir(String outputDir) {
    	File directory = new File(outputDir.concat("src"+File.separator+"main"+File.separator+"java"));
        if (directory.exists() && directory.isFile())
        {
            System.out.print("The dir with name could not be  created as it is a normal file - ");
            return false;
        } else {
            if (!directory.exists())
			{
			    if (!directory.mkdirs()); {
			    	return false;
			    } 
			} 
			return true;
        }
    	
    }
    
    public static boolean writeFile(String file, StringTemplate template, boolean replace)
    {
        boolean returnedValue = false;
        
        try
        {
            File handle = new File(file);
            
            if(!handle.exists() || replace)
            {
                FileWriter fw = new FileWriter(file);
                String data = template.toString();
                fw.write(data, 0, data.length());
                fw.close();
            }
            else
            {
                System.out.println("INFO: " + file + " exists. Skipping.");
            }

            returnedValue = true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }   

        return returnedValue;
    }
    
	public static String getFileExtension(String fileName)
	{
		int lastDot = fileName.lastIndexOf(".");
		
		return fileName.substring(lastDot+1);
	}
}
