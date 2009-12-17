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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.zip.GZIPOutputStream;

/**
 * STDFWriter
 * Based on pystdf <http://code.google.com/p/pystdf/>
 * And libstdf <http://freestdf.sourceforge.net/>
 */
public class STDFWriter{
   private static final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

   private OutputStream stream = null;

   public STDFWriter(String fileName) throws IOException{
      this(new File(fileName));
   }

   public STDFWriter(String fileName, boolean compressed) throws IOException{
      this(new File(fileName), compressed);
   }

   public STDFWriter(File file) throws IOException{
      String  name       = file.getName();
      boolean compressed = false;

      if(name.endsWith(".z") || name.endsWith(".gz"))
         compressed = true;

      open(file, compressed);
   }

   public STDFWriter(File file, boolean compressed) throws IOException{
      open(file, compressed);
   }

   public STDFWriter(OutputStream stream){
      this.stream = stream;
   }

   protected void open(File file, boolean compressed) throws IOException{
      if(compressed)
         stream = new GZIPOutputStream(new FileOutputStream(file));
      else
         stream = new FileOutputStream(file);
   }

   public void close() throws IOException{
      if(stream != null){
         stream.close();
         stream = null;
      }
      else
         throw new NullPointerException();
   }

   public void write(Iterable<Record> container) throws IOException{
      // default to v4 types if none specified
      write(container, com.tragicphantom.stdf.v4.Types.getRecordDescriptors());
   }

   public void write(Iterable<Record> container,
                     Map<RecordType, RecordDescriptor> records) throws IOException{
      Map<String, RecordDescriptor> descs = typeNames(records);

      for(Record record : container){
         RecordDescriptor desc = descs.get(record.getType());

         int available = getLength(record, desc);
         writeHeader(available, desc.getRecordType());

         ArrayList<Object> fieldValues = new ArrayList<Object>();
         for(Field field : desc.getFields())
            fieldValues.add(record.getField(field.getName()));

         for(Field field : desc.getFields()){
            String fieldName = field.getName();
            if(record.hasField(fieldName)){
               available -= writeField(field.getType(), fieldValues, record.getField(fieldName));
               if(available <= 0)
                  break;
            }
         }
      }
   }

   protected Map<String, RecordDescriptor> typeNames(Map<RecordType, RecordDescriptor> records){
      HashMap<String, RecordDescriptor> descs = new HashMap<String, RecordDescriptor>();
      for(RecordDescriptor desc : records.values())
         descs.put(desc.getType(), desc);
      return descs;
   }

   protected String getArrayType(String typeCode){
      int pos    = 1;
      int length = typeCode.length();
      for(; pos < length; pos++){
         char c = typeCode.charAt(pos);
         if(c >= 'A' && c <= 'Z')
            break;
      }
      return typeCode.substring(pos);
   }

   protected int getFieldLength(String typeCode, Object value) throws IOException{
      int fieldLength = 0;

      char   type   = typeCode.charAt(0);
      String length = typeCode.substring(1);

      if(type == 'U' || type == 'I'){
/*
         boolean valid = true;
         if(value instanceof Long){
            long x = (Long)value;
            if(x == 0)
               valid = false;
         }
         else if(value instanceof Integer){
            int x = (Integer)value;
            if(x == 0)
               valid = false;
         }
         else
            throw new IOException("Invalid numeric type: " + value.getClass().getName());

         if(valid)
*/
            fieldLength += Integer.parseInt(length);
      }
      else if(type == 'R'){
/*
         if(value instanceof Double){
            double x = (Double)value;
            if(x != 0.0)
               fieldLength += Integer.parseInt(length);
         }
         else
*/
            fieldLength += Integer.parseInt(length);
      }
      else if(typeCode.equals("B1")){
/*
         byte x = (Byte)value;
         if(x != (byte)0)
*/
            fieldLength++;
      }
      else if(type == 'B' || type == 'N' || type == 'D'){
         if(value instanceof Integer)
            fieldLength += 4;
         else if(length.equals("n"))
            fieldLength += 1 + ((byte[])value).length;
         else
            fieldLength += Integer.parseInt(length);
      }
      else if(type == 'C'){
         if(length.equals("n"))
            fieldLength += 1 + ((String)value).length();
         else
            fieldLength += Integer.parseInt(length);
      }
      else if(type == 'k'){
         String arrayType = getArrayType(typeCode);
         ArrayList<Object> x = (ArrayList<Object>)value;
         for(Object o : x){
            if(o != null)
               fieldLength += getFieldLength(arrayType, o);
         }
      }
      else if(type == 'V'){
         fieldLength += 2;
         ArrayList<Object> x = (ArrayList<Object>)value;
         for(Object o : x){
            if(o != null){
               if(o instanceof Integer)
                  fieldLength += 2;
               else if(o instanceof Long)
                  fieldLength += 4;
               else if(o instanceof String)
                  fieldLength += ((String)o).length();
               else if(o instanceof Byte)
                  fieldLength++;
               else if(o instanceof Float)
                  fieldLength += 4;
               else if(o instanceof Double)
                  fieldLength += 8;
            }
         }
      }

      return fieldLength;
   }

   protected int getLength(Record record, RecordDescriptor desc) throws IOException{
      int length = 0;
      for(Field field : desc.getFields()){
         String fieldName = field.getName();
         if(record.hasField(fieldName)){
            Object value = record.getField(fieldName);
            if(value != null)
               length += getFieldLength(field.getType(), value);
         }
      }
      return length;
   }

   private void writeHeader(int length, RecordType rt) throws IOException{
      writeUnsigned(length         , 2);
      writeUnsigned(rt.getType()   , 1);
      writeUnsigned(rt.getSubType(), 1);
   }

   protected int writeField(String typeCode, ArrayList<Object> fields, Object value) throws IOException{
      assert typeCode.length() >= 2: "Invalid type code: " + typeCode;

      char   type   = typeCode.charAt(0);
      String length = typeCode.substring(1);

      switch(type){
         case 'U':
            if(value instanceof Integer)
               return writeUnsigned(((Integer)value).longValue(), Integer.parseInt(length));
            else
               return writeUnsigned((Long)value, Integer.parseInt(length));
         case 'I':
            return writeSigned((Integer)value, Integer.parseInt(length));
         case 'B':
         case 'N':
            {
               byte [] bytes;

               if(value instanceof Byte)
                  bytes = new byte[]{ (Byte)value };
               else if(value instanceof Integer)
                  bytes = new byte[]{ (byte)(int)(Integer)value };
               else
                  bytes = (byte[])value;

               int extra = 0;
               int count;
               if(length.equals("n")){
                  count = bytes.length;
                  writeUnsigned(count, 1);
                  extra = 1;
               }
               else
                  count = Integer.parseInt(length);

               return writeBytes(bytes, count) + extra;
            }
         case 'D':
            return writeBits((byte[])value, length);
         case 'C':
            return writeString((String)value, length);
         case 'R':
            {
               int count = Integer.parseInt(length);
               ByteBuffer buffer = ByteBuffer.allocate(count).order(byteOrder);
               if(count == 4){
                  if(value instanceof Double)
                     buffer.putFloat(((Double)value).floatValue());
                  else
                     buffer.putFloat((Float)value);
               }
               else
                  buffer.putDouble((Double)value);
               return writeBytes(buffer.array(), count);
            }
         case 'V':
            return writeVariableTypeList(value);
         case 'k':
            return writeArray(typeCode, fields, value);
      }

      throw new IOException("Invalid type code: " + typeCode);
   }

   protected int writeSigned(int value, int numBits) throws IOException{
      ByteBuffer buffer = ByteBuffer.allocate(numBits).order(byteOrder);
      switch(numBits){
         case 1:
            buffer.put((byte)value);
            break;
         case 2:
            buffer.putShort((short)value);
            break;
         case 4:
            buffer.putInt(value);
            break;
         default:
            throw new IOException("Invalid number of bits for signed write: " + numBits);
      }

      stream.write(buffer.array());
      return numBits;
   }

   protected int writeUnsigned(long value, int numBits) throws IOException{
      ByteBuffer buffer = ByteBuffer.allocate(numBits).order(byteOrder);
      switch(numBits){
         case 1:
            buffer.put((byte)value);
            break;
         case 2:
            buffer.putShort((short)value);
            break;
         case 4:
            buffer.putInt((int)value);
            break;
         case 8:
            buffer.putLong(value);
         default:
            throw new IOException("Invalid number of bits for unsigned write: " + numBits);
      }

      stream.write(buffer.array());
      return numBits;
   }

   protected int writeBits(byte [] bytes, String lengthCode) throws IOException{
      int numBits = 0;
      int extra   = 0;
      if(lengthCode.equals("n")){
         if(bytes == null)
            bytes = new byte[]{};

         numBits = bytes.length;
         writeUnsigned(numBits, 2);
         extra = 2;
      }
      else
         numBits = Integer.parseInt(lengthCode);

      int length = numBits / 8;
      if((numBits % 8) > 0)
         length++;

      return writeBytes(bytes, length) + extra;
   }

   protected int writeBytes(byte [] bytes, int length) throws IOException{
      stream.write(bytes, 0, length);
      return length;
   }

   protected int writeString(String value, String length) throws IOException{
      int extra = 0;
      int count;
      if(length.equals("n")){
         if(value == null)
            value = "";

         count = value.length();
         writeUnsigned(count, 1);
         extra = 1;
      }
      else
         count = Integer.parseInt(length);

      return writeBytes(value.getBytes(), count) + extra;
   }

   protected int writeVariableTypeList(Object value) throws IOException{
      ArrayList<Object> list = (ArrayList<Object>)value;
      writeUnsigned(list.size(), 2);
      int length = 2;
      for(Object o : list){
         if(o == null)
            throw new IOException("Null value in variable type list");
         else{
            if(o instanceof Integer)
               length += writeSigned((Integer)o, 2);
            else if(o instanceof Long)
               length += writeUnsigned((Long)o, 4);
            else if(o instanceof String)
               length += writeString((String)o, "n");
            else if(o instanceof Byte)
               length += writeBytes(new byte[]{ (Byte)o }, 1);
            else if(o instanceof Float)
               length += writeField("R4", null, o);
            else if(o instanceof Double)
               length += writeField("R8", null, o);
            else
               throw new IOException("Unknown variable type: " + o.getClass().getName());
         }
      }

      return length;
   }

   protected int writeArray(String typeCode, ArrayList<Object> fields, Object value) throws IOException{
      // not using a regex here since this seems like it should be faster
      // may change if necessary
      int pos    = 1;
      int length = typeCode.length();
      for(; pos < length; pos++){
         char c = typeCode.charAt(pos);
         if(c >= 'A' && c <= 'Z')
            break;
      }

      int  fieldIndex = Integer.parseInt(typeCode.substring(1, pos));
      long count      = (Long)fields.get(fieldIndex);

      typeCode = typeCode.substring(pos);

      ArrayList<Object> list = (ArrayList<Object>)value;

      length = 0;

      for(int i = 0; i < count; i++)
         length += writeField(typeCode, fields, list.get(i));

      return length;
   }
}
