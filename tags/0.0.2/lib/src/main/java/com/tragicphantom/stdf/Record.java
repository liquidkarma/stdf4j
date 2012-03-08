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

import java.nio.ByteOrder;

import java.text.ParseException;

public class Record{
   private RecordDescriptor desc;
   private int              pos;
   private byte []          data;
   private ByteOrder        byteOrder;
   private RecordData       rd;

   public Record(RecordDescriptor desc, int pos,
                 byte [] data, ByteOrder byteOrder){
      this.desc      = desc;
      this.pos       = pos;
      this.data      = data;
      this.byteOrder = byteOrder;
      this.rd        = null;
   }

   public Record(RecordDescriptor desc, RecordData rd){
      this.desc = desc;
      this.rd   = rd;
      this.pos  = -1;
   }

   public String getType(){
      return desc.getType();
   }

   public int getPosition(){
      return pos;
   }

   public RecordData getData() throws ParseException{
      if(rd == null)
         rd = desc.parse(pos, data, byteOrder);
      return rd;
   }

   public String toString(){
      try{
         return getData().toString();
      }
      catch(Exception e){
         return "(null)";
      }
   }
}
