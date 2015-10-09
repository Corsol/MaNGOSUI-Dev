/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Simone
 */
public class MaNGOSUI extends WorkExecutor {

    private static boolean gitOk = false;
    private static boolean mysqlOk = false;
    private static boolean cmakeOk = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("-c")) {
            System.console().printf("MaNGOS Universal Installer: console mode.\nCredit to: Antz (for MySQL and OpenSSL checks) and Faded (for unix dependecy setup)\n\n");

            System.console().printf("Loading configuration file... ");
            ConfLoader confLoader = new ConfLoader();
            if (confLoader.isConfLoaded()) {
                System.console().printf(" Done!\n\n");
            } else {
                System.console().printf(" FAILED! Check conf.property file.\n\n");
                return;
            }

            CommandManager cmdManager = new CommandManager();
            cmdManager.setDebugLevel(confLoader.getDebugLevel());
            if (cmdManager.getCURR_OS() > 0) {
                String input;
                String serverFolder;
                serverFolder = setGitFolder(confLoader.getGitFolderServer(), confLoader.getGitURLServer());
                String databaseFolder;
                databaseFolder = setGitFolder(confLoader.getGitFolderDatabase(), confLoader.getGitURLDatabase());
                String elunaFolder;
                elunaFolder = setGitFolder(confLoader.getGitFolderEluna(), confLoader.getGitURLEluna());

                System.console().printf("INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch() + "\n\n");

                /**
                 * ** Dependencies checks ***
                 */
                gitOk = checkGit(confLoader.getWinGitHubPath(), confLoader.getWinGitExtPath(), cmdManager, null, null);
                if (!gitOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.console().printf("WARNING: Git not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.console().printf(" Visit \"" + confLoader.getURLGit() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.console().printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupGit(null, null);
                            }
                        }
                    } else {
                        System.console().printf("");
                        System.console().printf("To install Git run MaNGOS UI in root console.\n");
                    }
                }
                mysqlOk = checkMySQL(databaseFolder, confLoader.getPathToMySQL(), cmdManager, null, null);
                if (!mysqlOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.console().printf("WARNING: MySQL not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.console().printf(" Visit \"" + confLoader.getURLMySQL() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.console().printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupMySQL(null, null);
                            }
                        }
                    } else {
                        System.console().printf("\n");
                        System.console().printf("To install MySQL run MaNGOS UI in root console.\n");
                    }
                }
                cmakeOk = checkCMake(confLoader.getWin32PathCMake(), confLoader.getWin64PathCMake(), cmdManager, null, null);
                if (!cmakeOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.console().printf("WARNING: CMake not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.console().printf(" Visit \"" + confLoader.getURLCMake() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.console().printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupCMake(null, null);
                                cmdManager.setupOpenSSL(null, null);
                            }
                        }
                    } else {
                        System.console().printf("To install CMake run MaNGOS UI in root console.\n");
                    }
                }
                if (cmakeOk) {
                    if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && cmdManager.checkMySQLLib("", null).isEmpty() || cmdManager.checkMySQLInclude("", null).isEmpty()) {
                        System.console().printf("ERROR: MySQL library for CMAKE was not found on system. Do you want to use portable version?\n");
                        System.console().printf("WARNING: Portable MySQL library is for 5.6 version. Use this for other MySQL version can be insecure.\n");
                        System.console().printf("Do you want to install MySQL portable library? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            cmakeOk = cmdManager.installMySQLPortable(null);
                            if (!cmakeOk) {
                                System.console().printf("ERROR: MySQL library for CMAKE not installed. Try again running as Administrator!\n");
                            }
                        } else {
                            cmakeOk = false;
                        }
                    }

                }

                boolean optGitSrvInstall = false;
                boolean optGitSrvWipe = false;
                boolean optGitSrvUpdate = false;
                boolean optGitDBInstall = false;
                boolean optGitDBWipe = false;
                boolean optGitDBUpdate = false;
                boolean optGitElunaInstall = false;
                boolean optGitElunaWipe = false;
                boolean optGitElunaUpdate = false;

                // Version selection
                System.console().printf("\nSelect wich version of MaNGOS install:\n");
                System.console().printf("0 - MaNGOS Zero\n");
                System.console().printf("1 - MaNGOS One\n");
                System.console().printf("2 - MaNGOS Two\n");
                System.console().printf("3 - MaNGOS Three\n");
                System.console().printf("4 - MaNGOS Four\n");
                System.console().printf("Version ? [0-4, default:0] ");
                input = System.console().readLine();
                if (input.isEmpty()) {
                    input = "0";
                }
                confLoader.getGitURLServer(input);
                confLoader.getGitURLDatabase(input);
                confLoader.getGitURLEluna(input);
                confLoader.getGitBranchServer(input);
                confLoader.getGitBranchDatabase(input);
                confLoader.getGitBranchEluna(input);

                /**
                 * **** Git download *****
                 */
                System.console().printf("\nDo you want to download source from Git repositories? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && gitOk) {
                    System.console().printf("\nProxy configuration:\n");
                    System.console().printf("Server name: " + confLoader.getProxyServer() + "\n");
                    System.console().printf("Server port: " + confLoader.getProxyPort() + "\n");
                    System.console().printf("\nDo you want to use a proxy server with this parameter? [y/n, default:n] ");
                    input = System.console().readLine();
                    if (!"y".equalsIgnoreCase(input)) {
                        confLoader.setProxyPort("");
                        confLoader.setProxyServer("");
                        cmdManager.remGitProxy(null);
                    } else {
                        if (cmdManager.setGitProxy(confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                            confLoader.setProxyPort("");
                            confLoader.setProxyServer("");
                            System.console().printf("Global proxy settings applyed\n");
                        } else {
                            System.console().printf("WARNING: Failed to apply global settings! Proxy settings will be used on local git operation.\n");
                        }
                    }
                    // Show server git param
                    System.console().printf("\n\n*** Server download parameters:\n");
                    System.console().printf("URL                          : " + confLoader.getGitURLServer() + "\n");
                    System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer() + "\n");
                    System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer() + "\n");
                    System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.console().printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setGitURLServer(readNewParam("URL", confLoader.getGitURLServer()));
                        confLoader.setGitFolderServer(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderServer()));
                        confLoader.setGitBranchServer(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchServer()));
                        System.console().printf("\n\n*** NEW Server download parameters:\n");
                        System.console().printf("URL                          : " + confLoader.getGitURLServer() + "\n");
                        System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer() + "\n");
                        System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer() + "\n");
                    }
                    // Check server folder
                    if (cmdManager.checkFolder(serverFolder)) {
                        optGitSrvWipe = true;
                        // Check server version for update
                        if (!cmdManager.isRepoUpToDate(serverFolder)) {
                            optGitSrvUpdate = true;
                        }
                    } else {
                        optGitSrvInstall = true;
                    }
                    // Ask what to do (first clone, wipe and clone, checkout and update) for server
                    System.console().printf("\n*** Server download option avaiable:\n");
                    if (optGitSrvInstall) {
                        System.console().printf("N - New download.\n");
                    }
                    if (optGitSrvWipe) {
                        System.console().printf("W - Wipe current and download again.\n");
                    }
                    if (optGitSrvUpdate) {
                        System.console().printf("U - Update local server.\n");
                    }
                    System.console().printf("Empty for no action.\n");
                    System.console().printf("\nInsert action: ");
                    input = System.console().readLine();
                    gitDownload(input, confLoader.getGitURLServer(), serverFolder, confLoader.getGitBranchServer(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null, null);

                    // Show database git param
                    System.console().printf("\n\n*** Database download parameters:\n");
                    System.console().printf("URL                          : " + confLoader.getGitURLDatabase() + "\n");
                    System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase() + "\n");
                    System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase() + "\n");
                    System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.console().printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setGitURLDatabase(readNewParam("URL", confLoader.getGitURLDatabase()));
                        confLoader.setGitFolderDatabase(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderDatabase()));
                        confLoader.setGitBranchDatabase(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchDatabase()));
                        System.console().printf("\n\n*** NEW Database download parameters:\n");
                        System.console().printf("URL                          : " + confLoader.getGitURLDatabase() + "\n");
                        System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase() + "\n");
                        System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase() + "\n");
                    }
                    // Check database folder
                    databaseFolder = setGitFolder(confLoader.getGitFolderDatabase(), confLoader.getGitURLDatabase() + "\n");
                    if (cmdManager.checkFolder(databaseFolder)) {
                        optGitDBWipe = true;
                        // Check database version for update
                        if (!cmdManager.isRepoUpToDate(databaseFolder)) {
                            optGitDBUpdate = true;
                        }
                    } else {
                        optGitDBInstall = true;
                    }
                    // Ask what to do (first clone, wipe and clone, checkout and update) for database
                    System.console().printf("\n*** Database download option avaiable:\n");
                    if (optGitDBInstall) {
                        System.console().printf("N - New download.\n");
                    }
                    if (optGitDBWipe) {
                        System.console().printf("W - Wipe current and download again.\n");
                    }
                    if (optGitDBUpdate) {
                        System.console().printf("U - Update local database.\n");
                    }
                    System.console().printf("Empty for no action.\n");
                    System.console().printf("\nInsert action: ");
                    input = System.console().readLine();
                    gitDownload(input, confLoader.getGitURLDatabase(), databaseFolder, confLoader.getGitBranchDatabase(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null, null);

                    // Show LUA Script git param
                    if (!confLoader.getGitURLEluna().isEmpty()) {
                        System.console().printf("\n\n*** LUA Script download parameters:\n");
                        System.console().printf("URL                          : " + confLoader.getGitURLEluna() + "\n");
                        System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderEluna() + "\n");
                        System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchEluna() + "\n");
                        System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.console().printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setGitURLEluna(readNewParam("URL", confLoader.getGitURLEluna()));
                            confLoader.setGitFolderEluna(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderEluna()));
                            confLoader.setGitBranchEluna(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchEluna()));
                            System.console().printf("\n\n*** NEW LUA Script download parameters:\n");
                            System.console().printf("URL                          : " + confLoader.getGitURLEluna() + "\n");
                            System.console().printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderEluna() + "\n");
                            System.console().printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchEluna() + "\n");
                        }
                        // Check server folder

                        if (cmdManager.checkFolder(elunaFolder)) {
                            optGitElunaWipe = true;
                            // Check server version for update
                            if (!cmdManager.isRepoUpToDate(elunaFolder)) {
                                optGitElunaUpdate = true;
                            }
                        } else {
                            optGitElunaInstall = true;
                        }
                        // Ask what to do (first clone, wipe and clone, checkout and update) for server
                        System.console().printf("\n*** LUA Script download option avaiable:\n");
                        if (optGitElunaInstall) {
                            System.console().printf("N - New download.\n");
                        }
                        if (optGitElunaWipe) {
                            System.console().printf("W - Wipe current and download again.\n");
                        }
                        if (optGitElunaUpdate) {
                            System.console().printf("U - Update local LUA Script.\n");
                        }
                        System.console().printf("Empty for no action.\n");
                        System.console().printf("\nInsert action: ");
                        input = System.console().readLine();
                        gitDownload(input, confLoader.getGitURLEluna(), elunaFolder, confLoader.getGitBranchEluna(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null, null);
                    }
                } else {
                    System.console().printf("\n*** Download operation skipped. Git command not ready.\n");
                }

                /**
                 * **** Database install *****
                 */
                System.console().printf("\nDo you want to install databases? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && mysqlOk) {
                    System.console().printf("\n\n*** Database server installation parameters:\n");
                    System.console().printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                    System.console().printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                    System.console().printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                    System.console().printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                    System.console().printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                    System.console().printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                    System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.console().printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setDatabaseServer(readNewParam("SERVER", confLoader.getDatabaseServer()));
                        confLoader.setDatabasePort(readNewParam("PORT", confLoader.getDatabasePort()));
                        confLoader.setDatabaseAdmin(readNewParam("ADMIN USER", confLoader.getDatabaseAdmin()));
                        confLoader.setDatabaseAdminPass(readNewParam("ADMIN PASS", confLoader.getDatabaseAdminPass()));
                        confLoader.setDatabaseUser(readNewParam("DB USER", confLoader.getDatabaseUser()));
                        confLoader.setDatabaseUserPass(readNewParam("DB PASS", confLoader.getDatabaseUserPass()));
                        System.console().printf("\n\n*** NEW Database server installation parameters:\n");
                        System.console().printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                        System.console().printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                        System.console().printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                        System.console().printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                        System.console().printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                        System.console().printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                    }
                    System.console().printf("\n\n*** Database install configuration:\n");
                    System.console().printf("WORLD DB       : " + confLoader.getWorldDBName() + "\n");
                    for (String upd : confLoader.getWorldUpdRel().values()) {
                        System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                    }
                    System.console().printf("CHAR DB        : " + confLoader.getCharDBName() + "\n");
                    for (String upd : confLoader.getCharUpdRel().values()) {
                        System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                    }
                    System.console().printf("REALM DB       : " + confLoader.getRealmDBName() + "\n");
                    for (String upd : confLoader.getRealmUpdRel().values()) {
                        System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                    }
                    System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        HashMap<String, String> newUpdRel = new HashMap<String, String>();
                        System.console().printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setWorldDBName(readNewParam("WORLD DB", confLoader.getWorldDBName()));
                        for (String updKey : confLoader.getWorldUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getWorldUpdRel().get(updKey)));
                        }
                        confLoader.setWorldUpdRel(newUpdRel);
                        confLoader.setCharDBName(readNewParam("CHAR DB", confLoader.getCharDBName()));
                        newUpdRel = new HashMap<String, String>();
                        for (String updKey : confLoader.getCharUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getCharUpdRel().get(updKey)));
                        }
                        confLoader.setCharUpdRel(newUpdRel);
                        confLoader.setRealmDBName(readNewParam("REALM DB", confLoader.getRealmDBName()));
                        newUpdRel = new HashMap<String, String>();
                        for (String updKey : confLoader.getRealmUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getRealmUpdRel().get(updKey)));
                        }
                        confLoader.setRealmUpdRel(newUpdRel);
                        System.console().printf("\n\n*** NEW Database install configuration:\n");
                        System.console().printf("WORLD DB    : " + confLoader.getWorldDBName() + "\n");
                        System.console().printf("CHAR DB     : " + confLoader.getCharDBName() + "\n");
                        System.console().printf("REALM DB    : " + confLoader.getRealmDBName() + "\n");
                        System.console().printf("WORLD DB       : " + confLoader.getWorldDBName() + "\n");
                        for (String upd : confLoader.getWorldUpdRel().values()) {
                            System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                        }
                        System.console().printf("CHAR DB        : " + confLoader.getCharDBName() + "\n");
                        for (String upd : confLoader.getCharUpdRel().values()) {
                            System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                        }
                        System.console().printf("REALM DB       : " + confLoader.getRealmDBName() + "\n");
                        for (String upd : confLoader.getRealmUpdRel().values()) {
                            System.console().printf(" - UPD RELEASE : " + upd+ "\n");
                        }
                    }

                    if (!optGitDBInstall && !optGitDBUpdate) {
                        // Reset after previous check done.
                        optGitDBWipe = false;
                        System.console().printf("\n\n*** Database installation option avaiable:\n");
                        System.console().printf("N - New install.\n");
                        System.console().printf("W - Wipe current and install again.\n");
                        System.console().printf("U - Update database.\n");
                        System.console().printf("Empty for no action.\n");
                        System.console().printf("\nInsert action: ");
                        input = System.console().readLine();
                        if (input.equalsIgnoreCase("N")) {
                            optGitDBInstall = true;
                        } else if (input.equalsIgnoreCase("W")) {
                            optGitDBWipe = true;
                        } else if (input.equalsIgnoreCase("U")) {
                            optGitDBUpdate = true;
                        }
                    }
                    if (optGitDBInstall || optGitDBWipe) {
                        if (!optGitDBWipe) {
                            System.console().printf("\nIs your first DB installation: [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                mysqlCreateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                        confLoader.getDatabaseUser(), confLoader.getDatabaseUserPass(), confLoader.getWorldDBName(), confLoader.getCharDBName(),
                                        confLoader.getRealmDBName(), cmdManager, null, null);
                            }
                        }
                        System.console().printf("\nDo you want to wipe (if already installed) and install Realm database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), confLoader.getRealmLoadDBName(), confLoader.getRealmDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getRealmUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), updFolders, null, confLoader.getRealmDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }

                        System.console().printf("\nDo you want to wipe (if already installed) and install Character database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Installing Character database
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), confLoader.getCharLoadDBName(), confLoader.getCharDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getCharUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), updFolders, null, confLoader.getCharDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }

                        System.console().printf("\nDo you want to wipe (if already installed) and install World database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Installing World database
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), confLoader.getWorldLoadDBName(), confLoader.getWorldDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null, null);

                            ArrayList<String> loadFolder = new ArrayList<>();
                            loadFolder.add(confLoader.getWorldFullDB());
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), loadFolder, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getWorldUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), updFolders, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }
                    } else if (optGitDBUpdate) {
                        System.console().printf("\nDo you want to update Realm database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getRealmUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), updFolders, null, confLoader.getRealmDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }

                        System.console().printf("\nDo you want update Character database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getCharUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), updFolders, null, confLoader.getCharDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }

                        System.console().printf("\nDo you want to update World database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getWorldUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), updFolders, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null, null);
                        }
                    }
                } else {
                    System.console().printf("\n*** Database operation skipped. MySQL command not ready.\n");
                }

                /**
                 * **** Server compile and install *****
                 */
                System.console().printf("\nDo you want to build and install serve sources? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && cmakeOk) {
                    System.console().printf("\n\n*** Buil and install folders configuration:\n");
                    System.console().printf("BUILD FOLDER  : " + confLoader.getCMakeBuildFolder().replace("\"", "") + "\n");
                    System.console().printf("INSTALL FOLDER: " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                    System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.console().printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setCMakeBuildFolder(readNewParam("BUILD FOLDER", confLoader.getCMakeBuildFolder().replace("\"", "")));
                        confLoader.setCMakeRunFolder(readNewParam("INSTALL FOLDER", confLoader.getCMakeRunFolder().replace("\"", "")));
                        System.console().printf("\n\n*** NEW buil and install folders configuration:\n");
                        System.console().printf("LUA SCRIPT FOLDER: " + confLoader.getCMakeBuildFolder().replace("\"", "") + "\n");
                        System.console().printf("SERVER RUN FOLDER    : " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                    }
                    System.console().printf("\n*** Other build options (empty values means default value):\n");
                    ArrayList<String> cmakeOptions = new ArrayList<String>();
                    Collections.sort(cmakeOptions);
                    for (String optKey : confLoader.getCmakeOptions().keySet()) {
                        System.console().printf(optKey.substring(optKey.indexOf(".") + 1) + ": " + confLoader.getCmakeOptions().get(optKey) + "\n");
                    }
                    System.console().printf("\nTo modify this options edit \"cmake.\" into \"config.property\" file.\n");
                    System.console().printf("\nPress any key to continue...\n");
                    System.console().readLine();
                    System.console().printf("\nDo you want to build source code (into folder '" + confLoader.getCMakeBuildFolder().replace("\"", "") + "')?\n"
                            + "WARNING: this operation may overwrite already built project! [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.console().printf("Configuring CMake option for compile.\n");
                        serverFolder = setGitFolder(confLoader.getGitFolderServer(), confLoader.getGitURLServer());
                        cmdManager.cmakeConfig(serverFolder, confLoader.getCMakeBuildFolder(), confLoader.getCmakeOptions(), null);
                    }
                    System.console().printf("\nDo you want to compile and install built source (into folder '" + confLoader.getCMakeRunFolder().replace("\"", "") + "')? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        if (cmdManager.cmakeInstall(confLoader.getCMakeBuildFolder(), confLoader.getCMakeRunFolder(), confLoader.getCMakeBuildType(), null)) {
                            System.console().printf("MaNGOS server is now installed. To complete the installation you need:\n");
                            System.console().printf("1. Add a new row inside \"realmlist\" table with new MaNGOS installed information.\n");
                            System.console().printf("2. Extract game data from a WoW client (use next tab to do this).\n");
                            System.console().printf("3. Configure \".conf\" file.\n");
                            System.console().printf("4. Run \"realmd\" and \"mangosd\" deamons from intall folder.\n");
                            System.console().printf("\n\nEnjoy!\n");
                            System.console().printf("\nPress any key to continue...\n");
                            System.console().readLine();
                        }
                    }
                } else {
                    System.console().printf("\n*** Compiling operation skipped. CMAKE command not ready.\n");
                }

                /**
                 * **** LUA Scripts install *****
                 */
                if (!confLoader.getGitURLEluna().isEmpty()) {
                    System.console().printf("\nDo you want to install LUA Script scripts? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input) && mysqlOk) {
                        System.console().printf("\n\n*** LUA Script database installation parameters:\n");
                        System.console().printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                        System.console().printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                        System.console().printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                        System.console().printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                        System.console().printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                        System.console().printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                        System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.console().printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setDatabaseServer(readNewParam("SERVER", confLoader.getDatabaseServer()));
                            confLoader.setDatabasePort(readNewParam("PORT", confLoader.getDatabasePort()));
                            confLoader.setDatabaseAdmin(readNewParam("ADMIN USER", confLoader.getDatabaseAdmin()));
                            confLoader.setDatabaseAdminPass(readNewParam("ADMIN PASS", confLoader.getDatabaseAdminPass()));
                            confLoader.setDatabaseUser(readNewParam("DB USER", confLoader.getDatabaseUser()));
                            confLoader.setDatabaseUserPass(readNewParam("DB PASS", confLoader.getDatabaseUserPass()));
                            System.console().printf("\n\n*** NEW Database server installation parameters:\n");
                            System.console().printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                            System.console().printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                            System.console().printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                            System.console().printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                            System.console().printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                            System.console().printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                        }
                        System.console().printf("\n\n*** LUA Script install configuration:\n");
                        System.console().printf("LUA SCRIPT FOLDER: " + confLoader.getGitFolderEluna().replace("\"", "") + "\n");
                        System.console().printf("SERVER RUN FOLDER: " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                        System.console().printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.console().printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setGitFolderEluna(readNewParam("LUA SCRIPT FOLDER", confLoader.getGitFolderEluna().replace("\"", "")));
                            confLoader.setCMakeRunFolder(readNewParam("SERVER RUN FOLDER", confLoader.getCMakeRunFolder().replace("\"", "")));
                            System.console().printf("\n\n*** NEW LUA Script install configuration:\n");
                            System.console().printf("LUA SCRIPT FOLDER: " + confLoader.getGitFolderEluna().replace("\"", "") + "\n");
                            System.console().printf("SERVER RUN FOLDER    : " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                        }

                        // Installing LUA Script database
                        String setupPath = elunaFolder + File.separator + "sql";
                        System.console().printf("Update database for LUA Script from: " + setupPath);
                        mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                databaseFolder, null, null, setupPath, confLoader.getWorldLoadDBName(),
                                null, cmdManager, null, null, null);

                        // Installing LUA scripts
                        String luaSrc = (confLoader.getGitFolderEluna().isEmpty() ? elunaFolder : confLoader.getGitFolderEluna().replace("\"", ""))
                                + File.separator + "lua_scripts";
                        String luaDst = confLoader.getCMakeRunFolder() + File.separator + "lua_scripts";
                        System.console().printf("Update LUA script from: " + setupPath);
                        boolean cpRet = cmdManager.copyFolder(luaSrc, luaDst, null);
                        if (cpRet) {
                            System.console().printf("Done\n");
                        } else {
                            System.console().printf("ERROR: check console log.\n");
                        }

                    } else {
                        System.console().printf("\n*** LUA Script operation skipped. MySQL command not ready.\n");
                    }
                }

                /**
                 * **** Map, VMap, MMAp extractions *****
                 */
                System.console().printf("\nDo you want to extract game data (Map, VMAp, MMAp) from WoW client? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input)) {
                    System.console().printf("\n\n*** Path configuration:\n");
                    String unixRun = "";
                    if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                        unixRun = "bin" + File.separator;
                    }
                    String toolPath = confLoader.getCMakeRunFolder() + File.separator + unixRun + "tools";
                    String dataPath = confLoader.getCMakeRunFolder() + File.separator + "data";;
                    String clientPath;
                    System.console().printf("\nInsert path to tools folder: [example: " + toolPath + "]");
                    toolPath = System.console().readLine();
                    System.console().printf("\nInsert path to data folder: [exmaple: " + dataPath + "]");
                    dataPath = System.console().readLine();
                    System.console().printf("\nInsert path to WoW client folder:");
                    clientPath = System.console().readLine();
                    boolean retOps = true;
                    for (int i = 1; i <= 4; i++) {
                        retOps &= cmdManager.mapExtraction(toolPath, clientPath, dataPath, i, null, null);
                    }
                    retOps &= cmdManager.deleteFolder(dataPath + File.separator + "Buildings");
                    if (retOps) {
                        System.console().printf("Done\n");
                    } else {
                        System.console().printf("ERROR: check console log.\n");
                    }
                }
            } else {
                System.console().printf("CRITICAL: Operatig system not supported.\n");
            }

        } else {
            MainWindow.main(args);
        }

    }

    private static String readNewParam(String paramName, String paramValue) {
        System.console().printf("Insert new value for '" + paramName + "' parameter [default: " + paramValue + "]:");
        String input = System.console().readLine();
        return (!input.isEmpty()) ? input.trim() : paramValue;
    }

}
