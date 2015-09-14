/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import static mangosui.WindowsCommands.writeToGUIConsole;

/**
 *
 * @author Boni
 */
public abstract class Command {

    private int debugLevel = 0; // 0: None, 1: console, 2: full

    /**
     *
     */
    public Command() {
    }

    abstract boolean isRepoUpToDate(String pathToRepo) throws InterruptedException, IOException;

    //abstract boolean gitOperation(Object console, String gitCommand, boolean toBuffer) throws InterruptedException, IOException;
    abstract boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) throws IOException, InterruptedException;

    abstract boolean cmakeInstall(String buildFolder, String runFolder, Object console) throws IOException, InterruptedException;
    
    abstract String checkOpenSSLInclude(String pathToOpenSSL, Object console);
    abstract String checkOpenSSLLib(String pathToOpenSSL, Object console);
    
    abstract String checkMySQLInclude(String pathToMySQL, Object console);
    abstract String checkMySQLLib(String pathToMySQL, Object console);

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
    protected boolean execute(String[] commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);

        if (!toBuffer && guiConsole == null) {
            /*builder.redirectErrorStream(true);*/
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            builder.inheritIO();
        }
        //builder.inheritIO();
        Process proc = builder.start();
        if (guiConsole != null) {
            Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\Z");
            writeToGUIConsole(proc.getInputStream(), guiConsole, proc);
        } else if (!toBuffer) {
            //Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\Z");
            //System.out.println(s.next());
        } else {
            Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\Z");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                rawConsole.append(line).append("\n");
            }
            reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                rawConsole.append(line).append("\n");
            }
        }
        proc.waitFor();
        //System.out.println(command + "\n " + sb.toString() + proc.exitValue());
        return proc.exitValue() <= 0;
    }

    static void writeToGUIConsole(InputStream in, Object console, Process proc) throws IOException, InterruptedException {
        try {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, line, ConsoleManager.getInstance().TEXT_BLACK);
                //proc.wait(500);
                //System.out.println(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
            return false;
        }
    }
    
    /**
     *
     * @return
     */
    public int getDebugLevel() {
        return debugLevel;
    }

    /**
     *
     * @param debugLevel
     */
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

}
