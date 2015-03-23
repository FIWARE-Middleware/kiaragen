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

import org.antlr.stringtemplate.StringTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.text.CharacterIterator;

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
    	File directory = new File(outputDir);
        if (directory.exists() && directory.isFile())
        {
            System.out.print("The dir with name could not be  created as it is a normal file - ");
            return false;
        } else {
            if (!directory.exists())
			{
			    if (!directory.mkdirs()); {
			    	if (directory.exists()) {
			    		return true;
			    	}
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

    public static String readFile(String fileName) throws IOException {
        try (final BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    public static String escapeString(String text, boolean addQuotes) {
        final StringBuilder result = new StringBuilder();
        if (addQuotes) {
            result.append('"');
        }
        for (int i = 0; i < text.length(); ++i) {
            final char c = text.charAt(i);
            switch (c) {
                case '\b':
                    result.append("\\b");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\"':
                    result.append("\\\"");
                    break;
                case '\'':
                    result.append("\\\'");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                default:
                    // encode non-ascii chars as unicode
                    if ((c < 32) || (c > 127)) {
                        String hex = Integer.toHexString(c);
                        result.append("\\u");
                        for (int i1 = hex.length(); i1 < 4; i1++) {
                            result.append('0');
                        }
                        result.append(hex);
                    } else {
                        result.append(c);
                    }
                    break;
            }
        }
        if (addQuotes) {
            result.append('"');
        }
        return result.toString();
    }

}
