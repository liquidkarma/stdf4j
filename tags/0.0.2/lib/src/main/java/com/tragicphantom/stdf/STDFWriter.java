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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

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
         RecordData       data;

         try{
            data = record.getData();
         }
         catch(java.text.ParseException pe){
            throw new IOException("Unable to parse record data", pe);
         }

         int available = getLength(data, desc);
         writeHeader(available, desc.getRecordType());

         int size = desc.size();
         Object[] fieldValues = new Object[size];
         for(int i = 0; i < size; i++)
            fieldValues[i] = data.getField(i);

         for(Field field : desc.getFields()){
            String fieldName = field.getName();
            if(data.hasField(fieldName)){
               available -= writeField(field.getType(), field.getLength(),
                                       field.getArrayType(),
                                       field.getArraySizeFieldIndex(),
                                       fieldValues, data.getField(fieldName));

               if(available < 0){
                  //System.err.println(data);
                  throw new IOException("Ran out of available bytes writing " + record.getType() + "." + fieldName);
               }
               else if(available == 0)
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

   protected int getFieldLength(char type, int length,
                                char arrayType, Object value) throws IOException{
      int fieldLength = 0;

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
            fieldLength += length;
      }
      else if(type == 'R'){
/*
         if(value instanceof Double){
            double x = (Double)value;
            if(x != 0.0)
               fieldLength += length;
         }
         else
*/
            fieldLength += length;
      }
      else if(type == 'B' && length == 1){
/*
         byte x = (Byte)value;
         if(x != (byte)0)
*/
            fieldLength++;
      }
      else if(type == 'B' || type == 'N' || type == 'D'){
         if(value instanceof Integer)
            fieldLength += 4;
         else if(length < 0)
            fieldLength += 1 + ((byte[])value).length;
         else
            fieldLength += length;
      }
      else if(type == 'C'){
         if(length < 0)
            fieldLength += 1 + ((String)value).length();
         else
            fieldLength += length;
      }
      else if(type == 'k'){
         for(Object o : (Object[])value){
            if(o != null)
               fieldLength += getFieldLength(arrayType, length, ' ', o);
         }
      }
      else if(type == 'V'){
         fieldLength += 2;
         for(Object o : (Object[])value){
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

   protected int getLength(RecordData data,
                           RecordDescriptor desc) throws IOException{
      int length = 0;
      for(Field field : desc.getFields()){
         String fieldName = field.getName();
         if(data.hasField(fieldName)){
            Object value = data.getField(fieldName);
            if(value != null)
               length += getFieldLength(field.getType(), field.getLength(),
                                        field.getArrayType(), value);
            else if(field.getType() == 'C' && field.getLength() < 0)
               length++;
         }
      }
      return length;
   }

   private void writeHeader(int length, RecordType rt) throws IOException{
      writeUnsigned(length         , 2);
      writeUnsigned(rt.getType()   , 1);
      writeUnsigned(rt.getSubType(), 1);
   }

   protected int writeField(char type, int length,
                            char arrayType, int arraySizeFieldIndex,
                            Object[] fields, Object value) throws IOException{
      switch(type){
         case 'U':
            if(value instanceof Integer)
               return writeUnsigned(((Integer)value).longValue(), length);
            else
               return writeUnsigned((Long)value, length);
         case 'I':
            return writeSigned((Integer)value, length);
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
               if(length < 0){
                  count = bytes.length;
                  writeUnsigned(count, 1);
                  extra = 1;
               }
               else
                  count = length;

               return writeBytes(bytes, count) + extra;
            }
         case 'D':
            return writeBits((byte[])value, length);
         case 'C':
            return writeString((String)value, length);
         case 'R':
            {
               ByteBuffer buffer = ByteBuffer.allocate(length).order(byteOrder);
               if(length == 4){
                  if(value instanceof Double)
                     buffer.putFloat(((Double)value).floatValue());
                  else
                     buffer.putFloat((Float)value);
               }
               else
                  buffer.putDouble((Double)value);
               return writeBytes(buffer.array(), length);
            }
         case 'V':
            return writeVariableTypeList(value);
         case 'k':
            return writeArray(arrayType, length,
                              arraySizeFieldIndex, fields, value);
      }

      throw new IOException("Invalid type code: " + type);
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

   protected int writeBits(byte [] bytes, int length) throws IOException{
      int numBits = 0;
      int extra   = 0;
      if(length < 0){
         if(bytes == null)
            bytes = new byte[]{};

         numBits = bytes.length;
         writeUnsigned(numBits, 2);
         extra = 2;
      }
      else
         numBits = length;

      length = numBits / 8;
      if((numBits % 8) > 0)
         length++;

      return writeBytes(bytes, length) + extra;
   }

   protected int writeBytes(byte [] bytes, int length) throws IOException{
      stream.write(bytes, 0, length);
      return length;
   }

   protected int writeString(String value, int length) throws IOException{
      int extra = 0;
      int count;
      if(length < 0){
         if(value == null)
            value = "";

         count = value.length();
         writeUnsigned(count, 1);
         extra = 1;
      }
      else{
         count = length;

         if(value == null){
            char [] dummy = new char[count];
            Arrays.fill(dummy, ' ');
            value = new String(dummy);
         }
      }

      return writeBytes(value.getBytes(), count) + extra;
   }

   protected int writeVariableTypeList(Object value) throws IOException{
      Object [] list = (Object[])value;
      writeUnsigned(list.length, 2);
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
               length += writeString((String)o, -1);
            else if(o instanceof Byte)
               length += writeBytes(new byte[]{ (Byte)o }, 1);
            else if(o instanceof Float)
               length += writeField('R', 4, ' ', -1, null, o);
            else if(o instanceof Double)
               length += writeField('R', 8, ' ', -1, null, o);
            else
               throw new IOException("Unknown variable type: " + o.getClass().getName());
         }
      }

      return length;
   }

   protected int writeArray(char type, int length,
                            int fieldIndex, Object[] fields, Object value) throws IOException{
      Object[] list  = (Object[])value;
      long     count = (Long)fields[fieldIndex];
      int      size  = 0;

      for(Object x : list)
         size += writeField(type, length, ' ', -1, fields, x);

      return size;
   }
}
