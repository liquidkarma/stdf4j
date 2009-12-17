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

   public void handleRecord(Record record){
      if(types != null && types.contains(record.getType().toUpperCase()))
         records.add(record);
   }
}
