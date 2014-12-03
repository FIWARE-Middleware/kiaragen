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

package com.kiara.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.eprosima.idl.generator.manager.TemplateGroup;
import com.eprosima.idl.generator.manager.TemplateManager;
import com.eprosima.idl.parser.grammar.KIARAIDLLexer;
import com.eprosima.idl.parser.grammar.KIARAIDLParser;
import com.eprosima.idl.parser.tree.Definition;
import com.eprosima.idl.parser.tree.Interface;
import com.eprosima.idl.parser.tree.Specification;
import com.eprosima.idl.parser.tree.TypeDeclaration;
import com.eprosima.idl.parser.typecode.StructTypeCode;
import com.eprosima.idl.parser.typecode.TypeCode;
import com.eprosima.idl.util.Util;
import com.eprosima.log.ColorMessage;
import com.kiara.generator.exceptions.*;
import com.kiara.generator.idl.grammar.Context;
import com.kiara.generator.util.Utils;

/**
*
* @author Rafael Lara <rafaellara@eprosima.com>
*/
public class kiaragen {
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Attributes
	 */
	
	private static ArrayList<String> m_platforms = null;
	
	private Vector<String> m_idlFiles;
	protected static String m_appEnv = "EPROSIMARTPSHOME";
	private String m_exampleOption = null;
	private String m_languageOption = "Java";
    private boolean m_ppDisable = false; 
    private boolean m_replace = false;
    private String m_ppPath = null;
    private final String m_defaultOutputDir = "." + File.separator;
    private String m_outputDir = m_defaultOutputDir;
    private String m_tempDir = null;
    protected static String m_appName = "kiaragen";
    
    private boolean m_publishercode = true;
    private boolean m_subscribercode = true;
    protected static String m_localAppProduct = "Kiara";

    private ArrayList<String> m_includePaths = new ArrayList<String>();

    private String m_os = null;
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Constructor
	 */
	
	public kiaragen(String [] args) throws BadArgumentException {
		
		int count = 0;
		String arg;
		
		// Detect OS
		m_os = System.getProperty("os.name");
		
		m_idlFiles = new Vector<String>();
		
		// Check arguments
		while (count < args.length) {
			
			arg = args[count++];
			
			if (!arg.startsWith("-")) {
				m_idlFiles.add(arg);
			} else if (arg.equals("-example")) {
				if (count < args.length) {
					m_exampleOption = args[count++];
					if (!m_platforms.contains(m_exampleOption)) {
						throw new BadArgumentException("Unknown example arch " + m_exampleOption);
					}
				} else {
					throw new BadArgumentException("No architecture speficied after -example argument");
				}
			} else if (arg.equals("-language")) {
				if (count < args.length) {
					m_languageOption = args[count++];
					if (!m_languageOption.equals("C++") && !m_languageOption.equals("c++")) {
						throw new BadArgumentException("Unknown language " + m_languageOption);
					}
				} else {
					throw new BadArgumentException("No language specified after -language argument");
				}
			} else if(arg.equals("-ppPath")) {
				if (count < args.length) {
					m_ppPath = args[count++];
				} else {
					throw new BadArgumentException("No URL specified after -ppPath argument");
				}
			} else if (arg.equals("-ppDisable")) {
				m_ppDisable = true;
			} else if (arg.equals("-replace")) {
				m_replace = true;
			} else if (arg.equals("-d")) {
				if (count < args.length) {
					m_outputDir = Utils.addFileSeparator(args[count++]);
				} else {
					throw new BadArgumentException("No URL specified after -d argument");
				}
			} else if (arg.equals("-version")) {
				showVersion();
				System.exit(0);
			} else if (arg.equals("-help")) {
				printHelp();
				System.exit(0);
			} else { 
				throw new BadArgumentException("Unknown argument " + arg);
			}
			
		}
		
		if (m_idlFiles.isEmpty()) {
			throw new BadArgumentException("No input files given");
		}
		
	}
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Listener classes
	 */
	
	class TemplateErrorListener implements StringTemplateErrorListener
    {  
        public void error(String arg0, Throwable arg1)
        {
            System.out.println(ColorMessage.error() + arg0);
            arg1.printStackTrace();
        }

        public void warning(String arg0)
        {
            System.out.println(ColorMessage.warning() + arg0);   
        }   
    }
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Main methods
	 */
	
	public boolean execute() {
		
	
		if (!m_outputDir.equals(m_defaultOutputDir)) {
			File dir = new File(m_outputDir);
			
			if (!dir.exists()) {
				System.out.println(ColorMessage.error() + "The specified output directory does not exist");
				return false;
			}
		}
		
		boolean returnedValue = globalInit();
		
		if (returnedValue) {
			
			// Load string templates
			System.out.println("Loading templates...");
			TemplateManager.setGroupLoaderDirectories("com/kiara/generator/idl/templates");
			
			System.out.println(this.getClass().getCanonicalName());
			System.out.println(Context.class.getCanonicalName());
			
			for (int count = 0; returnedValue && (count < m_idlFiles.size()); ++count) {
				boolean result = process(m_idlFiles.get(count));
				
				if (result) {
					returnedValue = result;
				} else {
					returnedValue = false;
				}
			}
			
		}
		
		return returnedValue;
		
	}
	
	
	
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Auxiliary methods
	 */
	
	private String getVersion()
    {
        try
        {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream("eprosimartps_version.h");
            byte[] b = new byte[input.available()];
            input.read(b);
            String text = new String(b);
            int beginindex = text.indexOf("\"");
            int endindex = text.indexOf("\"", beginindex + 1);
            return text.substring(beginindex + 1, endindex);
        }
        catch(Exception ex)
        {
            System.out.println(ColorMessage.error() + "Getting version. " + ex.getMessage());
        }

        return "";
    }
	
	private void showVersion()
    {
        String version = getVersion();
		System.out.println(m_appName + " version " + version);
    }
	
	public static void printHelp()
    {
        System.out.println(m_appName + " usage:");
        System.out.println("\t" + m_appName + " [options] <file> [<file> ...]");
        System.out.println("\twhere the options are:");
        System.out.println("\t\t-help: shows this help");
        System.out.println("\t\t-version: shows the current version of KIARAGEN");
		System.out.println("\t\t-example <platform>: Generates a solution for a specific platform.");
        System.out.println("\t\t\tSupported platforms:");
        for(int count = 0; count < m_platforms.size(); ++count)
            System.out.println("\t\t\t * " + m_platforms.get(count));
        System.out.println("\t\t-replace: replaces existing generated files.");
        System.out.println("\t\t-ppDisable: disables the preprocessor.");
        System.out.println("\t\t-ppPath: specifies the preprocessor path.");
        System.out.println("\t\t-d <path>: sets an output directory for generated files.");
		System.out.println("\t\t-t <temp dir>: sets a specific directory as a temporary directory.");
        System.out.println("\tand the supported input files are:");
        System.out.println("\t* IDL files.");
        
    }
	
	public boolean globalInit() {
		// Set the temporary folder
		if (m_tempDir == null) {
			if (m_os.contains("Windows")) {
				String tempPath = System.getenv("TEMP");
				
				if (tempPath == null) {
					tempPath = System.getenv("TMP");
				}

				m_tempDir = tempPath;
			} else if (m_os.contains("Linux")) {
				m_tempDir = "/tmp/";
			}
		}
		
		if (m_tempDir.charAt(m_tempDir.length() - 1) != File.separatorChar) {
			m_tempDir += File.separator;
		}
		
		return true;
	}
	
	private boolean process(String idlFilename) {
		
		System.out.println("Processing the file " + idlFilename + "...");
		
		try {
			// Protocol CDR
			parseIDLtoCDR(idlFilename); 
			return true;
		} catch (Exception ioe) {
			System.out.println(ColorMessage.error() + "Cannot generate the files");
			if (!ioe.getMessage().equals("")) {
				System.out.println(ioe.getMessage());
			}
		} 
		
		return false;
		
	}
	
	private void parseIDLtoCDR(String idlFilename) {
		boolean returnedValue = false;
		String idlParseFileName = idlFilename;
		
		String onlyFileName = Util.getIDLFileNameOnly(idlFilename);
		
		if (!m_ppDisable) {
			idlParseFileName = callPreprocessor(idlFilename);
		}
		
		if (idlParseFileName != null) {
			Context ctx = new Context(onlyFileName, idlFilename, m_includePaths, m_subscribercode, m_publishercode, m_localAppProduct);
			
			// Create template manager
			TemplateManager tmanager = new TemplateManager("eprosima:Common");
			
			tmanager.changeCppTypesTemplateGroup("JavaTypes");
			
			// Load ServerExample template
			tmanager.addGroup("KIARAServerExample");
			
			// Load Servant template
			tmanager.addGroup("KIARAServerExampleGradle");
			
			// Load ServantExample template
			tmanager.addGroup("KIARAServantExample");
			
			// Load Servant template
			tmanager.addGroup("KIARAServant");
			
			// Load Servant template
			tmanager.addGroup("KIARAExample");
			
			// Load Servant template
			tmanager.addGroup("KIARAExampleAsync");
			
			// Load Servant template
			tmanager.addGroup("KIARAClient");
			
			// Load Servant template
			tmanager.addGroup("KIARAProxy");
			
			// Load Servant template
			tmanager.addGroup("KIARAClientExample");
			
			// Load Servant template
			tmanager.addGroup("KIARAClientExampleGradle");
			
			// Load Support class template
			tmanager.addGroup("KIARASupportType");
			
			// Create main template
			TemplateGroup maintemplates = tmanager.createTemplateGroup("main");
			maintemplates.setAttribute("ctx", ctx);
			
			try {
				ANTLRFileStream input = new ANTLRFileStream(idlParseFileName);
				KIARAIDLLexer lexer = new KIARAIDLLexer(input);
				lexer.setContext(ctx);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				KIARAIDLParser parser = new KIARAIDLParser(tokens);
				// Pass the finelame without the extension
				Specification specification = parser.specification(ctx, tmanager, maintemplates).spec;
				returnedValue = specification != null;
				
			} catch (FileNotFoundException ex) {
				System.out.println(ColorMessage.error("FileNotFounException") + "The File " + idlParseFileName + " was not found.");
			} catch (Exception ex) {
				System.out.println(ColorMessage.error("Exception") + ex.getMessage());
			}
			
			if (returnedValue) {
				
				System.out.println("Generating Type support classes... ");
				
				for (Definition d: ctx.getDefinitions()) {
					// Check if it is a structure
					if (d.isIsTypeDeclaration()) {
						TypeDeclaration type = (TypeDeclaration) d;
						TypeCode tc = type.getTypeCode();
						if (tc.getKind() == 0x0000000a) { // Struct
							StructTypeCode st = (StructTypeCode) tc;
							ctx.setCurrentSt(st);
							System.out.print("Generating Type support class for structure " + st.getName() +"... ");
							if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + st.getName() + ".java", maintemplates.getTemplate("KIARASupportType"), m_replace)) {
								System.out.println("OK");
							}
						}
						
					}
				}
				
				if (m_exampleOption != null) {
					
					for (Interface ifz: ctx.getInterfaces()) {
						ctx.setCurrentIfz(ifz);
						
						System.out.print("Generating application main entry files for interface " + ifz.getName() +"... ");
						if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + ".java", maintemplates.getTemplate("KIARAExample"), m_replace)) {
							if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + "Async.java", maintemplates.getTemplate("KIARAExampleAsync"), m_replace)) {
								if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + "Client.java", maintemplates.getTemplate("KIARAClient"), m_replace)) {
									System.out.println("OK");
								}
							}
						}
						
						System.out.print("Generating specific server side files for interface " + ifz.getName() +"... ");
						if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + "Servant.java", maintemplates.getTemplate("KIARAServant"), m_replace)) {
							if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + "ServantExample.java", maintemplates.getTemplate("KIARAServantExample"), m_replace)) {
								System.out.println("OK");
							}
						}
						
						
						System.out.print("Generating specific client side files for interface " + ifz.getName() +"... ");
						if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + ifz.getName() + "Proxy.java", maintemplates.getTemplate("KIARAProxy"), m_replace)) {
							System.out.println("OK");
						}
						
					}
					
					Interface ifz_handler = ctx.getFirstInterface();
					ctx.setCurrentIfz(ifz_handler);
					
					System.out.print("Generating common server side files... ");
					if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + "ServerExample.java", maintemplates.getTemplate("KIARAServerExample"), m_replace)) {
						if (returnedValue = Utils.writeFile(m_outputDir + "build_server.gradle", maintemplates.getTemplate("KIARAServerExampleGradle"), m_replace)) {
							System.out.println("OK");
							
						}
						
					}
					
					System.out.print("Generating common client side files... ");
					if (returnedValue = Utils.writeFile(m_outputDir + "/src/main/java/" + "ClientExample.java", maintemplates.getTemplate("KIARAClientExample"), m_replace)) {
						if (returnedValue = Utils.writeFile(m_outputDir + "build_client.gradle", maintemplates.getTemplate("KIARAClientExampleGradle"), m_replace)) {
							System.out.println("OK");
						}
					}
					
				}
			}
			
		}
		
	}
	
	String callPreprocessor(String idlFilename) {
		final String METHOD_NAME = "callPreprocessor";
		
		// Set line command.
		ArrayList<String> lineCommand = new ArrayList<String>();
		String [] lineCommandArray = null;
		String outputfile = Util.getIDLFileOnly(idlFilename) + ".cc";
		int exitVal = -1;
		OutputStream of = null;
		
		// Use temp directory.
		if (m_tempDir != null) {
			outputfile = m_tempDir + outputfile;
		}
		
		if (m_os.contains("Windows")) {
			try {
				of = new FileOutputStream(outputfile);
			} catch (FileNotFoundException ex) {
				System.out.println(ColorMessage.error(METHOD_NAME) + "Cannot open file " + outputfile);
				return null;
			}
		}
		
		// Set the preprocessor path
		String ppPath = m_ppPath;
		
		if (ppPath == null) {
			if (m_os.contains("Windows")) {
				ppPath = "cl.exe";
			} else if (m_os.contains("Linux")) {
				ppPath = "cpp";
			}
		}
		
		// Add command
		lineCommand.add(ppPath);
		
		// Add the include paths given as parameters.
		for (int i=0; i < m_includePaths.size(); ++i) {
			if (m_os.contains("Windows")) {
				lineCommand.add(((String) m_includePaths.get(i)).replaceFirst("^-I", "/I"));
			} else if (m_os.contains("Linux")) {
				lineCommand.add(m_includePaths.get(i));
			}
		}
		
		if (m_os.contains("Windows")) {
			lineCommand.add("/E");
			lineCommand.add("/C");
		}
		
		// Add input file.
		lineCommand.add(idlFilename);
		
		if(m_os.contains("Linux")) {
			lineCommand.add(outputfile);
		}
		
		lineCommandArray = new String[lineCommand.size()];
		lineCommandArray = (String[])lineCommand.toArray(lineCommandArray);
		
		try {
			Process preprocessor = Runtime.getRuntime().exec(lineCommandArray);
			ProcessOutput errorOutput = new ProcessOutput(preprocessor.getErrorStream(), "ERROR", false, null);
			ProcessOutput normalOutput = new ProcessOutput(preprocessor.getInputStream(), "OUTPUT", false, of);
			errorOutput.start();
			normalOutput.start();
			exitVal = preprocessor.waitFor();
			errorOutput.join();
			normalOutput.join();
		} catch (Exception e) {
			System.out.println(ColorMessage.error(METHOD_NAME) + "Cannot execute the preprocessor. Reason: " + e.getMessage());
			return null;
		}
		
		if (of != null) {
			try {
				of.close();
			} catch (IOException e) {
				System.out.println(ColorMessage.error(METHOD_NAME) + "Cannot close file " + outputfile);
			}
			
		}
		
		if (exitVal != 0) {
			System.out.println(ColorMessage.error(METHOD_NAME) + "Preprocessor return an error " + exitVal);
			return null;
		}
		
		return outputfile;
	}
	
	/*
	 * ----------------------------------------------------------------------------------------
	 * 
	 * Main entry point
	 */
	
	public static void main(String[] args) {
		ColorMessage.load();
		
		m_platforms = new ArrayList<String>();
		m_platforms.add("gradle");
		
		try {
			
			kiaragen main = new kiaragen(args);
			if (main.execute()) {
				System.exit(0);
			}
			
		} catch (BadArgumentException e) {
			
			System.out.println(ColorMessage.error("BadArgumentException") + e.getMessage());
            printHelp();
			
		}
			
		System.exit(-1);
	}
	
}

class ProcessOutput extends Thread
{
    InputStream is = null;
    OutputStream of = null;
    String type;
    boolean m_check_failures;
    boolean m_found_error = false;
    final String clLine = "#line";

    ProcessOutput(InputStream is, String type, boolean check_failures, OutputStream of)
    {
        this.is = is;
        this.type = type;
        m_check_failures = check_failures;
        this.of = of;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                if(of == null)
                    System.out.println(line);
                else
                {
                    // Sustituir los \\ que pone cl.exe por \
                    if(line.startsWith(clLine))
                    {
                        line = "#" + line.substring(clLine.length());
                        int count = 0;
                        while((count = line.indexOf("\\\\")) != -1)
                        {
                            line = line.substring(0, count) + "\\" + line.substring(count + 2);
                        }
                    }

                    of.write(line.getBytes());
                    of.write('\n');
                }

                if(m_check_failures)
                {
                    if(line.startsWith("Done (failures)"))
                    {
                        m_found_error = true;
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();  
        }
    }

    boolean getFoundError()
    {
        return m_found_error;
    }
}
