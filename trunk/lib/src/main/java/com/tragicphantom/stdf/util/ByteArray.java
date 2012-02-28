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
package com.tragicphantom.stdf.util;

import java.nio.ByteOrder;

import java.util.Arrays;

/**
 * Convenience class for working with byte arrays.
 * Most of the methods are adapted directly from
 * the openjdk source code.
 *
 * @author Trevor Pounds (trevor.pounds@gmail.com)
 */
public class ByteArray
{
   private ByteOrder byteOrder = ByteOrder.nativeOrder();

   public ByteArray(){
   }

   public ByteArray(ByteOrder byteOrder){
      setByteOrder(byteOrder);
   }

   public void setByteOrder(ByteOrder byteOrder){
      this.byteOrder = byteOrder;
   }

   public ByteOrder getByteOrder(){
      return byteOrder;
   }

   public final static boolean equals(final byte[] lhs, final byte[] rhs)
      { return Arrays.equals(lhs, rhs); }

   public final static int hashCode(final byte[] bytes)
      { return Arrays.hashCode(bytes); }

   public final char toChar(final byte[] bytes){
      if(byteOrder == ByteOrder.BIG_ENDIAN)
         return (char) ((bytes[1] & 0xFF) + ((bytes[0] & 0xFF) << 8));
      else
         return (char) ((bytes[0] & 0xFF) + ((bytes[1] & 0xFF) << 8));
   }

   public final double toDouble(final byte[] bytes)
      { return Double.longBitsToDouble(toLong(bytes)); }

   public final float toFloat(final byte[] bytes)
      { return Float.intBitsToFloat(toInt(bytes)); }

   public final int toInt(final byte[] bytes){
      if(bytes.length == 4){
         if(byteOrder == ByteOrder.BIG_ENDIAN)
            return (int) (((bytes[0] & 0xFF) << 24) + ((bytes[1] & 0xFF) << 16) +
                          ((bytes[2] & 0xFF) << 8)  +  (bytes[3] & 0xFF));
         else
            return (int) ( (bytes[0] & 0xFF)         + ((bytes[1] & 0xFF) << 8) +
                          ((bytes[2] & 0xFF) << 16)  + ((bytes[3] & 0xFF) << 24));
      }
      else
         return 0;
   }

   public int toSigned(final byte[] bytes, int length){
      int value = 0;

      if(length == 1 && bytes.length >= 1)
         value = (int)bytes[0];
      else if(length == 2 && bytes.length >= 2)
         value = (int)toShort(bytes);
      else
         value = toInt(bytes);

      return value;
   }

   // java does not support unsigned natively so try some trickery
   // to get type promotion without sign extension
   public long toUnsigned(final byte[] bytes, int length){
      long value = 0;

      if(length == 1 && bytes.length >= 1){
         byte b = bytes[0];
         value = b < 0 ? ((long)(b & 0x7F) | 0x80) : b;
      }
      else if(length == 2 && bytes.length >= 2){
         short s = toShort(bytes);
         value = s < 0 ? ((long)(s & 0x7FFF) | 0x8000) : s;
      }
      else{
         int i = toInt(bytes);
         value = i < 0 ? ((long)(i & 0x7FFFFFFFL) | 0x80000000L) : i;
      }

      return value;
   }

   public int toUnsignedInt(final byte[] bytes, int length){
      int value = 0;

      if(length == 1 && bytes.length >= 1){
         byte b = bytes[0];
         value = b < 0 ? ((b & 0x7F) | 0x80) : b;
      }
      else if(length == 2 && bytes.length >= 2){
         short s = toShort(bytes);
         value = s < 0 ? ((s & 0x7FFF) | 0x8000) : s;
      }

      return value;
   }

   public final long toLong(final byte[] bytes){
      if(bytes.length == 8){
         if(byteOrder == ByteOrder.BIG_ENDIAN)
            return (long) (((bytes[0] & 0xFFL) << 56) + ((bytes[1] & 0xFFL) << 48) +
                           ((bytes[2] & 0xFFL) << 40) + ((bytes[3] & 0xFFL) << 32) +
                           ((bytes[4] & 0xFFL) << 24) + ((bytes[5] & 0xFFL) << 16) +
                           ((bytes[6] & 0xFFL) << 8)  +  (bytes[7] & 0xFFL));
         else
            return (long) ( (bytes[0] & 0xFFL)        + ((bytes[1] & 0xFFL) <<  8) +
                           ((bytes[2] & 0xFFL) << 16) + ((bytes[3] & 0xFFL) << 24) +
                           ((bytes[4] & 0xFFL) << 32) + ((bytes[5] & 0xFFL) << 40) +
                           ((bytes[6] & 0xFFL) << 48) + ((bytes[7] & 0xFFL) << 56));
      }
      else
         return 0;
   }

   public final short toShort(final byte[] bytes){
      if(bytes.length == 2){
         if(byteOrder == ByteOrder.BIG_ENDIAN)
            return (short) ((bytes[1] & 0xFF) + ((bytes[0] & 0xFF) << 8));
         else
            return (short) ((bytes[0] & 0xFF) + ((bytes[1] & 0xFF) << 8));
      }
      else
         return 0;
   }

   public final static String toString(final byte[] bytes){
      return new String(bytes);
   }
}
