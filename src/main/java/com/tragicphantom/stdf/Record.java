/**
 * Copyright 2009 tragicphantom
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

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Calendar;

public class Record{
   private String                  type;
   private HashMap<String, Object> fields = new HashMap<String, Object>();

   public Record(String type){
      this.type = type;
   }

   public void addField(String name, Object value){
      fields.put(name, value);
   }

   public String getType(){
      return type;
   }

   public HashMap<String, Object> getFields(){
      return fields;
   }

   public int size(){
      return fields.size();
   }

   public boolean hasField(String name){
      return fields.containsKey(name);
   }

   public Object getField(String name){
      return fields.get(name);
   }

   // this method has been tailored to output similar to libstdf format
   // to help with comparing output of the two libraries
   public String toString(){
      StringBuilder sb = new StringBuilder(type.toUpperCase());
      sb.append("\n");
      // sort fields using TreeMap for easier comparisons
      for(Map.Entry<String, Object> field : new TreeMap<String, Object>(fields).entrySet()){
         String name  = field.getKey();
         Object value = field.getValue();

         String repr;
         if(value == null)
            repr = "(null)";
         else if(value instanceof Double)
            repr = String.format("%f", (Double)value);
         else if(value instanceof Float)
            repr = String.format("%f", (Float)value);
         else if(value instanceof ArrayList){
            StringBuilder lb = new StringBuilder();
            for(Object item : (ArrayList)value){
               if(lb.length() > 0)
                  lb.append(", ");
               lb.append(item == null ? "(null)" : item.toString());
            }
            repr = lb.toString();
         }
         else if(value instanceof Byte)
            repr = String.format("%x", (Byte)value);
         else if(name.endsWith("_T")){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis((Long)value * 1000L);
            repr = String.format("%1$ta %1$tb %1$td %1$tT %1$tY", cal);
         }
         else
            repr = value.toString();

         if(repr.length() == 0)
            repr = "(null)";

         sb.append("   ")
           .append(name)
           .append(": ")
           .append(repr)
           .append("\n");
      }
      return sb.toString();
   }
}
