/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Boni
 */
public class FileFilterBuild implements FilenameFilter {

    private final String[] koFileExtensions
            = new String[]{".ilk", ".pdb"};

    /*@Override
     public boolean accept(File file)
     {
     }*/
    @Override
    public boolean accept(File dir, String name) {
        for (String extension : koFileExtensions) {
            if (name.toLowerCase().endsWith(extension)) {
                return false;
            }
        }
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}