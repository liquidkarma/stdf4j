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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import java.nio.ByteOrder;

import java.text.ParseException;

public class RecordDescriptor{
   private String                   type;
   private RecordType               rt;
   private Field[]                  fields;
   private HashMap<String, Integer> indexes;

   public RecordDescriptor(String type, RecordType rt, ArrayList<Field> fields){
      this.type   = type;
      this.rt     = rt;

      this.fields  = new Field[fields.size()];
      this.indexes = new HashMap<String, Integer>();

      int index = 0;
      for(Field field : fields){
         this.fields[index] = field;
         indexes.put(field.getName(), index);
         index++;
      }
   }

   public String getType(){
      return type;
   }

   public RecordType getRecordType(){
      return rt;
   }

   public Field[] getFields(){
      return fields;
   }

   public Set<String> getFieldNames(){
      return indexes.keySet();
   }

   public int size(){
      return fields.length;
   }

   public int getIndex(String name){
      return indexes.get(name);
   }

   public boolean contains(String name){
      return indexes.containsKey(name);
   }

   public RecordData parse(int pos, byte [] bytes, ByteOrder byteOrder) throws ParseException{
      return new RecordDataParser(this, pos, bytes, byteOrder).parse();
   }
}
