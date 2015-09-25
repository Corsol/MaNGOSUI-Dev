/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JButton;

/**
 *
 * @author Simone
 */
public class CommandManager {

    private static final String OS_NAME = "os.name";
    private static final String OS_VERSION = "os.version";
    private static final String OS_ARCH = "os.arch";

    /**
     *
     */
    public final int WINDOWS = 1;

    /**
     *
     */
    public final int UNIX = 2;

    /**
     *
     */
    public final int MACOS = 3;

    private CommandsWindows winCmd;
    private CommandsLinux unixCmd;
    private CommandsMacOS macCmd;

    private String osName = "";
    private String osVersion = "";
    private String osArch = "";
    private int CURR_OS = 0; // 0: N/A, 1: Windows, 2: Unix, 3: MAC?
    private boolean PSScriptEnabled = false;
    private String MySQLPath = "";
    //private String OpenSSLPath = "";
    //private String MySQLWinPath = "";
    private JButton btnInvoker;

    /**
     *
     */
    public CommandManager() {
        osName = System.getProperty(OS_NAME);
        osVersion = System.getProperty(OS_VERSION);
        osArch = System.getProperty(OS_ARCH);
        if (osName.toLowerCase().contains("windows")) {
            CURR_OS = 1;
            winCmd = new CommandsWindows();
            PSScriptEnabled = winCmd.checkPSScript();
        } else if (osName.toLowerCase().contains("linux")) {
            CURR_OS = 2;
            unixCmd = new CommandsLinux();
        } else if (osName.toLowerCase().contains("mac")) {
            CURR_OS = 3;
            //macCmd = new CommandsMacOS();
        } else {
        }
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkGit(Object console) {
        String command = "git --version";
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    // Set toBuffer param to true to avoid console text
                    ret_val = unixCmd.gitOperation(command, console, true);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkCMAKE(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "cmake --help";
        // Set toBuffer param to true to avoid console text
        return runOSCommand(command, console, sb, true);
    }

    /**
     *
     * @param pathToCMake
     * @param console
     * @return
     */
    public boolean checkCMAKE(String pathToCMake, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "\"" + pathToCMake + File.separator + "cmake.exe\" --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = runOSCommand(command, console, sb, true);
        if (ret) {
            this.setWinCmakePath(pathToCMake + File.separator);
        }
        return ret;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkMySQL(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "mysql --help";
        // Set toBuffer param to true to avoid console text
        return runOSCommand(command, console, sb, true);
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public boolean checkMySQL(String pathToMySQL, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "." + File.separator + pathToMySQL + File.separator + "mysql.exe --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = runOSCommand(command, console, sb, true);
        if (ret) {
            MySQLPath = pathToMySQL + File.separator;
        }
        return ret;
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public String checkMySQLLib(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public String checkMySQLInclude(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean installMySQLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    basePath = System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case 3: // MAC OS
                    basePath = "";
                    break;
                default:
                    basePath = "";
                    ret_val = false;
                    break;
            }
            File destDir = new File(basePath);
            if (!destDir.exists()) {
                boolean ret = destDir.mkdirs();
                if (!ret) {
                    System.out.println("Unable to create portable directory: " + basePath);
                    return false;
                }
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream("mysql_portable.zip"));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = basePath + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            ret_val = true;
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean installOpenSSLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    basePath = "C:" + File.separator + "OpenSSL-Win32";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case 3: // MAC OS
                    basePath = "";
                    break;
                default:
                    basePath = "";
                    ret_val = false;
                    break;
            }
            File destDir = new File(basePath);
            if (!destDir.exists()) {
                boolean ret = destDir.mkdirs();
                if (!ret) {
                    System.out.println("Unable to create portable directory: " + basePath);
                    return false;
                }
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream("openssl_portable.zip"));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = basePath + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            ret_val = true;
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    public boolean checkDBUser(String dbServer, String dbPort, String username, String password, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + username + " --password=" + password;
        String sqlCommand = " -e \"exit\"";
        // Set toBuffer param to true to avoid console text
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command + sqlCommand, null, sb, true);
                    break;
                case 2: // Unix
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command.replace(".exe", "") + sqlCommand, null, sb, true);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        if (!ret_val) {
            String msg = "\nERROR: " + sb.toString();
            if (console != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
            } else {
                System.out.println(msg);
            }
        }
        return ret_val;
    }

    public boolean checkDBStructure(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        /* This will be implemented better!!!!! */
        String sqlCommand = " -e \"CREATE TABLE `" + worldDBName + "`.dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE `" + worldDBName + "`.dbcheck;"
                + "CREATE TABLE `" + charDBName + "`.dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE `" + charDBName + "`.dbcheck;"
                + "CREATE TABLE `" + realmDBName + "`.dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE `" + realmDBName + "`.dbcheck;\"";
        // Set toBuffer param to true to avoid console text
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command + sqlCommand, null, sb, true);
                    break;
                case 2: // Unix
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command.replace(".exe", "") + sqlCommand, null, sb, true);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        if (!ret_val) {
            String msg = "\nERROR: " + sb.toString();
            if (console != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
            } else {
                System.out.println(msg);
            }
        }
        return ret_val;
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }

    }
    /*public boolean checkOpenSSL(String pathToOpenSSL, Object console){
     StringBuilder sb = new StringBuilder();
     File file = new File(pathToOpenSSL);
     // Set toBuffer param to true to avoid console text
     boolean ret = file.exists();
     if (ret) {
     MySQLPath = pathToOpenSSL + File.separator;
     }
     return ret;
     }*/

    /**
     *
     * @param config
     * @param console
     * @return
     */
    //public boolean createDB(ConfLoader config, Object console) {
    public boolean createDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        //String command = MySQLPath + "mysql.exe -q -s -h "+server+" --port="+port+" --user="+usrAdmin+" --password="+usrAdminPwd+" < World"+File.separator+"Setup"+File.separator+"mangosdCreateDB.sql";
        String command = MySQLPath + "mysql.exe -q -s -f --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " -e \""
                + "CREATE DATABASE " + worldDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + charDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + realmDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE USER '" + dbUser + "'@'%' IDENTIFIED BY '" + dbUserPwd + "';"
                + "CREATE USER '" + dbUser + "'@'localhost' IDENTIFIED BY '" + dbUserPwd + "';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + worldDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + charDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + realmDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';\"";
        //String[] command = new String[]{"cmd.exe"};
        //return runOSCommand(command, console, sb, false);
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command + sqlCommand, console, sb, false);
                    break;
                case 2: // Unix
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, false);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param config
     * @param Database
     * @param setupFolder
     * @param console
     * @return
     */
    //public boolean loadDB(ConfLoader config, String Database, String setupFolder, Object console) {
    public boolean loadDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String setupFolder, Object console) {
        StringBuilder sb = new StringBuilder();
        //String command = "";
        String command = MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " " + Database + " < " + setupFolder;
        //return runOSCommand(command, console, sb, false);
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command + sqlCommand, console, sb, false);
                    break;
                case 2: // Unix
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, false);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param config
     * @param Database
     * @param updateFolder
     * @param console
     * @return
     */
    //public boolean loadDBUpdate(ConfLoader config, String Database, String updateFolder, Object console) {
    public boolean loadDBUpdate(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String updateFolder, Object console) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = true;
        File file = new File(updateFolder);
        if (file.isDirectory() && file.list().length > 0) {
            for (File subFile : file.listFiles(new SQLFileFilter())) {
                if (!subFile.isDirectory()) {
                    command = MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
                    String sqlCommand = " " + Database + " < " + updateFolder + File.separator + subFile.getName();
                    //ret_val &= runOSCommand(command, console, sb, false);
                    try {
                        switch (CURR_OS) {
                            case 1: // Windows
                                // Set toBuffer param to true to avoid console text
                                ret_val &= runOSCommand(command + sqlCommand, console, sb, false);
                                break;
                            case 2: // Unix
                                //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                                //commands.add(sqlCommand);
                                // Set toBuffer param to true to avoid console text
                                ret_val &= runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, false);
                                break;
                            case 3: // MAC OS
                                break;
                            default:
                                ret_val = false;
                                break;
                        }
                    } catch (Exception ex) {
                        return ret_val &= false;
                    }
                }
            }
        }
        return ret_val;
    }

    /**
     *
     * @param serverFolder
     * @param buildFolder
     * @param options
     * @param console
     * @return
     */
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) {
        boolean ret_val = false;
        try {
            if (checkFolder(buildFolder)) {
                deleteFolder(buildFolder);
            }
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param buildFolder
     * @param runFolder
     * @param console
     * @return
     */
    public boolean cmakeInstall(String buildFolder, String runFolder, Object console) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.cmakeInstall(buildFolder, runFolder, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.cmakeInstall(buildFolder, runFolder, console);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param path
     * @return
     */
    public boolean checkFolder(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     *
     * @param path
     * @return
     */
    public boolean deleteFolder(String path) {
        try {
            File file = new File(path);
            if (file.isDirectory() && file.list().length > 0) {
                for (String subFile : file.list()) {
                    deleteFolder(path + File.separator + subFile);
                }
            }
            return file.delete();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     *
     * @param pathToRepo
     * @return
     */
    public boolean isRepoUpToDate(String pathToRepo) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.isRepoUpToDate(pathToRepo);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.isRepoUpToDate(pathToRepo);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    public boolean setGitProxy(String proxyServer, String proxyPort, Object console) {
        String command = "git config --global --add http.proxy http://" + proxyServer + ":" + proxyPort;
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    // Set toBuffer param to true to avoid console text
                    ret_val = unixCmd.gitOperation(command, console, true);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    public boolean remGitProxy(Object console) {
        String command = "git config --global --unset http.proxy";
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    // Set toBuffer param to true to avoid console text
                    ret_val = unixCmd.gitOperation(command, console, true);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param url
     * @param folder
     * @param branch
     * @param console
     * @return
     */
    public boolean gitDownload(String url, String folder, String branch, Object console) {
        return gitDownload(url, folder, branch, "", "", console);
    }

    /**
     *
     * @param url
     * @param folder
     * @param branch
     * @param proxyServer
     * @param proxyPort
     * @param console
     * @return
     */
    public boolean gitDownload(String url, String folder, String branch, String proxyServer, String proxyPort, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "git clone --recursive";
        if (!branch.isEmpty()) {
            command += " -b " + branch;
        }
        if (!proxyServer.isEmpty()) {
            command += " -c http.proxy=http://" + proxyServer + ":" + proxyPort;
        }
        command += " " + url;
        if (!folder.isEmpty()) {
            command += " " + folder;
        }
        //return runOSCommand(command, console, sb, false);
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.gitOperation(command, console, false);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.gitOperation(command, console, false);
                    //ret_val = runOSCommand(command, console, sb, false);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        return ret_val;

    }

    /**
     *
     * @param console
     * @return
     */
    public boolean gitCheckout(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "git pull";
        return runOSCommand(command, console, sb, false);
    }

    /**
     *
     * @param gitPath
     */
    public void setWinGitPath(String gitPath) {
        switch (CURR_OS) {
            case 1: // Windows
                winCmd.setGitPath(gitPath);
                break;
            case 2: // Unix
                break;
            case 3: // MAC OS
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param cmakePath
     */
    public void setWinCmakePath(String cmakePath) {
        switch (CURR_OS) {
            case 1: // Windows
                winCmd.setCmakePath(cmakePath);
                break;
            case 2: // Unix
                break;
            case 3: // MAC OS
                break;
            default:
                break;
        }
    }

    private boolean runOSCommand(String command, Object console, final StringBuilder sb, boolean toBuffer) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.executeCmd(command, console, sb, toBuffer);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, toBuffer);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    private boolean runOSCommand(ArrayList<String> command, Object console, final StringBuilder sb, boolean toBuffer) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.executeCmd(command, console, sb, toBuffer);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, toBuffer);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    public boolean copyFolder(String source, String dest) {
        return Command.getInstance().copyFolder(new File(source), new File(dest));
    }

    /**
     * @return the osName
     */
    public String getOsName() {
        return osName;
    }

    /**
     * @return the osVersion
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @return the osArch
     */
    public String getOsArch() {
        return osArch;
    }

    /**
     * @return the CURR_OS
     */
    public int getCURR_OS() {
        return CURR_OS;
    }

    /**
     * @return the PSScriptEnabled
     */
    public boolean isPSScriptEnabled() {
        return PSScriptEnabled;
    }

    /**
     *
     * @return
     */
    public int getDebugLevel() {
        int ret_val = 0;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.getDebugLevel();
                    break;
                case 2: // Unix
                    ret_val = unixCmd.getDebugLevel();
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = 0;
                    break;
            }
        } catch (Exception ex) {
            return 0;
        }
        return ret_val;
    }

    /**
     *
     * @param debugLevel
     */
    public void setDebugLevel(int debugLevel) {
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    winCmd.setDebugLevel(debugLevel);
                    break;
                case 2: // Unix
                    unixCmd.setDebugLevel(debugLevel);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    //ret_val = 0;
                    break;
            }
        } catch (Exception ex) {
        }
    }

    public JButton getBtnInvoker() {
        return btnInvoker;
    }

    public void setBtnInvoker(JButton btnInvoker) {
        //this.btnInvoker = btnInvoker;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    winCmd.setBtnInvoker(btnInvoker);
                    break;
                case 2: // Unix
                    winCmd.setBtnInvoker(btnInvoker);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
        }
    }
}

/* Default check structure
 boolean ret_val = false;
 try {
 switch (CURR_OS) {
 case 1: // Windows
 break;
 case 2: // Unix
 break;
 case 3: // MAC OS
 break;
 default:
 ret_val = false;
 break;
 }
 } catch (Exception ex) {
 return false;
 }
 return ret_val;
 */
