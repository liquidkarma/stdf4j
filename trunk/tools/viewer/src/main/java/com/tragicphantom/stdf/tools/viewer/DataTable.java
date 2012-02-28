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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Calendar;

import com.tragicphantom.stdf.Record;

public class DataTable extends JTable implements RecordSelectionListener,
                                                 MouseListener,
                                                 MouseMotionListener,
                                                 SettingsProvider,
                                                 PreferenceListener
{
   private DefaultTableModel tableModel = new DefaultTableModel();
   private TableSorter       sorter     = new TableSorter(tableModel);

   private int yTableDrag = 0;
   private int yTableRow  = 0;

   private boolean allFields = DefaultSettings.SHOW_ALL_FIELDS;

   public DataTable(){
      super();

      setModel(sorter);

      sorter.addMouseListenerToHeaderInTable(this);

      addMouseListener(this);
      addMouseMotionListener(this);
   }

   public void clear(){
      Vector data    = new Vector();
      Vector columns = new Vector();
      tableModel.setDataVector(data, columns);
   }

   public void mouseClicked(MouseEvent e){
   }

   public void mouseEntered(MouseEvent e){
   }

   public void mouseExited(MouseEvent e){
   }

   public void mousePressed(MouseEvent e){
      if((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK){
         yTableDrag = e.getY();
         yTableRow = rowAtPoint(e.getPoint());
         setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
      }
   }

   public void mouseReleased(MouseEvent e){
      yTableDrag = yTableRow = 0;
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   }

   public void mouseDragged(MouseEvent e){
      int y = e.getY();
      if(y != yTableDrag){
         int height = getRowHeight(yTableRow);
         setRowHeight(yTableRow, height - (yTableDrag - y));
         yTableDrag = y;
      }
   }

   public void mouseMoved(MouseEvent e){
   }

   public void setSelectedRecord(RecordTree tree, TreeRecord record){
      if(record.id < 0)
         clear();
      else{
         Object [][] data  = null;
         int         count = 0;

         if(record.getStdfRecord() != null){
            HashMap<String, Object> fields;
            try{
               fields = record.getStdfRecord().getData().getFields();
            }
            catch(Exception e){
               throw new RuntimeException(e);
            }

            TreeSet<String> fieldNames = new TreeSet(fields.keySet());

            data = new Object[fieldNames.size()][2];

            for(String fieldName : fieldNames){
               Object value = fields.get(fieldName);
               if(allFields || value != null){
                  String repr = null;
                  if(value != null){
                     if(fieldName.endsWith("_T")){
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis((Long)value * 1000L);
                        repr = String.format("%1$ta %1$tb %1$td %1$tT %1$tY", c);
                     }
                     else
                        repr = value.toString();
                  }

                  data[count][0] = fieldName;
                  data[count][1] = new DataCell(record, fieldName, repr);
                  count++;
               }
            }
         }

         if(!allFields && count > 0){
            Object [][] newData = new Object[count][2];
            for(int i = 0; i < count; i++){
               newData[i][0] = data[i][0];
               newData[i][1] = data[i][1];
            }

            data = newData;
         }

         String [] columnNames = {"Field Name", "Data"};
         tableModel.setDataVector(data, columnNames);

         TableColumn tableColumn = getColumnModel().getColumn(1);
         tableColumn.setCellEditor(new TextAreaEditor());
         tableColumn.setCellRenderer(new TextAreaRenderer());

         //buffer row height so that scroll bars don't always appear
         FontMetrics metrics = getFontMetrics(getFont());
         int height = metrics.getHeight() + metrics.getDescent();
         setRowHeight(2 * height);
      }
   }

   public void getSettings(){
      Settings.put("allFields", new Boolean(allFields));
   }

   public void setSettings(){
      allFields = ((Boolean)Settings.get("allFields", allFields)).booleanValue();
   }

   public void preferencesChanged(Preferences preferences){
      allFields = preferences.getAllFields();
   }
}
