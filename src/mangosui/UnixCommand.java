/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Simone
 */
public class UnixCommand extends Command {
//public class UnixCommand {

    public UnixCommand() {
    }

    public boolean executeShell(String command, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
        if (super.getDebugLevel() > 0) {
            System.out.println("\nDEBUG - command:" + command + "\n");
            if (guiConsole != null) {
            }
        }
        return execute(new String[]{command}, guiConsole, rawConsole, toBuffer);
    }

    public boolean executeShell(ArrayList<String> commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
        ArrayList<String> command = new ArrayList<String>();
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

    @Override
    public String checkMySQLInclude(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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

    @Override
    public String checkMySQLLib(String pathToMySQL, Object console) {
        try {
            if (pathToMySQL == null || pathToMySQL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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

    @Override
    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        try {
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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

    @Override
    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        try {
            if (pathToOpenSSL == null || pathToOpenSSL.isEmpty()) {
                ArrayList<String> mysqlSearches = new ArrayList<String>();
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

    @Override
    boolean isRepoUpToDate(String pathToRepo) throws InterruptedException, IOException {
        StringBuilder sb = new StringBuilder();
        String command = "git status";
        executeShell(command, null, sb, true);
        return sb.toString().contains("Your branch is up-to-date");
    }

    @Override
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        File file = new File(buildFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        //ArrayList<String> command = new ArrayList<>();

        String command = "cd " + buildFolder + " & cmake.exe -Wno-dev";
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
        return executeShell(command, console, sb, false);
    }

    @Override
    public boolean cmakeInstall(String buildFolder, String runFolder, Object console) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        File file = new File(runFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        //ArrayList<String> command = new ArrayList<>();

        String command = "cd " + buildFolder + " & make install";

        //command.add(cmd);
        return executeShell(command, console, sb, false);
    }

}
