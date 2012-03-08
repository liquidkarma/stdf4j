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

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

class TextAreaRenderer extends JScrollPane implements TableCellRenderer{
   private JTextArea textArea = new JTextArea();

   public TextAreaRenderer(JTextArea text, int intColWidth, int intMaxScrollPane){
      super(text);
      textArea = text;
      setPreferredSize(new Dimension(intColWidth + 10, intMaxScrollPane));
      setDefaults();
   }

   public TextAreaRenderer(){
      super();
      super.setViewportView(textArea);
      setDefaults();
   }

   private void setDefaults(){
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      textArea.setLineWrap(false);
      textArea.setWrapStyleWord(true);
      textArea.setOpaque(true);
   }

   public Component getTableCellRendererComponent(JTable table, Object _data,
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int column){
      String value;
      if(_data instanceof DataCell)
         value = ((DataCell)_data).getValue();
      else
         value = (String)_data;

      textArea.setText((value == null) ? "" : value.toString());

      try{
         textArea.setCaretPosition(1);
      }
         catch(Exception expGen){
      }

      if(hasFocus)
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      else
         setBorder(new EmptyBorder(1, 1, 1, 1));

      return this;
   }
}
