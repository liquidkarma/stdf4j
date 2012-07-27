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

import java.util.Arrays;

import java.nio.ByteOrder;

import java.text.ParseException;

import com.tragicphantom.stdf.util.ByteArray;

public class RecordDataParser{
   private RecordDescriptor desc;
   private int              pos;
   private byte[]           data;
   private ByteArray        byteArray;
   private int              available;
   private int              offset;

   public RecordDataParser(RecordDescriptor desc, int pos, byte [] data,
                           ByteOrder byteOrder){
      this.desc      = desc;
      this.pos       = pos;
      this.data      = data;
      this.byteArray = new ByteArray(byteOrder);
      this.available = data.length;
      this.offset    = 0;
   }

   public RecordData parse() throws ParseException{
      Object[]         fieldList = new Object[desc.size()];
      int              index     = 0;

      for(Field field : desc.getFields()){
         Object value = null;
         if(available > 0 && validField(field, fieldList)){
            //System.err.println(field.getName() + " => " + field.getType() + " => " + field.getLength());
            value = readField(field.getType(),
                              field.getLength(),
                              field.getLengthFieldIndex(),
                              field.getArrayType(),
                              field.getArraySizeFieldIndex(),
                              fieldList);
         }
         else{
            char type = field.getType();
            if(type == 'U' || type == 'I')
               value = Integer.valueOf(0);
            else if(type == 'R')
               value = Double.valueOf(0.0);
            else if(type == 'k')
               value = readArray(field.getArrayType(),
                                 field.getArraySizeFieldIndex(),
                                 field.getLengthFieldIndex(),
                                 field.getLength(),
                                 fieldList, true);
            else if(type == 'B' && field.getLength() == 1)
               value = Byte.valueOf((byte)0);
         }

         //System.err.println("(" + available + ") " + field.getName() + "[" + field.getType() + "] = " + value);

         fieldList[index] = value;
         index++;
      }

      return new RecordData(desc, fieldList);
   }

   protected boolean validField(Field field, Object[] values){
      boolean valid = true;
      if(field.getFlagIndex() >= 0 && field.getFlagIndex() <= values.length){
         byte flag = (Byte)values[field.getFlagIndex()];
         valid = field.isValid(flag);
      }
      return valid;
   }

   protected Object readField(char type, int length, int lengthFieldIndex,
                              char arrayType,
                              int arraySizeFieldIndex,
                              Object[] fields) throws ParseException{
      switch(type){
         case 'U':
            if(lengthFieldIndex >= 0)
               return readUnsigned((int)getFieldSize(fields[lengthFieldIndex]));
            else
               return readUnsigned(length);
         case 'I':
            if(lengthFieldIndex >= 0)
               return readSigned((int)getFieldSize(fields[lengthFieldIndex]));
            else
               return readSigned(length);
         case 'B':
         case 'N':
            if(length < 0)
               return getBytes(readUnsignedInt(1));
            else{
               if(length == 1)
                  return getBytes(1)[0];
               else
                  return getBytes(length);
            }
         case 'D':
            return readBits(length);
         case 'C':
         case 'S':
            if(length >= 0)
               return readString(length);
            else if(lengthFieldIndex >= 0)
               return readString((int)getFieldSize(fields[lengthFieldIndex]));
            else if(type == 'S')
               return readString(readUnsignedInt(2));
            else // type == 'C'
               return readString(readUnsignedInt(1));
         case 'R':
            if(length == 4)
               return new Float(byteArray.toFloat(getBytes(4)));
            else
               return new Double(byteArray.toDouble(getBytes(8)));
         case 'V':
            return readVariableTypeList();
         case 'k':
            return readArray(arrayType, arraySizeFieldIndex, lengthFieldIndex,
                             length, fields, false);
      }

      throw new ParseException("Invalid type code: " + type, pos);
   }

   protected Object[] readVariableTypeList() throws ParseException{
      int length = readUnsignedInt(2);
      Object[] list = new Object[length];
      for(int i = 0; i < length; i++){
         byte type = getBytes(1)[0];
         switch(type){
            case 0:
               // just padding, no need to read anything
               break;
            case 1:
               list[i] = readUnsignedInt(1);
               break;
            case 2:
               list[i] = readUnsignedInt(2);
               break;
            case 3:
               list[i] = readUnsigned(4);
               break;
            case 4:
               list[i] = readSigned(1);
               break;
            case 5:
               list[i] = readSigned(2);
               break;
            case 6:
               list[i] = readSigned(4);
               break;
            case 7:
               list[i] = readField('R', 4, -1, ' ', -1, null);
               break;
            case 8:
               list[i] = readField('R', 8, -1, ' ', -1, null);
               break;
            case 10:
               list[i] = readField('C', -1, -1, ' ', -1, null);
               break;
            case 11:
               list[i] = readField('B', -1, -1, ' ', -1, null);
               break;
            case 12:
               list[i] = readField('D', -1, -1, ' ', -1, null);
               break;
            case 13:
               list[i] = readField('U', 1, -1, ' ', -1, null);
               break;
            default:
               // we may have prematurely found the end of the array
               // assume any remaining bytes belong to the next record
               // this works around a bug with some test programs writing
               // invalid GDR records
               available = 0;
               break;
         }
         //System.err.print("read [" + (i + 1) + "/" + length + " => " + type + "]: ");
         //System.err.println(list[i] != null ? list[i].toString() : "<null>");
      }

      return list;
   }

   protected Object[] readArray(char type, int fieldIndex,
                                int lengthFieldIndex,
                                int length, Object[] fields,
                                boolean fillNull)
                               throws ParseException{
      long count = getFieldSize(fields[fieldIndex]);

      Object[] array = new Object[(int)count];

      if(!fillNull){
         if(type == 'N' && length == 1){
            long byteCount = count / 2 + count % 2;
            long index     = 0;
            for(long i = 0; i < byteCount; i++){
               int value = readUnsignedInt(1);
               array[(int)index] = (value & 0xF0) >> 4;
               if(++index < (count - 1)){
                  array[(int)index] = value & 0x0F;
                  index++;
               }
            }
         }
         else{
            if(lengthFieldIndex >= 0)
               length = (int)getFieldSize(fields[lengthFieldIndex]);

            for(long i = 0; i < count; i++)
               array[(int)i] = readField(type, length, -1, ' ', -1, null);
         }
      }

      return array;
   }

   protected long getFieldSize(Object field){
      long size = 0;
      if(field instanceof Long)
         size = (Long)field;
      else
         size = (long)(int)(Integer)field;
      return size;
   }

   protected String readString(int length){
       /**
       * The following is a trick from Trevor Pounds:
       * The following byte[] to char[] conversion is much faster than the
       * internal character encoding logic used by java.lang.String which
       * is slow due to several extraneous method calls and object alloction
       * to support charset detection and encoding. Since we only care about
       * UTF-8 we can bypass all of the slowness with a custom conversion loop.
       * FWIW, hotspot may optimize the generalized UTF-8 java.lang.String case
       * but from my testing this custom logic outperforms the internal charset
       * encoding algorithm (i.e. -agentlib:hprof=cpu=times).
       */
      final byte[] bbuf = getBytes(length);

      length = bbuf.length; // adjust according to how much data actually read

      char [] cbuf = new char[length];
      for(int i = 0; i < length; i++)
         cbuf[i] = (char) (0xFF & bbuf[i]);

      return new String(cbuf, 0, length).intern();
   }

   protected int readSigned(int length){
      return byteArray.toSigned(getBytes(length), length);
   }

   protected long readUnsigned(int length){
      return byteArray.toUnsigned(getBytes(length), length);
   }

   protected int readUnsignedInt(int length){
      return byteArray.toUnsignedInt(getBytes(length), length);
   }

   protected byte[] readBits(int length){
      int numBits = 0;
      if(length < 0)
         numBits = readUnsignedInt(2);
      else
         numBits = length;

      length = numBits / 8;
      if((numBits % 8) > 0)
         length++;

      return getBytes(length);
   }

   protected byte[] getBytes(int numBytes){
      available -= numBytes;
      if(available < 0){
         numBytes += available;
         available = 0;
      }

      byte[] bytes = Arrays.copyOfRange(data, offset, offset + numBytes);
      offset += numBytes;

      return bytes;
   }
}
