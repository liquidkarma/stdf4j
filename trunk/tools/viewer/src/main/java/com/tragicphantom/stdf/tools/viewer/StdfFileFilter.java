/**
 * Copyright 2009-2012 tragicphantom
 *
 * This file is part of stdf4j.
 *
 * Stdf4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Stdf4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with stdf4j.  If not, see <http://www.gnu.org/licenses/>.
**/
package com.tragicphantom.stdf.tools.viewer;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class StdfFileFilter extends FileFilter{
   public String getDescription(){
      return "STDF Files";
   }

   public boolean accept(File f){
      if(f.isDirectory())
         return true;

      String name = f.getName();
      int i = name.lastIndexOf('.');

      if(i > 0 && i < name.length() - 1){
         String ext = name.substring(i + 1).toLowerCase();
         if(ext.equals("gz") || ext.equals("bz2")){
            int j = name.lastIndexOf('.', i - 1);
            if(j > 0)
               ext = name.substring(j + 1, i).toLowerCase();
         }

         if(ext.equals("std") || ext.equals("stdf"))
            return true;
      }

      return false;
   }
}
