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
public class CommandsWindows extends Command {

    private String gitPath = "";
    private String cmakePath = "";
    private static final Logger LOG = Logger.getLogger(CommandsWindows.class.getName());

    /**
     * Default constructor
     */
    public CommandsWindows() {
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
    public boolean executeCmd(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        if (super.getDebugLevel() > 1) {
            System.console().printf("\nDEBUG - command:" + command + "\n\n");
            if (guiConsole != null) {
            }
        }
        return this.execute(new String[]{"cmd.exe", "/c", command}, guiConsole, rawConsole, toBuffer, prbCurrWork);
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
    public boolean executeCmd(ArrayList<String> commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        ArrayList<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
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
     * Execute the commands and return the exit status code as boolean
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
     * @param command The String commands list to be
     * executed
     * @param guiConsole The JTextPane swing component into wich append
     * execution output
     * @param rawConsole The String variable into wich excution output will be
     * saved
     * @param toBuffer The boolean value to save output into String variable and
     * avoid console output
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if local execution exit value is 0, false if local execution
     * exit value is not equals to 0, false if execution will be executed in
     * separated thread.
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean executePS(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        if (super.getDebugLevel() > 1) {
            System.console().printf("\nDEBUG - command:" + command + "\n\n");
            if (guiConsole != null) {
            }
        }
        return this.execute(new String[]{"powershell.exe", command}, guiConsole, rawConsole, toBuffer, prbCurrWork);
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
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.1" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.2" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.3" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.4" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.5" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.7" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.1" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.2" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.3" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.4" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.5" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6" + File.separator + "include");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.7" + File.separator + "include");
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
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.1" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.2" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.3" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.4" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.5" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles") + File.separator + "MySQL" + File.separator + "MySQL Server 5.7" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.1" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.2" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.3" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.4" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.5" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6" + File.separator + "lib");
                mysqlSearches.add(System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.7" + File.separator + "lib");
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
            String includePath = "include" + File.separator + "openssl";
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                StringBuilder sb;
                ArrayList<String> openSSLSearches = new ArrayList<>();
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (64-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                for (String registry : openSSLSearches) {
                    String cmdRegVal = "reg query " + '"' + registry + "\" /v \"InstallLocation\"";
                    sb = new StringBuilder();
                    try {
                        this.executeCmd(cmdRegVal, console, sb, true, null);
                    } catch (IOException | InterruptedException ex) {
                    }
                    if (!sb.toString().isEmpty() && sb.indexOf("REG_SZ") > 0) {
                        String path = sb.substring(sb.indexOf("REG_SZ") + 6).trim() + includePath;
                        if (super.checkFolder(path)) {
                            return path;
                        }
                    }
                }
                openSSLSearches = new ArrayList<>();
                openSSLSearches.add("C:" + File.separator + "OpenSSL-Win32" + File.separator + includePath);
                openSSLSearches.add("C:" + File.separator + "OpenSSL-Win64" + File.separator + includePath);
                for (String path : openSSLSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
            } else {
                if (super.checkFolder(pathToOpenSSL)) {
                    return pathToOpenSSL;
                } else {
                    return "";
                }
            }
            return "";
        } catch (ExecutionException ex) {
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
            String includePath = "lib";
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                StringBuilder sb;
                ArrayList<String> openSSLSearches = new ArrayList<>();
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (64-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                for (String registry : openSSLSearches) {
                    String cmdRegVal = "reg query " + '"' + registry + "\" /v \"InstallLocation\"";
                    sb = new StringBuilder();
                    try {
                        this.executeCmd(cmdRegVal, console, sb, true, null);
                    } catch (IOException | InterruptedException ex) {
                    }
                    if (!sb.toString().isEmpty() && sb.indexOf("REG_SZ") > 0) {
                        String path = sb.substring(sb.indexOf("REG_SZ") + 6).trim() + includePath;
                        if (super.checkFolder(path)) {
                            return path;
                        }
                    }
                }
                openSSLSearches = new ArrayList<>();
                openSSLSearches.add("C:" + File.separator + "OpenSSL-Win32" + File.separator + includePath);
                openSSLSearches.add("C:" + File.separator + "OpenSSL-Win64" + File.separator + includePath);
                for (String path : openSSLSearches) {
                    if (super.checkFolder(path)) {
                        return path;
                    }
                }
            } else {
                if (super.checkFolder(pathToOpenSSL)) {
                    return pathToOpenSSL;
                } else {
                    return "";
                }
            }
            return "";
        } catch (ExecutionException ex) {
            return "";
        }

    }

    /**
     * Check if Power Shell scripts execution are anabled in current Power Shell
     * sessions
     *
     * @return True if Power Shell scripts execution is enabled, false otherwise
     */
    public boolean checkPSScript() {
        try {
            StringBuilder sb = new StringBuilder();
            this.executePS("Get-ExecutionPolicy", null, sb, true, null);
            return !(!sb.toString().contains("Unrestricted") && !sb.toString().contains("Bypass"));
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return false;
        }
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
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) throws IOException, InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        File file = new File(buildFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        String command = "cd " + buildFolder + " & \"" + this.cmakePath + "cmake.exe\" -Wno-dev";
        for (String optKey : options.keySet()) {
            if (!options.get(optKey).isEmpty()) {
                command += " -D" + optKey.substring(optKey.indexOf('.') + 1) + "=" + options.get(optKey);
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
        command += " " + serverFolder;
        return this.executeCmd(command, console, sb, false, null);
    }

    /**
     * Compile the configured cmake project from buildFolder.<br />
     * NOTE: In GUI mode, the compiled binary need to be copied into
     * installation folder separately
     *
     * @param buildFolder The String path to cmake build folder
     * @param runFolder The String path to binari installation folder
     * @param buildType The String typology of build (Debug/Release)
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake compile suceeded, false if Object console is not
     * null, false otherwise
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean cmakeInstall(String buildFolder, String runFolder, String buildType, Object console) throws IOException, InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        runFolder = runFolder.replace("\"", "");
        File folder = new File(runFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String command = "cd " + buildFolder + " & \"" + this.cmakePath + "cmake.exe\" --build .";

        boolean ret = this.executeCmd(command, console, sb, false, null);
        String txtCopy = "\nCoping built file to install destination (" + folder.getPath() + ")...";
        System.console().printf("%s\n", txtCopy);
        if (ret) {
            super.copyFolder(new File(buildFolder + File.separator + "bin" + File.separator + buildType), folder, console);
        }
        return ret;
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
        String pre = "cd " + pathToRepo;
        String gitStatus;
        if (this.gitPath.isEmpty()) {
            command = pre + " & " + command;
            gitStatus = this.executeCmd(command, null, sb, true, null) ? sb.toString() : "";
        } else {
            if (this.gitPath.contains("GitHub")) {
                command = "& \"" + this.gitPath + File.separator + "shell.ps1\" \n " + command;
                gitStatus = this.executePS(command, null, sb, true, null) ? sb.toString() : "";
            } else {
                command = " \"" + this.gitPath + File.separator + "\"" + command;
                gitStatus = this.executeCmd(command, null, sb, true, null) ? sb.toString() : "";
            }
        }
        return gitStatus.contains("Your branch is up-to-date");
    }

    /**
     * Execute git command in current system shell.<br />
     * NOTE: This execution is compatible with both GitExtension and GitHub
     * softwares. To add more software compatibility add here the specific
     * execution
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
    public boolean gitOperation(String gitCommand, Object console, boolean toBuffer) throws InterruptedException, IOException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        if (console != null) {
            ConsoleManager.getInstance().updateGUIConsole(console, gitCommand, ConsoleManager.TEXT_ORANGE);
        } else if (!toBuffer) {
            System.console().printf("%s\n", gitCommand);
        }
        String command;
        if (this.gitPath.isEmpty()) {
            // Git command is present into shell environment
            command = gitCommand;
            return this.executeCmd(command, console, sb, toBuffer, null);
        } else {
            // Git command is NOT present into shell environment so full path to git.exe is needed and it also need to be executed into a specific shell (cmd or PowerShell)
            if (this.gitPath.contains("GitHub")) {
                // Comatibiltity with GitHub software that need PowerShell script execution
                command = "& \"" + this.gitPath + File.separator + "shell.ps1\" \n " + gitCommand;
                return this.executePS(command, console, sb, toBuffer, null);
            } else {
                // Compatibility with GitExtension software that need cmd script execution
                command = " \"" + this.gitPath + File.separator + "\"" + gitCommand;
                return (this.executeCmd(command, console, sb, toBuffer, null));
            }
        }
    }

    /**
     * @return The String value of current git.exe binary path
     */
    public String getGitPath() {
        return this.gitPath;
    }

    /**
     * Set the path to git.exe binary path
     *
     * @param gitPath The String value for git.exe binary path
     */
    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
    }

    /**
     * @return The String value of current cmake.exe binary path
     */
    public String getCmakePath() {
        return this.cmakePath;
    }

    /**
     * Set the path to cmake.exe binary path
     *
     * @param cmakePath The String value for cmake.exe binary path
     */
    public void setCmakePath(String cmakePath) {
        this.cmakePath = cmakePath;
    }

}