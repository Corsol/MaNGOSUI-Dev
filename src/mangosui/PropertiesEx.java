/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 * This class allows to handle Netbeans properties file. It is based on the work
 * of :
 * http://stackoverflow.com/questions/6233532/reading-java-properties-file-without-escaping-values.
 * It overrides both load methods in order to load a netbeans property file,
 * taking into account the \ that were escaped by java properties original load
 * methods.
 *
 * @author stephane
 */
public class PropertiesEx extends Properties {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param fis
     * @throws IOException
     */
    public synchronized void load(FileInputStream fis) throws IOException {
        Scanner in = new Scanner(fis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (in.hasNext()) {
            out.write(in.nextLine().replace("\\", "\\\\").getBytes());
            out.write("\n".getBytes());
        }//while

        InputStream is = new ByteArrayInputStream(out.toByteArray());
        super.load(is);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        BufferedReader bfr = new BufferedReader(reader);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String readLine;
        while ((readLine = bfr.readLine()) != null) {
            out.write(readLine.replace("\\", "\\\\").getBytes());
            out.write("\n".getBytes());
        }//while

        InputStream is = new ByteArrayInputStream(out.toByteArray());
        super.load(is);
    }

    @Override
    public void load(InputStream is) throws IOException {
        load(new InputStreamReader(is));
    }

    /**
     *
     * @param keyPart
     * @return
     */
    public synchronized HashMap<String, String> getPropertyArray(String keyPart) {
        HashMap<String, String> props = new HashMap<>();
        Set<String> propList = this.stringPropertyNames();
        ArrayList<String> mapKey = new ArrayList<>(propList);
        Collections.sort(mapKey);
        for (String prop : mapKey) {
            if (prop.contains(keyPart)) {
                //System.out.println("Adding: " + prop);
                props.put(prop, this.getProperty(prop));
            }
        }
        return props;
    }
}
