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
package com.tragicphantom.stdf;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.TimeZone;

public class RecordData{
   private RecordDescriptor desc;
   private Object []        fields;

   // TODO: make configurable
   //private static String dateFormat = "%1$tY-%1$tm-%1$tdT%1$tT";
   private static String dateFormat = "%1$ta %1$tb %1$td %1$tT %1$tY";

   public RecordData(RecordDescriptor desc, Object [] fields){
      this.desc   = desc;
      this.fields = fields;
   }

   public String getType(){
      return desc.getType();
   }

   public int size(){
      return fields.length;
   }

   public boolean hasField(String name){
      return desc.contains(name);
   }

   public void setField(int index, Object value){
      fields[index] = value;
   }

   public void setField(String name, Object value){
      setField(desc.getIndex(name), value);
   }

   public Object getField(String name){
      return getField(desc.getIndex(name));
   }

   public Object getField(int index){
      return fields[index];
   }

   public HashMap<String, Object> getFields(){
      HashMap<String, Object> values = new HashMap<String, Object>();
      int index = 0;
      for(Field field : desc.getFields())
         values.put(field.getName(), fields[index++]);
      return values;
   }

   public String getString(String name){
      int    index = desc.getIndex(name);
      Object value = getField(index);
      char   type  = desc.getFields()[index].getType();

      String repr;
      if(value == null)
         repr = "(null)";
      else if(type == 'V' || type == 'k'){
         StringBuilder lb = new StringBuilder();
         for(Object item : (Object[])value){
            if(lb.length() > 0)
               lb.append(", ");
            lb.append(item == null ? "(null)" : item.toString());
         }
         repr = lb.toString();
      }
      else if(value instanceof Double)
         repr = String.format("%f", (Double)value);
      else if(value instanceof Float)
         repr = String.format("%f", (Float)value);
      else if(value instanceof Byte)
         repr = String.format("%x", (Byte)value);
      else if(value instanceof byte[]){
         StringBuilder bb = new StringBuilder("[");
         for(byte b : (byte[])value){
            if(bb.length() > 1)
               bb.append(", ");
            bb.append(Byte.toString(b));
         }
         bb.append("]");
         repr = bb.toString();
      }
      else if((name.endsWith("_T") && !name.equals("TEST_T"))
              || name.equals("MOD_TIM")){
         Calendar cal = Calendar.getInstance();
         //cal.setTimeZone(TimeZone.getTimeZone("GMT"));
         cal.setTimeInMillis((Long)value * 1000L);
         repr = String.format("%1$ta %1$tb %1$td %1$tT %1$tY", cal);
      }
      else
         repr = value.toString();

      if(repr.length() == 0)
         repr = "(null)";

      return repr;
   }

   // this method has been tailored to output similar to libstdf format
   // to help with comparing output of the two libraries
   public String toString(){
      StringBuilder sb = new StringBuilder(getType().toUpperCase());
      sb.append("\n");

      // sort fields for easier comparisons
      ArrayList<String> names = new ArrayList<String>(desc.getFieldNames());
      Collections.sort(names);

      for(String name : names){
         sb.append("   ")
           .append(name)
           .append(": ")
           .append(getString(name))
           .append("\n");
      }

      return sb.toString();
   }
}
