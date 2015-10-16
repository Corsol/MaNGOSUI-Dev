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
 * of: http://stackoverflow.com/questions/6233532/reading-java-properties-file-without-escaping-values.
 * It overrides both load methods in order to load a netbeans property file,
 * taking into account the \ that were escaped by java properties original load
 * methods.
 *
 * @author stephane and adaption of Boni Simone <simo.boni@gmail.com>
 */
public class PropertiesEx extends Properties {

    private static final long serialVersionUID = 1L;

    /**
     * Load data from property file
     *
     * @param fis The FileInputStream object that represent property file
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
        this.load(new InputStreamReader(is));
    }

    /**
     * Read a list of property with similar root (like and array) and store it into an HashMap
     *
     * @param keyPart The String value of root that identify multiple properties
     * @return an HashMap with properties found with key and relative value, empty HashMap if no root property found
     */
    public synchronized HashMap<String, String> getPropertyArray(String keyPart) {
        HashMap<String, String> props = new HashMap<>();
        Set<String> propList = this.stringPropertyNames();
        ArrayList<String> mapKey = new ArrayList<>(propList);
        Collections.sort(mapKey);
        for (String prop : mapKey) {
            if (prop.contains(keyPart)) {
                props.put(prop, this.getProperty(prop));
            }
        }
        return props;
    }
}
