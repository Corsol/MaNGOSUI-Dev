/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JTextField;

/**
 *
 * @author Simone
 */
public class ProcessExec extends javax.swing.SwingWorker {

    String line;
    String[] commands;
    Object guiConsole;
    JButton btnInvoker;

    public ProcessExec(String[] commands, Object guiConsole, JButton btnInvoker) {
        this.commands = commands;
        this.guiConsole = guiConsole;
        this.btnInvoker = btnInvoker;
    }

    //implements a method in the swingworker
    @Override
    public Object doInBackground() throws Exception {
        boolean done = false;
        int exitValue = 0;
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        System.out.print("EXECUTING:");
        for (String cmd : commands) {
            System.out.print(" " + cmd);
        }
        //System.out.print("\n");
        //builder.inheritIO();
        Process proc = builder.start();
        while (!done) {
            try {
                //publish("Executing");
                BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                while ((line = is.readLine()) != null) {
                    ConsoleManager.getInstance().updateGUIConsole(guiConsole, line, ConsoleManager.TEXT_BLACK);
                    //publish((line + "\n"));
                }
                //if (guiConsole != null) {
                    //ConsoleManager.getInstance().updateGUIConsole(guiConsole, proc.getInputStream(), ConsoleManager.TEXT_BLACK);
                //    writeToGUIConsole(proc.getInputStream(), guiConsole, proc);
                //}
                exitValue = proc.exitValue();
                done = true;
            } catch (IllegalThreadStateException e) {
                // This exception will be thrown only if the process is still running 
                // because exitValue() will not be a valid method call yet...
                //logger.info("Process is still running...")
            }
        }
        return exitValue;
        //publish();
        //proc.waitFor();
        //System.out.println(command + "\n " + sb.toString() + proc.exitValue());
        //return Boolean.valueOf(exitValue <= 0);

        /*
         //Read Process Stream Output and write to LOG file
         BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));

         while ((line = is.readLine()) != null) {
         ConsoleManager.getInstance().updateGUIConsole(guiConsole, line, ConsoleManager.TEXT_BLACK);
         publish((line + "\n"));
         }
         System.out.flush();*/
         //return null;
    }

    //This will happen on the UI Thread.
    /**
     *
     * @param lines
     */
    protected void process(List lines) {
        for (String o : lines.getItems()) {
            ConsoleManager.getInstance().updateGUIConsole(guiConsole, o, ConsoleManager.TEXT_BLACK);

            //String currText = guiConsole.getText();
            //jtp.setText(currText + "\n" + o);
        }
    }

    @Override
    public void done() {
        try {
            //get();
            if (btnInvoker != null) {
                if ((int) get() == 0) {
                    btnInvoker.setText("DONE");
                    //btnInvoker.setActionCommand("DONE");
                } else {
                    //btnInvoker.setActionCommand("ERROR");
                    btnInvoker.setText("ERROR");
                }
                btnInvoker.setEnabled(true);
            }
            //You will get here if everything was OK.  So show a popup or something to signal done.
        } catch (InterruptedException | ExecutionException ex) {
            //this is where your IO Exception will surface, should you have one.
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
}
