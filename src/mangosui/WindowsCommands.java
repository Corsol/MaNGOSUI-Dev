/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Simone
 */
public class WindowsCommands extends Command {

    private String gitPath = "";
    private String cmakePath = "";

    /**
     *
     */
    public WindowsCommands() {
    }

    /**
     *
     * @param command
     * @param guiConsole
     * @param rawConsole
     * @param toBuffer
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean executeCmd(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
//        command += " > NUL 2>&1";
        if (super.getDebugLevel() > 0) {
            System.out.println("\nDEBUG - command:" + command + "\n");
            if (guiConsole != null) {
            }
        }
        return execute(new String[]{"cmd.exe", "/c", command}, guiConsole, rawConsole, toBuffer);
    }

    /**
     *
     * @param commands
     * @param guiConsole
     * @param rawConsole
     * @param toBuffer
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean executeCmd(ArrayList<String> commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
        ArrayList<String> command = new ArrayList<String>();
        command.add("cmd.exe");
        command.add("/c");
        command.addAll(commands);
        if (super.getDebugLevel() > 0) {
            System.out.print("\nDEBUG - command:");
            for (String cmd : commands) {
                System.out.print("\n" + cmd);
            }
            System.out.println("");
            if (guiConsole != null) {
            }
        }
        return execute(command.toArray(new String[command.size()]), guiConsole, rawConsole, toBuffer);
    }

    /**
     *
     * @param command
     * @param guiConsole
     * @param rawConsole
     * @param toBuffer
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean executePS(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
        if (super.getDebugLevel() > 0) {
            System.out.println("\nDEBUG - command:" + command + "\n");
            if (guiConsole != null) {
            }
        }
        return execute(new String[]{"powershell.exe", command}, guiConsole, rawConsole, toBuffer);
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    //@Override
    public String checkMySQLInclude(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    //@Override
    public String checkMySQLLib(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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

    // "reg query " + '"'+ location + "\" /v \"" + key + "\"")
    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    //@Override
    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        try {
            String includePath = "include" + File.separator + "openssl";
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                StringBuilder sb;
                ArrayList<String> openSSLSearches = new ArrayList<String>();
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (64-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                for (String registry : openSSLSearches) {
                    String cmdRegVal = "reg query " + '"' + registry + "\" /v \"InstallLocation\"";
                    sb = new StringBuilder();
                    try {
                        executeCmd(cmdRegVal, console, sb, true);
                    } catch (IOException | InterruptedException ex) {
                    }
                    if (!sb.toString().isEmpty() && sb.indexOf("REG_SZ") > 0) {
                        String path = sb.substring(sb.indexOf("REG_SZ") + 6).trim() + includePath;
                        if (super.checkFolder(path)) {
                            return path;
                        }
                    }
                }
                openSSLSearches = new ArrayList<String>();
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
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    //@Override
    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        try {
            String includePath = "lib";
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                StringBuilder sb;
                ArrayList<String> openSSLSearches = new ArrayList<String>();
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (64-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                openSSLSearches.add("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\OpenSSL (32-bit)_is1");
                for (String registry : openSSLSearches) {
                    String cmdRegVal = "reg query " + '"' + registry + "\" /v \"InstallLocation\"";
                    sb = new StringBuilder();
                    try {
                        executeCmd(cmdRegVal, console, sb, true);
                    } catch (IOException | InterruptedException ex) {
                    }
                    if (!sb.toString().isEmpty() && sb.indexOf("REG_SZ") > 0) {
                        String path = sb.substring(sb.indexOf("REG_SZ") + 6).trim() + includePath;
                        if (super.checkFolder(path)) {
                            return path;
                        }
                    }
                }
                openSSLSearches = new ArrayList<String>();
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
        } catch (Exception ex) {
            return "";
        }

    }

    /**
     *
     * @return
     */
    public boolean checkPSScript() {
        try {
            StringBuilder sb = new StringBuilder();
            executePS("Get-ExecutionPolicy", null, sb, true);
            return !(!sb.toString().contains("Unrestricted") && !sb.toString().contains("Bypass"));
        } catch (InterruptedException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param serverFolder
     * @param buildFolder
     * @param options
     * @param console
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    //@Override
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        File file = new File(buildFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        //ArrayList<String> command = new ArrayList<>();

        String command = "cd " + buildFolder + " & \"" + cmakePath + "cmake.exe\" -Wno-dev";
        for (String optKey : options.keySet()) {
            if (!options.get(optKey).isEmpty()) {
                command += " -D" + optKey.substring(optKey.indexOf(".") + 1) + "=" + options.get(optKey);
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
        //command.add(cmd);
        return executeCmd(command, console, sb, false);
    }

    /**
     *
     * @param buildFolder
     * @param runFolder
     * @param console
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    //@Override
    public boolean cmakeInstall(String buildFolder, String runFolder, Object console) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        runFolder= runFolder.replace("\"", "");
        File file = new File(runFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        //ArrayList<String> command = new ArrayList<>();

        String command = "cd " + buildFolder + " & \"" + cmakePath + "cmake.exe\" --build .";

        //command.add(cmd);
        boolean ret = executeCmd(command, console, sb, false);
        String txtCopy = "Coping built file to install destination";
        if (console != null) {
            
        }
        System.out.println(txtCopy);
        if (ret) {
            super.copyFolder(new File(buildFolder + File.separator + "bin" + File.separator + "Debug"), file);
        }
        return ret;
    }

    //@Override
    boolean isRepoUpToDate(String pathToRepo) throws InterruptedException, IOException {
        StringBuilder sb = new StringBuilder();
        String command = "cd " + pathToRepo + "\n"
                + "& \"" + gitPath + "\\shell.ps1\"\n"
                + "git status";
        executePS(command, null, sb, true);
        return sb.toString().contains("Your branch is up-to-date");
    }

    //@Override
    boolean gitOperation(String gitCommand, Object console, boolean toBuffer) throws InterruptedException, IOException { //String url, String folder, String branch, String proxyServer, String proxyPort, String winPath){
        StringBuilder sb = new StringBuilder();
        if (console != null) {
            ConsoleManager.getInstance().updateGUIConsole(console, gitCommand, ConsoleManager.getInstance().TEXT_ORANGE);
        } else if (!toBuffer) {
            System.out.println(gitCommand);
        }
        String command = "& \"" + gitPath + "\\shell.ps1\" \n " + gitCommand;
        return executePS(command, console, sb, toBuffer);
    }

    /**
     *
     * @return
     */
    public String getGitPath() {
        return gitPath;
    }

    /**
     *
     * @param gitPath
     */
    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
    }

    /**
     *
     * @return
     */
    public String getCmakePath() {
        return cmakePath;
    }

    /**
     *
     * @param cmakePath
     */
    public void setCmakePath(String cmakePath) {
        this.cmakePath = cmakePath;
    }

}

/* Default structure
 try {
            
 } catch (Exception ex) {
 throw ex;
 }
 return false;
 */
