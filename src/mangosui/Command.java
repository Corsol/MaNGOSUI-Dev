/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import static mangosui.CommandsWindows.writeToGUIConsole;

/**
 *
 * @author Boni
 */
public class Command {

    private int debugLevel = 0; // 0: None, 1: console, 2: full
    private static Command instance = null;
    private JButton btnInvoker;

    /**
     *
     */
    public Command() {
    }

    /**
     *
     * @return
     */
    public static Command getInstance() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
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
    protected boolean execute(String[] commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer) throws IOException, InterruptedException, ExecutionException {
        if (guiConsole != null) {
            ProcessExec proc = new ProcessExec(commands, guiConsole, btnInvoker);
            proc.execute();
            return false;
        } else {
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);
            System.out.print("EXECUTING:");
            for (String cmd : commands) {
                System.out.print(" " + cmd);
            }
            System.out.print("\n");
            if (!toBuffer) {
                //builder.redirectErrorStream(true);
                builder.redirectError(ProcessBuilder.Redirect.INHERIT);
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                builder.inheritIO();
            }
            //builder.inheritIO();
            Process proc = builder.start();
            if (!toBuffer) {
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
    }

    static void writeToGUIConsole(InputStream in, Object console, Process proc) throws IOException, InterruptedException {
        try {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, line, ConsoleManager.TEXT_BLACK);
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

    public boolean copyFolder(File src, File dest) {
        try {
            if (src.isDirectory()) {

                //if directory not exists, create it
                if (!dest.exists()) {
                    dest.mkdir();
                    //System.out.println("Directory copied from " + src + "  to " + dest);
                }

                //list all the directory contents
                String files[] = src.list();

                for (String file : files) {
                    //construct the src and dest file structure
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    //recursive copy
                    copyFolder(srcFile, destFile);
                }

            } else {
                //if file, then copy it
                //Use bytes stream to support all file types
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes 
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
                //System.out.println("File copied from " + src + " to " + dest);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
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

    public JButton getBtnInvoker() {
        return btnInvoker;
    }

    public void setBtnInvoker(JButton btnInvoker) {
        this.btnInvoker = btnInvoker;
    }

}
