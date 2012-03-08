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

public class DataCell{
   private TreeRecord record;
   private String     field;
   private String     value;

   public DataCell(TreeRecord _record, String _field, String _value){
      record = _record;
      field  = _field;
      value  = _value;
   }

   public TreeRecord getRecord(){
      return record;
   }

   public String getFieldName(){
      return field;
   }

   public String getValue(){
      return value;
   }
}
