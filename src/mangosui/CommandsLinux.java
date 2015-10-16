/*
 * Copyright (C) 2015 Boni Simone <simo.boni@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package mangosui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class CommandsLinux extends Command {

    private String catalogCommand = "";
    private static final Logger LOG = Logger.getLogger(CommandsLinux.class.getName());

    /**
     * Default constructor that initialize main object
     */
    public CommandsLinux() {
        String osName = this.getOSDistroName();
        if (!osName.isEmpty()) {
            // Check linux distribution's name to set the properly catalog command
            if (osName.contains("Debian") || osName.contains("buntu")) {
                this.catalogCommand = "apt-get -y -qq";
            } else if (osName.contains("RedHat")) {
                this.catalogCommand = "yum -y -qq";
            }
        }
    }

    /**
     * Execute the commands list and return the exit status code as boolean
     * value. Output of commands can be redirected to user in many different
     * ways:<br/>
     * * JTextPane in GUI environment<br/>
     * * Console<br/>
     * * String<br/>
     * If guiConsole is null the process use current thread to run commands, and
     * user must wait commands execution to use the interface (gui or console).
     * Else if guiConsole was passed as argument the commands execution will be
     * run in a separated thread, so GUI will not freeze until execution is done
     * and output wiil be pasted into JTextPane specified. To manage subthread
     * ends use a JButton or JProgressBar and setup the PropertyChange event.
     * The separated thread will change "Text" (for Jbutton) and/or "Value" (for
     * JProgressBar) when work is done.
     *
     * @param command The String commands list to be executed
     * @param guiConsole The JTextPane swing component into wich append
     * execution output
     * @param rawConsole The String variable into wich excution output will be
     * saved
     * @param toBuffer The boolean value to save output into String variable and
     * avoid console output
     * @param prbCurrWork The JProgressBar used on separated thread execution
     * @return True if local execution exit value is 0, false if local execution
     * exit value is not equals to 0, false if execution will be executed in
     * separated thread.
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean executeShell(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        if (super.getDebugLevel() > 1) {
            System.console().printf("\nDEBUG - command:" + command + "\n\n");
            if (guiConsole != null) {
            }
        }
        return this.execute(new String[]{"bash", "-c", command}, guiConsole, rawConsole, toBuffer, prbCurrWork);
    }

    /**
     * Execute the commands list and return the exit status code as boolean
     * value. Output of commands can be redirected to user in many different
     * ways:<br/>
     * * JTextPane in GUI environment<br/>
     * * Console<br/>
     * * String<br/>
     * If guiConsole is null the process use current thread to run commands, and
     * user must wait commands execution to use the interface (gui or console).
     * Else if guiConsole was passed as argument the commands execution will be
     * run in a separated thread, so GUI will not freeze until execution is done
     * and output wiil be pasted into JTextPane specified. To manage subthread
     * ends use a JButton or JProgressBar and setup the PropertyChange event.
     * The separated thread will change "Text" (for Jbutton) and/or "Value" (for
     * JProgressBar) when work is done.
     *
     * @param commands The {@literal ArrayList<String>} commands list to be
     * executed
     * @param guiConsole The JTextPane swing component into wich append
     * execution output
     * @param rawConsole The String variable into wich excution output will be
     * saved
     * @param toBuffer The boolean value to save output into String variable and
     * avoid console output
     * @param prbCurrWork The JProgressBar used on separated thread execution
     * @return True if local execution exit value is 0, false if local execution
     * exit value is not equals to 0, false if execution will be executed in
     * separated thread.
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean executeShell(ArrayList<String> commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        ArrayList<String> command = new ArrayList<>();
        command.addAll(commands);
        if (super.getDebugLevel() > 1) {
            System.console().printf("\nDEBUG - command:");
            for (String cmd : commands) {
                System.console().printf("%s", "\n" + cmd);
            }
            System.console().printf("");
            if (guiConsole != null) {
            }
        }
        return this.execute(command.toArray(new String[command.size()]), guiConsole, rawConsole, toBuffer, prbCurrWork);
    }

    /**
     * Get the linux distribuion name
     *
     * @return The String value for current linux distribution name
     */
    private String getOSDistroName() {
        StringBuilder sb = new StringBuilder();
        String command = "cat /etc/*-release";
        try {
            if (this.executeShell(command, null, sb, true, null) && sb.toString().contains("NAME")) {
                int start = sb.toString().indexOf("NAME");
                String partSub = sb.toString().substring(start);
                start = partSub.indexOf('=');
                int end = partSub.indexOf('\n');
                return partSub.substring(start, end);
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * Check system folders to find MySQL include directory for CMake
     * installation.
     *
     * @param pathToMySQL The String path to MySQL include folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL include folder were found in specified path, false
     * otherwise
     */
    public String checkMySQLInclude(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<>();
                mysqlSearches.add(File.separator + "usr" + File.separator + "include");
                mysqlSearches.add(File.separator + "usr" + File.separator + "include" + File.separator + "mysql");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "include");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "include" + File.separator + "mysql");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "mysql" + File.separator + "include");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "include" + File.separator + "mysql*" + File.separator + "mysql");
                for (String path : mysqlSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
                return "";
            } else {
                if (super.checkFolder(pathToMySQL)) {
                    return pathToMySQL;
                } else {
                    return "";
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Check system folders to find MySQL library directory for CMake
     * installation.
     *
     * @param pathToMySQL The String path to MySQL lirary folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL library were found in specified path, false
     * otherwise
     */
    public String checkMySQLLib(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<>();
                mysqlSearches.add(File.separator + "usr" + File.separator + "lib" + File.separator + "mysql");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "lib" + File.separator + "mysql");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "mysql" + File.separator + "lib");
                mysqlSearches.add(File.separator + "opt" + File.separator + "local" + File.separator + "lib" + File.separator + "mysql*" + File.separator + "mysql");
                for (String path : mysqlSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
                return "";
            } else {
                if (super.checkFolder(pathToMySQL)) {
                    return pathToMySQL;
                } else {
                    return "";
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Check system folders to find openssl include directory for CMake
     * installation.
     *
     * @param pathToOpenSSL The String path to openssl lirary folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if openssl library were found in specified path, false
     * otherwise
     */
    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        try {
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<>();
                mysqlSearches.add(File.separator + "usr" + File.separator + "include");
                mysqlSearches.add(File.separator + "usr" + File.separator + "include" + File.separator + "openssl");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "include");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "include" + File.separator + "openssl");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "openssl" + File.separator + "include");
                for (String path : mysqlSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
                return "";
            } else {
                if (super.checkFolder(pathToOpenSSL)) {
                    return pathToOpenSSL;
                } else {
                    return "";
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Check system folders to find openssl library for CMake installation.
     *
     * @param pathToOpenSSL The String path to openssl lirary folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if openssl library were found in specified path, false
     * otherwise
     */
    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        try {
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<>();
                mysqlSearches.add(File.separator + "usr" + File.separator + "lib" + File.separator + "ssl");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "lib" + File.separator + "ssl");
                mysqlSearches.add(File.separator + "usr" + File.separator + "local" + File.separator + "ssl" + File.separator + "lib");
                for (String path : mysqlSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
                return "";
            } else {
                if (super.checkFolder(pathToOpenSSL)) {
                    return pathToOpenSSL;
                } else {
                    return "";
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Execute git command in current system shell
     *
     * @param gitCommand The String value for git command to be executed
     * @param console The JTextPane object to be used for output in swing GUI
     * @param toBuffer The boolean value to save output into String variable and
     * avoid console output
     * @return True if git command suceeded, false if Object console is not
     * null, false otherwise
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecutionException
     */
    public boolean gitOperation(String gitCommand, Object console, boolean toBuffer) throws InterruptedException, IOException, ExecutionException { //String url, String folder, String branch, String proxyServer, String proxyPort, String winPath){
        StringBuilder sb = new StringBuilder();
        if (console != null) {
            ConsoleManager.getInstance().updateGUIConsole(console, gitCommand, ConsoleManager.TEXT_ORANGE);
        } else if (!toBuffer) {
            System.console().printf("%s\n", gitCommand);
        }
        return this.executeShell(gitCommand, console, sb, toBuffer, null);
    }

    /**
     * Check if git repository in path specified is Up-To-Date or an update is
     * available
     *
     * @param pathToRepo The String path value fore git repository to check
     * @return True if repository is Up-To-Date, false otherwise
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecutionException
     */
    public boolean isRepoUpToDate(String pathToRepo) throws InterruptedException, IOException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        String command = "git status";
        this.executeShell(command, null, sb, true, null);
        return sb.toString().contains("Your branch is up-to-date");
    }

    /**
     * Create the build folder and set CMake environment ready to compile
     *
     * @param serverFolder The String path to installation folder
     * @param buildFolder The String path to cmake build folder
     * @param options The HashMap<String, String> object with cmake option and
     * value to use
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake configuration suceeded, false if Object console is
     * not null, false otherwise
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    //@Override
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) throws IOException, InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        File file = new File(buildFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        ArrayList<String> command = new ArrayList<>();
        command.add("bash");
        command.add("-c");
        String commands = "cd " + buildFolder + " ; cmake -Wno-dev";
        for (String optKey : options.keySet()) {
            if (!options.get(optKey).isEmpty()) {
                commands += " -D" + optKey.substring(optKey.indexOf('.') + 1) + "=" + options.get(optKey);
            }
        }
        if (buildFolder.indexOf(File.separator) > 0) {
            String[] subF = buildFolder.split(File.separator);
            for (int i = 0; i <= subF.length + 1; i++) {
                serverFolder = ".." + File.separator + serverFolder;
            }
        } else {
            serverFolder = ".." + File.separator + serverFolder;
        }
        commands += " " + serverFolder;
        command.add(commands);
        return this.executeShell(command, console, sb, false, null);
    }

    /**
     * Compile the configured cmake project from buildFolder.<br />
     *
     * @param buildFolder The String path to cmake build folder
     * @param runFolder The String path to binari installation folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake compile suceeded, false if Object console is not
     * null, false otherwise
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean cmakeInstall(String buildFolder, String runFolder, Object console) throws IOException, InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        File file = new File(runFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        String command = "cd " + buildFolder + " ; make install";
        if (Runtime.getRuntime().availableProcessors() > 1) {
            command += " -j " + Runtime.getRuntime().availableProcessors();
        }
        return this.executeShell(command, console, sb, false, null);
    }

    /**
     * @return The String value of catalog command for current linux distribution
     */
    public String getCatalogCommand() {
        return this.catalogCommand;
    }

}
