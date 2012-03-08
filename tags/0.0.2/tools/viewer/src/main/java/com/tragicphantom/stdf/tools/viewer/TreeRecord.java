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

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.tragicphantom.stdf.Record;

public class TreeRecord implements Transferable, Serializable{
   public static final DataFlavor STDF_FLAVOR = new DataFlavor(TreeRecord.class, "STDF Record");

   private static DataFlavor flavors[] = {STDF_FLAVOR};

   private static long nextId = 0;

   public Record stdfRecord;
   public String typeName;
   public long   id;
   public int    fileId;

   public TreeRecord(){
      stdfRecord = null;
      typeName   = "";
      id         = -1;
      fileId     = -1;
   }

   public TreeRecord(String _typeName, int _fileId){
      stdfRecord = null;
      typeName   = _typeName;
      id         = -1;
      fileId     = _fileId;
   }

   public TreeRecord(Record _stdfRecord, String _typeName, int _fileId){
      stdfRecord = _stdfRecord;
      typeName   = _typeName;
      id         = nextId++;
      fileId     = _fileId;
   }

   public Record getStdfRecord(){
      return stdfRecord;
   }

   public String toString(){
      return typeName;
   }

   // --------- Transferable --------------

   public boolean isDataFlavorSupported(DataFlavor df){
      return df.equals(STDF_FLAVOR);
   }

   /** implements Transferable interface */
   public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException{
      if(df.equals(STDF_FLAVOR))
         return this;
      else
         throw new UnsupportedFlavorException(df);
   }

   /** implements Transferable interface */
   public DataFlavor[] getTransferDataFlavors(){
      return flavors;
   }

   // --------- Serializable --------------

   private void writeObject(ObjectOutputStream out) throws IOException{
      out.defaultWriteObject();
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
      in.defaultReadObject();
   }
}

// vim:ts=3:et:sw=3
