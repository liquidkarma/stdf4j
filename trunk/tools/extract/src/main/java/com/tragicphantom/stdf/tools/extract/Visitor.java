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
package com.tragicphantom.stdf.tools.extract;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import java.text.ParseException;

import com.tragicphantom.stdf.STDFReader;
import com.tragicphantom.stdf.Record;
import com.tragicphantom.stdf.RecordVisitor;

public class Visitor implements RecordVisitor, Iterable<Record>{
   private ArrayList<Record> records = new ArrayList<Record>();
   private Set<String>       types   = null;

   public Visitor(String fileName, Set<String> types) throws FileNotFoundException,
                                                             IOException,
                                                             ParseException{
      this(new STDFReader(fileName), types);
   }

   public Visitor(STDFReader reader, Set<String> types) throws FileNotFoundException,
                                                               IOException,
                                                               ParseException{
      this.types = types;

      reader.parse(this);
   }

   public Iterator<Record> iterator(){
      return records.iterator();
   }

   public int size(){
      return records.size();
   }

   public void beforeFile(){
   }

   public void afterFile(){
   }

   public void handleRecord(Record record){
      if(types != null && types.contains(record.getType().toUpperCase()))
         records.add(record);
   }
}
