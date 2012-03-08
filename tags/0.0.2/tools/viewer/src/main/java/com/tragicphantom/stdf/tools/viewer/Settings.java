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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class Settings{
   static File settingsFile = null;
   static HashMap settings = new HashMap();

   static Vector<SettingsProvider> settingsProviders = new Vector<SettingsProvider>();

   static{
      getSettingsFile();
   }

   public static void getSettingsFile(){
      String fileDelim = System.getProperty("file.separator");
      String settingsString = System.getProperty("user.home") + fileDelim + ".stdfview";
      settingsFile = new File(settingsString);
   }

   public static void save(){
      if(settingsFile != null){
         try{
            getSettings();

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(settingsFile));
            oos.writeObject(settings);
            oos.close();
         }
         catch(Exception e){
            e.printStackTrace();
         }
      }
   }

   public static void restore(){
      // load default settings
      getSettings();

      if(settingsFile != null){
         try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingsFile));
            settings = (HashMap)ois.readObject();
            ois.close();

            setSettings();
         }
         catch(FileNotFoundException e){
         }
         catch(Exception e){
            e.printStackTrace();
         }
      }
   }

   public static void print(){
      Set keys = settings.keySet();
      Iterator keyIter = keys.iterator();
      System.out.println("Values:");
      while(keyIter.hasNext()){
         String key = keyIter.next().toString();
         System.out.println("   " + key + " -> " + settings.get(key).toString());
      }
   }

   public static boolean containsKey(Object key){
      return settings.containsKey(key);
   }

   public static Object get(Object key, Object defaultValue){
      if(settings.containsKey(key))
         return settings.get(key);
      else
         return defaultValue;
   }

   public static Object put(Object key, Object value){
      return settings.put(key, value);
   }

   public static void getSettings(){
      try{
         for(SettingsProvider provider : settingsProviders){
            if(provider != null)
               provider.getSettings();
         }
      }
      catch(Exception e){
         e.printStackTrace();
      }
   }

   public static void setSettings(){
      try{
         for(SettingsProvider provider : settingsProviders){
            if(provider != null)
               provider.setSettings();
         }
      }
      catch(Exception e){
         //e.printStackTrace();
      }
   }

   public static void addSettingsProvider(SettingsProvider provider){
      if(provider != null)
         settingsProviders.addElement(provider);
   }
}
