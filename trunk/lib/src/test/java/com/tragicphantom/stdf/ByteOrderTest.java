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
import java.nio.ByteBuffer;

import org.junit.Test;
import org.junit.Assert;

import com.tragicphantom.stdf.util.ByteArray;

public class ByteOrderTest{
   @Test
   public void testLittleEndian(){
      verifyBytes(ByteOrder.LITTLE_ENDIAN);
   }

   @Test
   public void testBigEndian(){
      verifyBytes(ByteOrder.BIG_ENDIAN);
   }

   protected void verifyBytes(ByteOrder order){
      ByteArray ba = new ByteArray(order);

      byte [] b = ByteBuffer.allocate(4).order(order).putInt(123456).array();
      Assert.assertEquals(ByteBuffer.wrap(b).order(order).getInt()   , ba.toInt(b));

      b = ByteBuffer.allocate(8).order(order).putLong(123456789123L).array();
      Assert.assertEquals(ByteBuffer.wrap(b).order(order).getLong()  , ba.toLong(b));

      b = ByteBuffer.allocate(2).order(order).putShort((short)12).array();
      Assert.assertEquals(ByteBuffer.wrap(b).order(order).getShort() , ba.toShort(b));

      b = ByteBuffer.allocate(4).order(order).putFloat(47.334f).array();
      Assert.assertEquals(ByteBuffer.wrap(b).order(order).getFloat() , ba.toFloat(b), 0.0001);

      b = ByteBuffer.allocate(8).order(order).putDouble(78.2732274).array();
      Assert.assertEquals(ByteBuffer.wrap(b).order(order).getDouble(), ba.toDouble(b), 0.0001);
   }
}
