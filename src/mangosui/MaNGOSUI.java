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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class MaNGOSUI extends WorkExecutor {

    private static boolean gitOk = false;
    private static boolean mysqlOk = false;
    private static boolean cmakeOk = false;
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MaNGOSUI.class.getName());

    /**
     * Execute the Mangos Universal Installer application and use GUI or Console
     * mode.<br/>
     * To use Console mode specify -c parameter. By default GUI mode is used.
     *
     * @param args The String[] value with arguments
     */
    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("-c")) {
            // Starting Console mode
            System.out.printf("MaNGOS Universal Installer: console mode.\nCredit to: Antz (for MySQL and OpenSSL checks) and Faded (for unix dependecy setup)\n\n");

            System.out.printf("Loading configuration file... ");
            ConfLoader confLoader = new ConfLoader();
            if (confLoader.isConfLoaded()) {
                System.out.printf(" Done!\n\n");
            } else {
                // Configuration file not loaded. Execution will be terminated.
                System.out.printf(" FAILED! Check conf.property file.\n\n");
                return;
            }

            CommandManager cmdManager = new CommandManager();
            cmdManager.setDebugLevel(confLoader.getDebugLevel());
            if (cmdManager.getCURR_OS() > 0) {
                // Current operating system is reconized from available operating systems. Console mode can go ahead
                String input;
                String serverFolder;
                serverFolder = setGitFolder(confLoader.getGitFolderServer(), confLoader.getGitURLServer());
                String databaseFolder;
                databaseFolder = setGitFolder(confLoader.getGitFolderDatabase(), confLoader.getGitURLDatabase());
                String elunaFolder;
                elunaFolder = setGitFolder(confLoader.getGitFolderEluna(), confLoader.getGitURLEluna());

                System.out.printf("INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch() + "\n\n");

                /**
                 * ** Dependencies checks ***
                 */
                gitOk = checkGit(confLoader.getWinGitHubPath(), confLoader.getWinGitExtPath(), cmdManager, null);
                if (!gitOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.out.printf("WARNING: Git not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.out.printf(" Visit \"" + confLoader.getURLGit() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.out.printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupGit(null, null);
                            }
                        }
                    } else {
                        System.out.printf("");
                        System.out.printf("To install Git run MaNGOS UI in root console.\n");
                    }
                }
                mysqlOk = checkMySQL(databaseFolder, confLoader.getPathToMySQL(), cmdManager, null);
                if (!mysqlOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.out.printf("WARNING: MySQL not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.out.printf(" Visit \"" + confLoader.getURLMySQL() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.out.printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupMySQL(null, null);
                            }
                        }
                    } else {
                        System.out.printf("\n");
                        System.out.printf("To install MySQL run MaNGOS UI in root console.\n");
                    }
                }
                cmakeOk = checkCMake(confLoader.getWin32PathCMake(), confLoader.getWin64PathCMake(), cmdManager, null);
                if (!cmakeOk) {
                    if (cmdManager.checkRootConsole(null)) {
                        System.out.printf("WARNING: CMake not installed.");
                        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
                            System.out.printf(" Visit \"" + confLoader.getURLCMake() + "\" to download and install it.\n");
                        } else if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                            System.out.printf(" Do you want to install it now? [y/n, default:n]\n");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmdManager.setupCMake(null, null);
                                cmdManager.setupOpenSSL(null, null);
                            }
                        }
                    } else {
                        System.out.printf("To install CMake run MaNGOS UI in root console.\n");
                    }
                }
                if (cmakeOk) {
                    if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && cmdManager.checkMySQLLib("", null).isEmpty() || cmdManager.checkMySQLInclude("", null).isEmpty()) {
                        System.out.printf("ERROR: MySQL library for CMAKE was not found on system. Do you want to use portable version?\n");
                        System.out.printf("WARNING: Portable MySQL library is for 5.6 version. Use this for other MySQL version can be insecure.\n");
                        System.out.printf("Do you want to install MySQL portable library? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            cmakeOk = cmdManager.installMySQLPortable(null);
                            if (!cmakeOk) {
                                System.out.printf("ERROR: MySQL library for CMAKE not installed. Try again running as Administrator!\n");
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

                // Version selection to use correct parameters
                System.out.printf("\nSelect wich version of MaNGOS install:\n");
                System.out.printf("0 - MaNGOS Zero\n");
                System.out.printf("1 - MaNGOS One\n");
                System.out.printf("2 - MaNGOS Two\n");
                System.out.printf("3 - MaNGOS Three\n");
                System.out.printf("4 - MaNGOS Four\n");
                System.out.printf("Version ? [0-4, default:0] ");
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
                System.out.printf("\nDo you want to download source from Git repositories? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && gitOk) {
                    System.out.printf("\nProxy configuration:\n");
                    System.out.printf("Server name: " + confLoader.getProxyServer() + "\n");
                    System.out.printf("Server port: " + confLoader.getProxyPort() + "\n");
                    System.out.printf("\nDo you want to use a proxy server with this parameter? [y/n, default:n] ");
                    input = System.console().readLine();
                    if (!"y".equalsIgnoreCase(input)) {
                        confLoader.setProxyPort("");
                        confLoader.setProxyServer("");
                        cmdManager.remGitProxy(null);
                    } else {
                        if (cmdManager.setGitProxy(confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                            confLoader.setProxyPort("");
                            confLoader.setProxyServer("");
                            System.out.printf("Global proxy settings applyed\n");
                        } else {
                            System.out.printf("WARNING: Failed to apply global settings! Proxy settings will be used on local git operation.\n");
                        }
                    }
                    // Show server git param
                    System.out.printf("\n\n*** Server download parameters:\n");
                    System.out.printf("URL                          : " + confLoader.getGitURLServer() + "\n");
                    System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer() + "\n");
                    System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer() + "\n");
                    System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.out.printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setGitURLServer(readNewParam("URL", confLoader.getGitURLServer()));
                        confLoader.setGitFolderServer(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderServer()));
                        confLoader.setGitBranchServer(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchServer()));
                        System.out.printf("\n\n*** NEW Server download parameters:\n");
                        System.out.printf("URL                          : " + confLoader.getGitURLServer() + "\n");
                        System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer() + "\n");
                        System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer() + "\n");
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
                    System.out.printf("\n*** Server download option avaiable:\n");
                    if (optGitSrvInstall) {
                        System.out.printf("N - New download.\n");
                    }
                    if (optGitSrvWipe) {
                        System.out.printf("W - Wipe current and download again.\n");
                    }
                    if (optGitSrvUpdate) {
                        System.out.printf("U - Update local server.\n");
                    }
                    System.out.printf("Empty for no action.\n");
                    System.out.printf("\nInsert action: ");
                    input = System.console().readLine();
                    gitDownload(input, confLoader.getGitURLServer(), serverFolder, confLoader.getGitBranchServer(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null);

                    // Show database git param
                    System.out.printf("\n\n*** Database download parameters:\n");
                    System.out.printf("URL                          : " + confLoader.getGitURLDatabase() + "\n");
                    System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase() + "\n");
                    System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase() + "\n");
                    System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.out.printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setGitURLDatabase(readNewParam("URL", confLoader.getGitURLDatabase()));
                        confLoader.setGitFolderDatabase(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderDatabase()));
                        confLoader.setGitBranchDatabase(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchDatabase()));
                        System.out.printf("\n\n*** NEW Database download parameters:\n");
                        System.out.printf("URL                          : " + confLoader.getGitURLDatabase() + "\n");
                        System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase() + "\n");
                        System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase() + "\n");
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
                    System.out.printf("\n*** Database download option avaiable:\n");
                    if (optGitDBInstall) {
                        System.out.printf("N - New download.\n");
                    }
                    if (optGitDBWipe) {
                        System.out.printf("W - Wipe current and download again.\n");
                    }
                    if (optGitDBUpdate) {
                        System.out.printf("U - Update local database.\n");
                    }
                    System.out.printf("Empty for no action.\n");
                    System.out.printf("\nInsert action: ");
                    input = System.console().readLine();
                    gitDownload(input, confLoader.getGitURLDatabase(), databaseFolder, confLoader.getGitBranchDatabase(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null);

                    // Show LUA Script git param if lua git url is present in property file
                    if (!confLoader.getGitURLEluna().isEmpty()) {
                        System.out.printf("\n\n*** LUA Script download parameters:\n");
                        System.out.printf("URL                          : " + confLoader.getGitURLEluna() + "\n");
                        System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderEluna() + "\n");
                        System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchEluna() + "\n");
                        System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.out.printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setGitURLEluna(readNewParam("URL", confLoader.getGitURLEluna()));
                            confLoader.setGitFolderEluna(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderEluna()));
                            confLoader.setGitBranchEluna(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchEluna()));
                            System.out.printf("\n\n*** NEW LUA Script download parameters:\n");
                            System.out.printf("URL                          : " + confLoader.getGitURLEluna() + "\n");
                            System.out.printf("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderEluna() + "\n");
                            System.out.printf("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchEluna() + "\n");
                        }
                        // Check LUA folder
                        if (cmdManager.checkFolder(elunaFolder)) {
                            optGitElunaWipe = true;
                            // Check LUA version for update
                            if (!cmdManager.isRepoUpToDate(elunaFolder)) {
                                optGitElunaUpdate = true;
                            }
                        } else {
                            optGitElunaInstall = true;
                        }
                        // Ask what to do (first clone, wipe and clone, checkout and update) for LUA
                        System.out.printf("\n*** LUA Script download option avaiable:\n");
                        if (optGitElunaInstall) {
                            System.out.printf("N - New download.\n");
                        }
                        if (optGitElunaWipe) {
                            System.out.printf("W - Wipe current and download again.\n");
                        }
                        if (optGitElunaUpdate) {
                            System.out.printf("U - Update local LUA Script.\n");
                        }
                        System.out.printf("Empty for no action.\n");
                        System.out.printf("\nInsert action: ");
                        input = System.console().readLine();
                        gitDownload(input, confLoader.getGitURLEluna(), elunaFolder, confLoader.getGitBranchEluna(), confLoader.getProxyServer(), confLoader.getProxyPort(), cmdManager, null);
                    }
                } else {
                    System.out.printf("\n*** Download operation skipped. Git command not ready.\n");
                }

                /**
                 * **** Database install *****
                 */
                System.out.printf("\nDo you want to install databases? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && mysqlOk) {
                    // Show and get database configuration param
                    System.out.printf("\n\n*** Database server installation parameters:\n");
                    System.out.printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                    System.out.printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                    System.out.printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                    System.out.printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                    System.out.printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                    System.out.printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                    System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.out.printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setDatabaseServer(readNewParam("SERVER", confLoader.getDatabaseServer()));
                        confLoader.setDatabasePort(readNewParam("PORT", confLoader.getDatabasePort()));
                        confLoader.setDatabaseAdmin(readNewParam("ADMIN USER", confLoader.getDatabaseAdmin()));
                        confLoader.setDatabaseAdminPass(readNewParam("ADMIN PASS", confLoader.getDatabaseAdminPass()));
                        confLoader.setDatabaseUser(readNewParam("DB USER", confLoader.getDatabaseUser()));
                        confLoader.setDatabaseUserPass(readNewParam("DB PASS", confLoader.getDatabaseUserPass()));
                        System.out.printf("\n\n*** NEW Database server installation parameters:\n");
                        System.out.printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                        System.out.printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                        System.out.printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                        System.out.printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                        System.out.printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                        System.out.printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                    }
                    // Show and get setup and update file and directory to be used for each database
                    System.out.printf("\n\n*** Database install configuration:\n");
                    System.out.printf("WORLD DB       : " + confLoader.getWorldDBName() + "\n");
                    for (String upd : confLoader.getWorldUpdRel().values()) {
                        System.out.printf(" - UPD RELEASE : " + upd + "\n");
                    }
                    System.out.printf("CHAR DB        : " + confLoader.getCharDBName() + "\n");
                    for (String upd : confLoader.getCharUpdRel().values()) {
                        System.out.printf(" - UPD RELEASE : " + upd + "\n");
                    }
                    System.out.printf("REALM DB       : " + confLoader.getRealmDBName() + "\n");
                    for (String upd : confLoader.getRealmUpdRel().values()) {
                        System.out.printf(" - UPD RELEASE : " + upd + "\n");
                    }
                    System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        HashMap<String, String> newUpdRel = new HashMap<>();
                        System.out.printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setWorldDBName(readNewParam("WORLD DB", confLoader.getWorldDBName()));
                        for (String updKey : confLoader.getWorldUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getWorldUpdRel().get(updKey)));
                        }
                        confLoader.setWorldUpdRel(newUpdRel);
                        confLoader.setCharDBName(readNewParam("CHAR DB", confLoader.getCharDBName()));
                        newUpdRel = new HashMap<>();
                        for (String updKey : confLoader.getCharUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getCharUpdRel().get(updKey)));
                        }
                        confLoader.setCharUpdRel(newUpdRel);
                        confLoader.setRealmDBName(readNewParam("REALM DB", confLoader.getRealmDBName()));
                        newUpdRel = new HashMap<>();
                        for (String updKey : confLoader.getRealmUpdRel().keySet()) {
                            newUpdRel.put(updKey, readNewParam(" - UPD RELEASE", confLoader.getRealmUpdRel().get(updKey)));
                        }
                        confLoader.setRealmUpdRel(newUpdRel);
                        System.out.printf("\n\n*** NEW Database install configuration:\n");
                        System.out.printf("WORLD DB    : " + confLoader.getWorldDBName() + "\n");
                        System.out.printf("CHAR DB     : " + confLoader.getCharDBName() + "\n");
                        System.out.printf("REALM DB    : " + confLoader.getRealmDBName() + "\n");
                        System.out.printf("WORLD DB       : " + confLoader.getWorldDBName() + "\n");
                        for (String upd : confLoader.getWorldUpdRel().values()) {
                            System.out.printf(" - UPD RELEASE : " + upd + "\n");
                        }
                        System.out.printf("CHAR DB        : " + confLoader.getCharDBName() + "\n");
                        for (String upd : confLoader.getCharUpdRel().values()) {
                            System.out.printf(" - UPD RELEASE : " + upd + "\n");
                        }
                        System.out.printf("REALM DB       : " + confLoader.getRealmDBName() + "\n");
                        for (String upd : confLoader.getRealmUpdRel().values()) {
                            System.out.printf(" - UPD RELEASE : " + upd + "\n");
                        }
                    }

                    if (!optGitDBInstall && !optGitDBUpdate) {
                        // Check what can be done with databases
                        optGitDBWipe = false;
                        System.out.printf("\n\n*** Database installation option avaiable:\n");
                        System.out.printf("N - New install.\n");
                        System.out.printf("W - Wipe current and install again.\n");
                        System.out.printf("U - Update database.\n");
                        System.out.printf("Empty for no action.\n");
                        System.out.printf("\nInsert action: ");
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
                            // First database configuration if it's a fresh installation
                            System.out.printf("\nIs your first DB installation: [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                mysqlCreateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                        confLoader.getDatabaseUser(), confLoader.getDatabaseUserPass(), confLoader.getWorldDBName(), confLoader.getCharDBName(),
                                        confLoader.getRealmDBName(), cmdManager, null);
                            }
                        }
                        System.out.printf("\nDo you want to wipe (if already installed) and install Realm database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Wipe and install Realm database
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), confLoader.getRealmLoadDBName(), confLoader.getRealmDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getRealmUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), updFolders, null, confLoader.getRealmDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }

                        System.out.printf("\nDo you want to wipe (if already installed) and install Character database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Wipe and install Character database
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), confLoader.getCharLoadDBName(), confLoader.getCharDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getCharUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), updFolders, null, confLoader.getCharDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }

                        System.out.printf("\nDo you want to wipe (if already installed) and install World database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Wipe and install Mangos database
                            mysqlLoadDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), confLoader.getWorldLoadDBName(), confLoader.getWorldDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null);

                            ArrayList<String> loadFolder = new ArrayList<>();
                            loadFolder.add(confLoader.getWorldFullDB());
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), loadFolder, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseSetupFolder(), cmdManager, null, null);

                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getWorldUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), updFolders, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }
                    } else if (optGitDBUpdate) {
                        System.out.printf("\nDo you want to update Realm database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Update only Realm database
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getRealmUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getRealmFolder(), updFolders, null, confLoader.getRealmDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }

                        System.out.printf("\nDo you want update Character database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Update only Character database
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getCharUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getCharFolder(), updFolders, null, confLoader.getCharDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }

                        System.out.printf("\nDo you want to update World database? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            // Update only Mangos database
                            ArrayList<String> updFolders = new ArrayList<>(confLoader.getWorldUpdRel().values());
                            Collections.sort(updFolders);
                            mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                    databaseFolder, confLoader.getWorldFolder(), updFolders, null, confLoader.getWorldDBName(),
                                    confLoader.getDatabaseUpdateFolder(), cmdManager, null, null);
                        }
                    }
                } else {
                    System.out.printf("\n*** Database operation skipped. MySQL command not ready.\n");
                }

                /**
                 * **** Server compile and install *****
                 */
                System.out.printf("\nDo you want to build and install serve sources? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input) && cmakeOk) {
                    // Show and get compile and build options
                    System.out.printf("\n\n*** Buil and install folders configuration:\n");
                    System.out.printf("BUILD FOLDER  : " + confLoader.getCMakeBuildFolder().replace("\"", "") + "\n");
                    System.out.printf("INSTALL FOLDER: " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                    System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        System.out.printf("\nTo remove a param insert a blank space.\n");
                        confLoader.setCMakeBuildFolder(readNewParam("BUILD FOLDER", confLoader.getCMakeBuildFolder().replace("\"", "")));
                        confLoader.setCMakeRunFolder(readNewParam("INSTALL FOLDER", confLoader.getCMakeRunFolder().replace("\"", "")));
                        System.out.printf("\n\n*** NEW buil and install folders configuration:\n");
                        System.out.printf("LUA SCRIPT FOLDER: " + confLoader.getCMakeBuildFolder().replace("\"", "") + "\n");
                        System.out.printf("SERVER RUN FOLDER    : " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                    }
                    System.out.printf("\n*** Other build options (empty values means default value):\n");
                    ArrayList<String> cmakeOptions = new ArrayList<>();
                    Collections.sort(cmakeOptions);
                    for (String optKey : confLoader.getCmakeOptions().keySet()) {
                        System.out.printf(optKey.substring(optKey.indexOf('.') + 1) + ": " + confLoader.getCmakeOptions().get(optKey) + "\n");
                    }
                    System.out.printf("\nTo modify this options edit \"cmake.\" into \"config.property\" file.\n");
                    System.out.printf("\nPress any key to continue...\n");
                    System.console().readLine();
                    System.out.printf("\nDo you want to build source code (into folder '" + confLoader.getCMakeBuildFolder().replace("\"", "") + "')?\n"
                            + "WARNING: this operation may overwrite already built project! [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        // Do cmake configuration
                        System.out.printf("Configuring CMake option for compile.\n");
                        serverFolder = setGitFolder(confLoader.getGitFolderServer(), confLoader.getGitURLServer());
                        cmdManager.cmakeConfig(serverFolder, confLoader.getCMakeBuildFolder(), confLoader.getCmakeOptions(), null);
                    }
                    System.out.printf("\nDo you want to compile and install built source (into folder '" + confLoader.getCMakeRunFolder().replace("\"", "") + "')? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input)) {
                        // Do cmake build and install
                        if (cmdManager.cmakeInstall(confLoader.getCMakeBuildFolder(), confLoader.getCMakeRunFolder(), confLoader.getCMakeBuildType(), null)) {
                            System.out.printf("\n\nMaNGOS server is now installed. To complete the installation you need:\n");
                            System.out.printf("1. Add a new row inside \"realmlist\" table with new MaNGOS installed information.\n");
                            System.out.printf("2. Extract game data from a WoW client (use next tab to do this).\n");
                            System.out.printf("3. Configure \".conf\" file.\n");
                            System.out.printf("4. Run \"realmd\" and \"mangosd\" deamons from intall folder.\n");
                            System.out.printf("\n\nEnjoy!\n");
                            System.out.printf("\nPress any key to continue...\n");
                            System.console().readLine();
                        }
                    }
                } else {
                    System.out.printf("\n*** Compiling operation skipped. CMAKE command not ready.\n");
                }

                /**
                 * **** LUA Scripts install *****
                 */
                if (!confLoader.getGitURLEluna().isEmpty()) {
                    System.out.printf("\nDo you want to install LUA Script scripts? [y/n, default:n] ");
                    input = System.console().readLine();
                    if ("y".equalsIgnoreCase(input) && mysqlOk) {
                        // Show and get LUA installation param
                        System.out.printf("\n\n*** LUA Script database installation parameters:\n");
                        System.out.printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                        System.out.printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                        System.out.printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                        System.out.printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                        System.out.printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                        System.out.printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                        System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.out.printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setDatabaseServer(readNewParam("SERVER", confLoader.getDatabaseServer()));
                            confLoader.setDatabasePort(readNewParam("PORT", confLoader.getDatabasePort()));
                            confLoader.setDatabaseAdmin(readNewParam("ADMIN USER", confLoader.getDatabaseAdmin()));
                            confLoader.setDatabaseAdminPass(readNewParam("ADMIN PASS", confLoader.getDatabaseAdminPass()));
                            confLoader.setDatabaseUser(readNewParam("DB USER", confLoader.getDatabaseUser()));
                            confLoader.setDatabaseUserPass(readNewParam("DB PASS", confLoader.getDatabaseUserPass()));
                            System.out.printf("\n\n*** NEW Database server installation parameters:\n");
                            System.out.printf("SERVER     : " + confLoader.getDatabaseServer() + "\n");
                            System.out.printf("PORT       : " + confLoader.getDatabasePort() + "\n");
                            System.out.printf("ADMIN USER : " + confLoader.getDatabaseAdmin() + "\n");
                            System.out.printf("ADMIN PASS : " + confLoader.getDatabaseAdminPass() + "\n");
                            System.out.printf("DB USER    : " + confLoader.getDatabaseUser() + "\n");
                            System.out.printf("DB PASS    : " + confLoader.getDatabaseUserPass() + "\n");
                        }
                        System.out.printf("\n\n*** LUA Script install configuration:\n");
                        System.out.printf("LUA SCRIPT FOLDER: " + confLoader.getGitFolderEluna().replace("\"", "") + "\n");
                        System.out.printf("SERVER RUN FOLDER: " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                        System.out.printf("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.out.printf("\nTo remove a param insert a blank space.\n");
                            confLoader.setGitFolderEluna(readNewParam("LUA SCRIPT FOLDER", confLoader.getGitFolderEluna().replace("\"", "")));
                            confLoader.setCMakeRunFolder(readNewParam("SERVER RUN FOLDER", confLoader.getCMakeRunFolder().replace("\"", "")));
                            System.out.printf("\n\n*** NEW LUA Script install configuration:\n");
                            System.out.printf("LUA SCRIPT FOLDER: " + confLoader.getGitFolderEluna().replace("\"", "") + "\n");
                            System.out.printf("SERVER RUN FOLDER    : " + confLoader.getCMakeRunFolder().replace("\"", "") + "\n");
                        }

                        // Installing LUA database script
                        String setupPath = elunaFolder + File.separator + "sql";
                        System.out.printf("Update database for LUA Script from: " + setupPath);
                        mysqlUpdateDB(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                                databaseFolder, null, null, setupPath, confLoader.getWorldLoadDBName(),
                                null, cmdManager, null, null);

                        // Installing LUA scripts
                        String luaSrc = (confLoader.getGitFolderEluna().isEmpty() ? elunaFolder : confLoader.getGitFolderEluna().replace("\"", ""))
                                + File.separator + "lua_scripts";
                        String luaDst = confLoader.getCMakeRunFolder() + File.separator + "lua_scripts";
                        System.out.printf("Update LUA script from: " + setupPath);
                        boolean cpRet = cmdManager.copyFolder(luaSrc, luaDst, null);
                        if (cpRet) {
                            System.out.printf("Done\n");
                        } else {
                            System.out.printf("ERROR: check console log.\n");
                        }

                    } else {
                        System.out.printf("\n*** LUA Script operation skipped. MySQL command not ready.\n");
                    }
                }

                /**
                 * **** Map, VMap, MMAp extractions *****
                 */
                System.out.printf("\nDo you want to extract game data (Map, VMAp, MMAp) from WoW client? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input)) {
                    System.out.printf("\n\n*** Path configuration:\n");
                    String unixRun = "";
                    if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
                        unixRun = "bin" + File.separator;
                    }
                    String toolPath = confLoader.getCMakeRunFolder() + File.separator + unixRun + "tools";
                    String dataPath = confLoader.getCMakeRunFolder() + File.separator + "data";;
                    String clientPath;
                    System.out.printf("\nInsert path to tools folder: [example: " + toolPath + "]");
                    toolPath = System.console().readLine();
                    System.out.printf("\nInsert path to data folder: [exmaple: " + dataPath + "]");
                    dataPath = System.console().readLine();
                    System.out.printf("\nInsert path to WoW client folder:");
                    clientPath = System.console().readLine();
                    boolean retOps = true;
                    for (int i = 1; i <= 4; i++) {
                        retOps &= cmdManager.mapExtraction(toolPath, clientPath, dataPath, i, null, null);
                    }
                    retOps &= cmdManager.deleteFolder(dataPath + File.separator + "Buildings");
                    if (retOps) {
                        System.out.printf("Done\n");
                    } else {
                        System.out.printf("ERROR: check console log.\n");
                    }
                }
            } else {
                // Current operating system is NOT reconized from available operating systems. Console mode will exit
                System.out.printf("CRITICAL: Operatig system not supported.\n");
            }

        } else {
            // Starting GUI mode
            MainWindow.main(args);
        }

    }

    /**
     * Read new value from console and return readed value
     *
     * @param paramName The String value of name for parameter to be readed
     * @param paramValue The String value of default value for this parameter if
     * present
     * @return The String value readed from console. This value can be empty but
     * not null
     */
    private static String readNewParam(String paramName, String paramValue) {
        System.out.printf("Insert new value for '" + paramName + "' parameter [default: " + paramValue + "]:");
        String input = System.console().readLine();
        return (!input.isEmpty()) ? input.trim() : paramValue;
    }

}
