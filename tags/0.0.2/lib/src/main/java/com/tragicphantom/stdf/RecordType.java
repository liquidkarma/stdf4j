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

public class RecordType{
   private int type;
   private int subType;

   public RecordType(int type, int subType){
      set(type, subType);
   }

   public void set(int type, int subType){
      this.type    = type;
      this.subType = subType;
   }

   public int getType(){
      return type;
   }

   public int getSubType(){
      return subType;
   }

   @Override
   public boolean equals(Object other){
      if(this == other)
         return true;
      else if(other != null && other instanceof RecordType){
         RecordType otherType = (RecordType)other;

         return (type == otherType.getType() && subType == otherType.getSubType());
      }

      return false;
   }

   @Override
   public int hashCode(){
      return (type ^ subType);
   }
}
