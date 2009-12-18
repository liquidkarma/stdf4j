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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Map;

import java.util.zip.GZIPInputStream;

import java.text.ParseException;

/**
 * STDFReader
 * Based on pystdf <http://code.google.com/p/pystdf/>
 * And libstdf <http://freestdf.sourceforge.net/>
 */
public class STDFReader{
   private InputStream stream     = null;
   private ByteOrder   byteOrder  = ByteOrder.nativeOrder();
   private int         available  = 0;
   private int         totalBytes = 0;

   public STDFReader(String fileName) throws FileNotFoundException, IOException{
      open(fileName);
   }

   public STDFReader(File file) throws FileNotFoundException, IOException{
      open(file);
   }

   public STDFReader(InputStream stream){
      this.stream = stream;
   }

   public void open(String fileName) throws FileNotFoundException, IOException{
      open(new File(fileName));
   }

   public void open(File file) throws IOException{
      try{
         stream = new GZIPInputStream(new FileInputStream(file));
      }
      catch(IOException e){
         if(e.getMessage().equals("Not in GZIP format"))
            stream = new FileInputStream(file);
         else
            throw e;
      }
   }

   public void parse(RecordVisitor visitor) throws FileNotFoundException,
                                                   IOException,
                                                   ParseException{
      // default to v4 types if none specified
      parse(visitor, com.tragicphantom.stdf.v4.Types.getRecordDescriptors());
   }

   public void parse(RecordVisitor visitor,
                     Map<RecordType, RecordDescriptor> records)
               throws FileNotFoundException,
                      IOException,
                      ParseException{
      if(stream == null)
         throw new FileNotFoundException();

      byteOrder = ByteOrder.nativeOrder();

      boolean checkFileOrder = true;

      try{
         // read until IOException
         while(true){
            Header header = readHeader();

            // first record in the file should be a FAR
            if(checkFileOrder){
               if(header.getType() != 0 && header.getSubType() != 10)
                  throw new ParseException("Invalid header sequence", 0);
               checkFileOrder = false;
            }

            available = header.getLength();

            //System.err.println(header.getType() + ", " + header.getSubType() + ": " + available + " bytes");

            if(records.containsKey(header.getRecordType())){
               RecordDescriptor  desc      = records.get(header.getRecordType());
               Record            record    = new Record(desc.getType());
               ArrayList<Object> fieldList = new ArrayList<Object>();

               for(Field field : desc.getFields()){
                  Object value = null;
                  if(available > 0)
                     value = readField(field.getType(), fieldList);
                  else{
                     String typeCode = field.getType();
                     if(typeCode.length() >= 2){
                        char type = typeCode.charAt(0);
                        if(type == 'U' || type == 'I')
                           value = Integer.valueOf(0);
                        else if(type == 'R')
                           value = Double.valueOf(0.0);
                        else if(type == 'k')
                           value = readArray(typeCode, fieldList, true);
                        else if(typeCode.equals("B1"))
                           value = Byte.valueOf((byte)0);
                     }
                  }

                  //System.err.println("(" + available + ") " + field.getName() + "[" + field.getType() + "] = " + value);

                  fieldList.add(value);
                  record.setField(field.getName(), value);
               }

               if(desc.getType().equals("Far")){
                  long cpuType = (Long)record.getField("CPU_TYPE");
                  byteOrder = (cpuType == 1) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
               }

               visitor.handleRecord(record);
            }
            else
               throw new ParseException("Unknown record type found: " + header.getType() + ", " + header.getSubType(), totalBytes);
         }
      }
      catch(IOException e){
         // Ignore
      }
      finally{
         stream.close();
         stream = null;
      }
   }

   protected Header readHeader() throws IOException,
                                        ParseException{
      return new Header((int)readUnsigned(2),
                        (int)readUnsigned(1),
                        (int)readUnsigned(1));
   }

   protected Object readField(String typeCode,
                              ArrayList<Object> fields) throws IOException,
                                                               ParseException{
      assert typeCode.length() >= 2: "Invalid type code: " + typeCode;

      char   type   = typeCode.charAt(0);
      String length = typeCode.substring(1);

      switch(type){
         case 'U':
            return readUnsigned(Integer.parseInt(length));
         case 'I':
            return readSigned(Integer.parseInt(length));
         case 'B':
         case 'N':
            if(length.equals("n"))
               return getBytes((int)readUnsigned(1));
            else{
               int size = Integer.parseInt(length);
               if(size == 1)
                  return getBytes(1)[0];
               else
                  return getBytes(size);
            }
         case 'D':
            return readBits(length);
         case 'C':
            if(length.equals("n"))
               return new String(getBytes((int)readUnsigned(1))).intern();
            else
               return new String(getBytes(Integer.parseInt(length))).intern();
         case 'R':
            if(Integer.parseInt(length) == 4)
               return ByteBuffer.wrap(getBytes(4)).order(byteOrder).getFloat();
            else
               return ByteBuffer.wrap(getBytes(8)).order(byteOrder).getDouble();
         case 'V':
            return readVariableTypeList();
         case 'k':
            return readArray(typeCode, fields, false);
      }

      throw new ParseException("Invalid type code: " + typeCode, totalBytes);
   }

   protected int readSigned(int length) throws IOException, ParseException{
      ByteBuffer buffer = ByteBuffer.wrap(getBytes(length)).order(byteOrder);
      int        value  = 0;

      if(length == 1)
         value = (int)buffer.get();
      else if(length == 2)
         value = (int)buffer.getShort();
      else
         value = buffer.getInt();

      return value;
   }

   // java does not support unsigned natively so try some trickery
   // to get type promotion without sign extension
   protected long readUnsigned(int length) throws IOException, ParseException{
      ByteBuffer buffer = ByteBuffer.wrap(getBytes(length)).order(byteOrder);
      long       value  = 0;

      if(length == 1){
         byte b = buffer.get();
         value = b < 0 ? ((long)(b & 0x7F) | 0x80) : b;
      }
      else if(length == 2){
         short s = buffer.getShort();
         value = s < 0 ? ((long)(s & 0x7FFF) | 0x8000) : s;
      }
      else{
         int i = buffer.getInt();
         value = i < 0 ? ((long)(i & 0x7FFFFFFFL) | 0x80000000L) : i;
      }

      return value;
   }

   protected byte[] readBits(String lengthCode) throws IOException,
                                                       ParseException{
      int numBits = 0;
      if(lengthCode.equals("n"))
         numBits = (int)readUnsigned(2);
      else
         numBits = Integer.parseInt(lengthCode);

      int length = numBits / 8;
      if((numBits % 8) > 0)
         length++;

      return getBytes(length);
   }

   protected ArrayList<Object> readVariableTypeList() throws IOException,
                                                             ParseException{
      int length = (int)readUnsigned(2);
      ArrayList<Object> list = new ArrayList<Object>(length);
      for(int i = 0; i < length; i++){
         byte type = getBytes(1)[0];
         switch(type){
            case 0:
               // just padding, no need to read anything
               break;
            case 1:
               list.add(readUnsigned(1));
               break;
            case 2:
               list.add(readUnsigned(2));
               break;
            case 3:
               list.add(readUnsigned(4));
               break;
            case 4:
               list.add(readSigned(1));
               break;
            case 5:
               list.add(readSigned(2));
               break;
            case 6:
               list.add(readSigned(4));
               break;
            case 7:
               list.add(readField("R4", null));
               break;
            case 8:
               list.add(readField("R8", null));
               break;
            case 10:
               list.add(readField("Cn", null));
               break;
            case 11:
               list.add(readField("Bn", null));
               break;
            case 12:
               list.add(readField("Dn", null));
               break;
            case 13:
               list.add(readField("U1", null));
               break;
         }
      }

      return list;
   }

   protected ArrayList<Object> readArray(String typeCode,
                                         ArrayList<Object> fields,
                                         boolean fillNull)
                               throws IOException,
                                      ParseException{
      // not using a regex here since this seems like it should be faster
      // may change if necessary
      int pos    = 1;
      int length = typeCode.length();
      for(; pos < length; pos++){
         char c = typeCode.charAt(pos);
         if(c >= 'A' && c <= 'Z')
            break;
      }

      int    fieldIndex = Integer.parseInt(typeCode.substring(1, pos));
      Object countObj   = fields.get(fieldIndex);
      long   count      = 0;

      if(countObj instanceof Long)
         count = (Long)fields.get(fieldIndex);
      else
         count = (long)(int)(Integer)fields.get(fieldIndex);

      typeCode = typeCode.substring(pos);

      ArrayList<Object> array = new ArrayList<Object>((int)count);

      if(fillNull){
         for(long i = 0; i < count; i++)
            array.add(null);
      }
      else{
         if(typeCode.equals("N1")){
            long byteCount = count / 2 + count % 2;
            for(long i = 0; i < byteCount; i++){
               int value = (int)readUnsigned(1);
               array.add((value & 0xF0) >> 4);
               if(--count > 0){
                  array.add(value & 0x0F);
                  count--;
               }
            }
         }
         else{
            for(long i = 0; i < count; i++)
               array.add(readField(typeCode, null));
         }
      }

      return array;
   }

   protected byte[] getBytes(int numBytes) throws IOException, ParseException{
      available  -= numBytes;
      totalBytes += numBytes;

      byte [] bytes = new byte[numBytes];
      int actualBytes = 0;
      if((actualBytes = stream.read(bytes, 0, numBytes)) != numBytes){
         int offset = 0;
         while(actualBytes > 0){
            numBytes -= actualBytes;
            offset   += actualBytes;
            if((actualBytes = stream.read(bytes, offset, numBytes)) == numBytes)
               break;
         }

         if(actualBytes != numBytes)
            throw new IOException("Invalid number of bytes read (expected: " + numBytes + ", got: " + actualBytes + ")");
            //throw new ParseException("Invalid number of bytes read (expected: " + numBytes + ", got: " + actualBytes + ")", totalBytes);
      }

      return bytes;
   }

   protected class Header{
      private int        length;
      private RecordType type;

      public Header(int length, int type, int subType){
         this.length = length;
         this.type   = new RecordType(type, subType);
      }

      public int getLength(){
         return length;
      }

      public int getType(){
         return type.getType();
      }

      public int getSubType(){
         return type.getSubType();
      }

      public RecordType getRecordType(){
         return type;
      }
   }
}
