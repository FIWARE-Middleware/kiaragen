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

package org.fiware.kiara.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fiware.kiara.generator.exceptions.ExceptionErrorStrategy;
import org.fiware.kiara.generator.idl.grammar.Context;
import org.fiware.kiara.generator.util.Utils;

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
import com.eprosima.log.ColorMessage;

/**
 *
 * @author Rafael Lara {@literal <rafaellara@eprosima.com>}
 */
public class Kiaragen {

    /*
     * Attributes
     */
    private static final String m_localAppProduct = "kiara";
    private static final String m_appName = Kiaragen.class.getSimpleName().toLowerCase();
    private static final String m_version = "0.1.1";
    private static final String m_os = System.getProperty("os.name");
    private static final boolean m_isWindows = m_os.contains("Windows");
    private static final boolean m_isLinux = m_os.contains("Linux");
    private static final boolean m_isOSx = m_os.contains("Mac OS X"); 
    private static final Options options = setupOptions();
    private static List<String> m_platforms = new ArrayList<String>();

    private List<File> m_idlFiles = new ArrayList<File>();
    private String m_platform = "java";
    private boolean m_replace = false;
    private boolean m_example = false;
    private boolean m_ppDisable = false;
    private String m_ppPath = null;
    private final File m_defaultOutputDir = new File(".");
    private File m_outputDir = m_defaultOutputDir;
    private String m_srcPath = "";
    private File m_srcDir = m_defaultOutputDir;
    private File m_srcPackageDir = m_srcDir;
    private File m_tempDir = null;
    private String m_javaPackage = null;

    private List<String> m_includePaths = new ArrayList<String>();

    private boolean m_publishercode = true;
    private boolean m_subscribercode = true;

    static {
        m_platforms.add("java");
        m_platforms.add("gradle");
    }
    /*
     * Constructor
     */
    public Kiaragen() {
    }
    
    public Kiaragen(String[] idlFiles) {
        for (String idlFile : idlFiles) {
            this.addIdlFile(idlFile);
        }
    }
    
    public Kiaragen(File[] idlFiles) {
        for (File idlFile : idlFiles) {
            this.addIdlFile(idlFile);
        }
    }
    
    public List<File> getIdlFiles() {
        return m_idlFiles;
    }

    public void addIdlFile(File idlFile) {
        if (idlFile == null) throw new IllegalArgumentException("IDL fill may not be null");
        try {
            this.m_idlFiles.add(idlFile.getCanonicalFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("IDL file " + idlFile + " not found.");
        }
        this.m_idlFiles.add(idlFile);
    }

    public void addIdlFile(String idlFilePath) {
        if (idlFilePath == null || idlFilePath.isEmpty()) {
            throw new IllegalArgumentException("IDL filepath may not be null or empty");
        }
        this.addIdlFile(new File(idlFilePath));
    }

    public String getPlatform() {
        return m_platform;
    }

    public void setPlatform(String platform) {
        platform = (platform != null)? platform.trim().toLowerCase() : "";
        if (!m_platforms.contains(platform)) {
            throw new IllegalArgumentException("Unknown target platform: " + platform);
        }
        this.m_platform = platform;
    }

    public boolean isReplace() {
        return m_replace;
    }

    public void setReplace(boolean replace) {
        this.m_replace = replace;
    }

    public boolean isExample() {
        return m_example;
    }

    public void setExample(boolean example) {
        this.m_example = example;
    }

    public boolean isPpDisable() {
        return m_ppDisable;
    }

    public void setPpDisable(boolean ppDisable) {
        this.m_ppDisable = ppDisable;
    }

    public String getPpPath() {
        return m_ppPath;
    }

    public void setPpPath(String ppPath) {
        this.m_ppPath = (ppPath != null)? ppPath.trim() : null;
    }

    public File getOutputDir() {
        return m_outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.m_outputDir = outputDir;
    }

    public String getSrcPath() {
        return m_srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.m_srcPath = (srcPath != null)? srcPath.trim() : "";
    }

    public File getTempDir() {
        return m_tempDir;
    }

    public void setTempDir(File tempDir) {
        this.m_tempDir = tempDir;
    }

    public String getJavaPackage() {
        return m_javaPackage;
    }

    public void setJavaPackage(String javaPackage) {
        this.m_javaPackage = (javaPackage != null && !javaPackage.trim().isEmpty())? 
                javaPackage.trim() : null;
    }

    public List<String> getIncludePaths() {
        return m_includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.m_includePaths = includePaths;
    }


    /* 
     * Listener classes
     */

    class TemplateErrorListener implements StringTemplateErrorListener {
        public void error(String arg0, Throwable arg1) {
            System.out.println(ColorMessage.error() + arg0);
            arg1.printStackTrace();
        }

        public void warning(String arg0) {
            System.out.println(ColorMessage.warning() + arg0);
        }
    }

    /* 
     * Main methods
     */
    public boolean execute() {
        // verify arguments
        if (m_idlFiles.isEmpty()) {
            throw new IllegalArgumentException("Missing IDL file(s)");
        }        
        
        // create output dir
        System.out.print("Creating output directory... " + m_outputDir + "... ");
        if (Utils.createDir(m_outputDir)) {
            System.out.println("OK");
        } else {
            System.out.println("ERROR");
            return false;
        }
        
        // define define and create directory to put generated sources
        if (m_srcPath == null || m_srcPath.isEmpty()) {
            m_srcDir = m_outputDir;
        } else if (m_srcPath.startsWith(File.separator) || m_srcPath.startsWith("/")) {
            m_srcDir = new File(m_srcPath);
        } else {
            m_srcDir = new File(m_outputDir, m_srcPath);
        }
        
        m_srcPackageDir = (m_javaPackage == null) ? m_srcDir : 
                new File(m_srcDir, m_javaPackage.replace(".", File.separator));
        System.out.print("Creating output source directory... " + m_srcPackageDir + "... ");
        if (Utils.createDir(m_srcPackageDir)) {
            System.out.println("OK");
        } else {
            System.out.println("ERROR");
            return false;
        }
        
        // Load string templates
        String templatesDir = 
                Kiaragen.class.getPackage().getName().replace('.', '/')+"/idl/templates";
        System.out.println("Loading templates from... " + templatesDir);
        TemplateManager.setGroupLoaderDirectories(templatesDir);

        System.out.println(this.getClass().getCanonicalName());
        System.out.println(Context.class.getCanonicalName());

        boolean success = true;
        for (Iterator<File> it = m_idlFiles.iterator(); success && it.hasNext();) {
            success = process(it.next());
        }
        return success;
    }

    private boolean process(File idlFile) {
        System.out.println("Processing the file " + idlFile + "...");
        boolean success = true;
        try {
            // Protocol CDR
            success = parseIDLtoCDR(idlFile);
            if (!success) {
                System.out.println(ColorMessage.error() + 
                    "Failed to generate the files. See error messages for details.");
            }
        } catch (Exception ex) {
            System.out.println(ColorMessage.error() + "Failed to generate the files");
            System.out.println(ColorMessage.error(ex.getClass().getSimpleName())
                    + ex.getMessage());
            ex.printStackTrace();
            success = false;
        }
        return success;
    }

    private boolean parseIDLtoCDR(File idlFile) throws IOException {
        boolean success = false;
        File idlParseFile = idlFile;

        String baseFileName = Utils.baseFilename(idlFile);

        if (!m_ppDisable) {
            idlParseFile = callPreprocessor(idlFile);
        }

        if (idlParseFile != null) {
            Context ctx = new Context(baseFileName, idlFile.getPath(),
                    m_includePaths, m_subscribercode, m_publishercode,
                    m_localAppProduct, m_javaPackage);

            // Create template manager
            TemplateManager tmanager = new TemplateManager("eprosima:Common");

            tmanager.changeCppTypesTemplateGroup("JavaTypes");

            // Load Support class template
            tmanager.addGroup("KIARASupportType");

            // Load Service template
            tmanager.addGroup("KIARAService");

            // Load Service Async template
            tmanager.addGroup("KIARAServiceAsync");

            // Load Client template
            tmanager.addGroup("KIARAClient");

            // Load Client Proxy template
            tmanager.addGroup("KIARAProxy");

            // Load Servant template
            tmanager.addGroup("KIARAServant");

            // Load ServantExample template
            tmanager.addGroup("KIARAServantExample");

            // Load ServerExample template
            tmanager.addGroup("KIARAServerExample");

            // Load Server Example Gradle template
            tmanager.addGroup("KIARAServerExampleGradle");

            // Load Client Example template
            tmanager.addGroup("KIARAClientExample");

            // Load Client Example Gradle template
            tmanager.addGroup("KIARAClientExampleGradle");

            // Create main template
            TemplateGroup maintemplates = tmanager.createTemplateGroup("main");
            maintemplates.setAttribute("ctx", ctx);

            try {
                ANTLRFileStream input = new ANTLRFileStream(idlParseFile.getCanonicalPath());
                KIARAIDLLexer lexer = new KIARAIDLLexer(input);
                lexer.setContext(ctx);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                KIARAIDLParser parser = new KIARAIDLParser(tokens);
                // Select handling strategy for errors
                parser.setErrorHandler(new ExceptionErrorStrategy());
                // Pass the filename without the extension
                Specification specification = parser.specification(ctx,
                        tmanager, maintemplates).spec;
                success = specification != null;

            } catch (FileNotFoundException ex) {
                throw new FileNotFoundException("The File " + idlParseFile + " was not found.");

            } catch (ParseCancellationException ex) {
                throw new ParseCancellationException("The File " + idlParseFile + " cannot be parsed.", ex);
            }

            if (success) {
                System.out.println("Generating Type support classes... ");
                for (Definition d : ctx.getDefinitions()) {
                    // Check if it is a structure
                    if (d.isIsTypeDeclaration()) {
                        TypeDeclaration type = (TypeDeclaration) d;
                        TypeCode tc = type.getTypeCode();
                        if (tc.getKind() == 0x0000000a) { // Struct
                            StructTypeCode st = (StructTypeCode) tc;
                            ctx.setCurrentSt(st);
                            System.out.println("Generating Type support class for "
                                    + "structure " + st.getName() + "... ");
                            success &= Utils.writeFile(
                                    new File(m_srcPackageDir, st.getName()+".java"), 
                                    maintemplates.getTemplate("KIARASupportType"),
                                    m_replace); 
                        }
                    }
                }

                if (m_platform.equals("java") || m_platform.equals("gradle")) {
                    for (Interface ifz : ctx.getInterfaces()) {
                        ctx.setCurrentIfz(ifz);

                        System.out.println("Generating interface files "
                                + "for service " + ifz.getName() + "... ");
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+".java"),
                                maintemplates.getTemplate("KIARAService"),
                                m_replace);
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+"Async.java"),
                                maintemplates.getTemplate("KIARAServiceAsync"),
                                m_replace);
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+"Client.java"), 
                                maintemplates.getTemplate("KIARAClient"),
                                m_replace); 

                        System.out.println("Generating specific server side files "
                                + "for service " + ifz.getName() + "... ");
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+"Servant.java"),
                                maintemplates.getTemplate("KIARAServant"),
                                m_replace);
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+"ServantExample.java"),
                                maintemplates.getTemplate("KIARAServantExample"),
                                m_replace); 

                        System.out.println("Generating specific client side files "
                                + "for service " + ifz.getName() + "... ");
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, ifz.getName()+"Proxy.java"),
                                maintemplates.getTemplate("KIARAProxy"),
                                m_replace);
                    }

                    Interface ifz_handler = null;
                    if (ctx.getScopedInterfaces().size() > 0) {
                        ifz_handler = ctx.getScopedInterfaces().get(0);
                    }
                    ctx.setCurrentIfz(ifz_handler);

                    if (m_example && !ctx.getInterfaces().isEmpty()) {
                        System.out.println("Generating example server side files... ");
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, "ServerExample.java"), 
                                maintemplates.getTemplate("KIARAServerExample"),
                                m_replace);

                        System.out.println("Generating example client side files... ");
                        success = success & Utils.writeFile(
                                new File(m_srcPackageDir, "ClientExample.java"), 
                                maintemplates.getTemplate("KIARAClientExample"),
                                m_replace);
                        if (m_platform.equals("gradle")) {
                            System.out.println("Generating example gradle build files... ");
                            success = success & Utils.writeFile(
                                    new File(m_outputDir, "build_server.gradle"), 
                                    maintemplates.getTemplate("KIARAServerExampleGradle"),
                                    m_replace);
                            success = success & Utils.writeFile(
                                    new File(m_outputDir, "build_client.gradle"), 
                                    maintemplates.getTemplate("KIARAClientExampleGradle"),
                                    m_replace);
                        }
                    }
                }
            }
        }
        return success;
    }

    File callPreprocessor(File idlFile) {
        final String METHOD_NAME = "callPreprocessor";

        // Set line command.
        List<String> lineCommand = new ArrayList<String>();
        String[] lineCommandArray = null;
        File outputfile = null;
        int exitVal = -1;
        OutputStream of = null;

        // Open output stream
        try {
            // create temporary output file
            outputfile = File.createTempFile(Utils.baseFilename(idlFile), ".cc", m_tempDir);
            outputfile.deleteOnExit(); // delete temporary file after program exits
            of = new FileOutputStream(outputfile);
        } catch (FileNotFoundException ex) {
            System.out.println(ColorMessage.error(METHOD_NAME)
                   + "Cannot open output file " + outputfile);
            return null;
        } catch (IOException ex) {
            System.out.println(ColorMessage.error(METHOD_NAME)
                    + "Failed to create temporary output file " + outputfile);
            return null;
        }

        // Set the preprocessor path
        String ppPath = m_ppPath;
        if (ppPath == null) {
            if (m_isWindows) {
                ppPath = "cl.exe";
            } else if (m_isLinux) {
                ppPath = "cpp";
            } else if (m_isOSx) {
                ppPath = "clang";
            }
        }

        // Add command
        lineCommand.add(ppPath);

        // Only start preprocessor phase
        if (ppPath.endsWith("cl.exe")) {
            lineCommand.add("/E");
            lineCommand.add("/C");
        } else if (ppPath.endsWith("clang") || ppPath.endsWith("gcc")) {
            lineCommand.add("-E");
        }

        // Add the include paths given as parameters.
        for (String includePath : m_includePaths) {
            if (ppPath.endsWith("cl.exe")) {
                lineCommand.add(includePath.replaceFirst("^-I", "/I"));
            } else if (ppPath.endsWith("cpp") || ppPath.endsWith("clang") || ppPath.endsWith("gcc") ) {
                lineCommand.add(includePath);
            }
        }

        // Add input file.
        lineCommand.add(idlFile.getAbsolutePath());

        // Send result to standard out (
        if (ppPath.endsWith("cpp")) {
            lineCommand.add("-"); // send to stdout
        }

        lineCommandArray = new String[lineCommand.size()];
        lineCommandArray = (String[]) lineCommand.toArray(lineCommandArray);

        // Start preprocessor and send stdout to output file stream
        try {
            Process preprocessor = Runtime.getRuntime().exec(lineCommandArray);
            ProcessOutput errorOutput = new ProcessOutput(
                    preprocessor.getErrorStream(), "ERROR", false, null);
            ProcessOutput normalOutput = new ProcessOutput(
                    preprocessor.getInputStream(), "OUTPUT", false, of);
            errorOutput.start();
            normalOutput.start();
            exitVal = preprocessor.waitFor();
            errorOutput.join();
            normalOutput.join();
        } catch (Exception e) {
            System.out.println(ColorMessage.error(METHOD_NAME)
                    + "Cannot execute the preprocessor. Reason: "
                    + e.getMessage());
            return null;
        }

        // Close output stream
        if (of != null) {
            try {
                of.close();
            } catch (IOException e) {
                System.out.println(ColorMessage.error(METHOD_NAME)
                        + "Cannot close output file " + outputfile);
            }
        }

        if (exitVal != 0) {
            System.out.println(ColorMessage.error(METHOD_NAME)
                    + "Preprocessor return an error " + exitVal);
            return null;
        }
        return outputfile;
    }

    /*
     * Main entry point
     */
    public static void main(String[] args) {
        ColorMessage.load();
        try {
            // create the command line parser
            CommandLineParser parser = new GnuParser();
            // parse the command line arguments
            CommandLine line = null;
            try {
                line = parser.parse( options, args );
            } catch(org.apache.commons.cli.ParseException exp ) {
                throw new IllegalArgumentException(exp.getMessage());
            }
            if (line.hasOption("help")) {
                printHelp();
                System.exit(0);
            }
            if (line.hasOption("version")) {
                printVersion();
                System.exit(0);
            }
            // create instance of kiaragen and pass all IDL files
            Kiaragen kiaragen = new Kiaragen(line.getArgs());
            // parse and set options
            kiaragen.setOutputDir(new File(line.getOptionValue("outputdir", ".")));
            kiaragen.setSrcPath(line.getOptionValue("srcpath"));
            kiaragen.setJavaPackage(line.getOptionValue("package", ""));
            kiaragen.setPlatform(line.getOptionValue("target", "java"));
            kiaragen.setExample(line.hasOption("example"));
            kiaragen.setReplace(line.hasOption("replace"));
            kiaragen.setPpDisable(line.hasOption("ppDisable"));
            kiaragen.setPpPath(line.getOptionValue("ppPath"));
            if (line.hasOption("tempdir")) {
                kiaragen.setTempDir(new File(line.getOptionValue("tempdir")));
            }
            // execute the generation process
            if (!kiaragen.execute()) {
                System.exit(-1);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(ColorMessage.error("Illegal Argument :")
                    + e.getMessage());
            printHelp();
            System.exit(-1);
        }
        System.exit(0);
    }
    
    @SuppressWarnings("static-access")
    private static Options setupOptions() {
        // define the Options
        Options options = new Options();
        options.addOption("e", "example", false, "Generate example classes to run the client and server.");
        options.addOption("h", "help",    false, "Show help information" );
        options.addOption("v", "version", false, "Show the current version of " + m_appName );
        options.addOption("r", "replace", false, "Replace existing generated files");
        options.addOption(null, "ppDisable",false,"Disables the preprocessor.");
        options.addOption(OptionBuilder
                .withLongOpt("package")
                .hasArg()
                .withArgName("package")
                .withDescription("Defines the package prefix of the generated "
                                + "java classes. [Default: no package]")
                .create("p"));
        options.addOption(OptionBuilder
                .withLongOpt("outputdir")
                .hasArg()
                .withArgName("path")
                .withDescription("Specifies the output directory for the "
                                + "generated files. [Default: current working dir]")
                .create("o"));
        options.addOption(OptionBuilder
                .withLongOpt("srcpath")
                .hasArg()
                .withArgName("path")
                .withDescription("Specifies a location for the generated source-files. "
                        + "It may be relative to the output-dir (e.g. \"src\"). [Default: outputdir]")
                .create('s'));
        options.addOption(OptionBuilder
                .withLongOpt("target")
                .hasArg()
                .withArgName("platform")
                .withDescription("Generates the support files "
                                + "(interfaces, stubs, skeletons,...) "
                                + "for the given target platform.").create('t'));
        options.addOption(OptionBuilder
                .withLongOpt("tempdir")
                .hasArg()
                .withArgName("path")
                .withDescription("Specifies a specific directory for "
                                + "temporary files. [Default: System temp directory]")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("ppPath")
                .hasArg()
                .withArgName("path")
                .withDescription("Specifies the path of the preprocessor. "
                                + "[Default: System C++ preprocessor]")
                .create());        
        return options;
    }
    
    public static void printHelp() {
        // generate the help statement
        String header = "Options:";
        StringBuffer footerSB = new StringBuffer("\r\nValid target platforms: \r\n");
        for (String platform : m_platforms) {
            footerSB.append(" - ").append(platform).append("\r\n");
        }
        footerSB.append("Arguments:\r\n - IDL-file(s)  KIARA Interface Definiton Files\r\n");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(m_appName + " [options]... IDL-file [IDL-file...]", 
                header, options, footerSB.toString());
        
    }
    
    public static void printVersion() {
        System.out.println(m_appName + " - Version " + m_version);
    }
}

class ProcessOutput extends Thread {
    InputStream is = null;
    OutputStream of = null;
    String type;
    boolean m_check_failures;
    boolean m_found_error = false;
    final String clLine = "#line";

    ProcessOutput(InputStream is, String type, boolean check_failures,
            OutputStream of) {
        this.is = is;
        this.type = type;
        m_check_failures = check_failures;
        this.of = of;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (of == null)
                    System.out.println(line);
                else {
                    // Sustituir los \\ que pone cl.exe por \
                    if (line.startsWith(clLine)) {
                        line = "#" + line.substring(clLine.length());
                        int count = 0;
                        while ((count = line.indexOf("\\\\")) != -1) {
                            line = line.substring(0, count) + "\\"
                                    + line.substring(count + 2);
                        }
                    }

                    of.write(line.getBytes());
                    of.write('\n');
                }

                if (m_check_failures) {
                    if (line.startsWith("Done (failures)")) {
                        m_found_error = true;
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    boolean getFoundError() {
        return m_found_error;
    }
}
