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

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import java.text.ParseException;

public class STDFContainer implements RecordVisitor, Iterable<Record>{
   private ArrayList<Record> records = new ArrayList<Record>();

   public STDFContainer(String fileName) throws FileNotFoundException,
                                                IOException,
                                                ParseException{
      this(new STDFReader(fileName));
   }

   public STDFContainer(InputStream is) throws FileNotFoundException,
                                               IOException,
                                               ParseException{
      this(new STDFReader(is));
   }

   public STDFContainer(STDFReader reader) throws FileNotFoundException,
                                                  IOException,
                                                  ParseException{
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
      records.add(record);
   }
}
