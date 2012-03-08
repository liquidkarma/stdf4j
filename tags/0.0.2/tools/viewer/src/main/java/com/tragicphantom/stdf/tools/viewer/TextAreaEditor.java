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

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.HashSet;

import com.tragicphantom.stdf.RecordData;

class TextAreaEditor extends AbstractCellEditor implements TableCellEditor,
                                                           FocusListener,
                                                           MouseMotionListener{
   private JScrollPane scroll;
   private JTextArea   textArea  = new JTextArea();
   private DataCell    data      = null;
   private String      origValue = "";

   public TextAreaEditor(JTextArea text, int intColWidth, int intMaxScrollPane){
      scroll = new JScrollPane(text);
      textArea = text;
      scroll.setPreferredSize(new Dimension(intColWidth+10, intMaxScrollPane));
      setDefaults();
   }

   public TextAreaEditor(){
      scroll = new JScrollPane(textArea);
      setDefaults();

      textArea.addMouseMotionListener(this);
      textArea.addFocusListener(this);

      HashSet<AWTKeyStroke> focusKeys = new HashSet<AWTKeyStroke>();
      focusKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
      focusKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER, 0));
      textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, focusKeys);

/*
      KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      focusManager.addPropertyChangeListener(
         new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e){
               String prop = e.getPropertyName();
               if(("focusOwner".equals(prop)) && (e.getOldValue() instanceof JTextArea)){
                  JTextArea textArea = (JTextArea)e.getOldValue();
                  System.err.println("losing focus: " + textArea.getText());
               }
            }
         }
      );
*/
   }

   private void setDefaults(){
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      textArea.setLineWrap(false);
      textArea.setWrapStyleWord(true);
      textArea.setOpaque(true);
      textArea.setEditable(true);
   }

   public Component getTableCellEditorComponent(JTable table, Object _data,
                                                boolean isSelected, int row, int column){
      String value;
      if(_data instanceof DataCell){
         data  = (DataCell)_data;
         value = data.getValue();
      }
      else{
         data  = null;
         value = (String)_data;
      }

      origValue = (value == null) ? "" : value.toString();
      textArea.setText(origValue);
      return scroll;
   }

   public Object getCellEditorValue(){
      return data;
   }

   public void focusGained(FocusEvent e){
   }

   public void focusLost(FocusEvent e){
      if(data != null){
         //System.err.println("focus lost [" + data.getFieldName() + "]: " + textArea.getText() + " [" + origValue + "]");
         if(!textArea.getText().equals(origValue)){
            String fieldName = data.getFieldName();
            if((fieldName.equals("TEST_TXT") || fieldName.equals("TEXT_DAT"))
                  && data.getRecord().getStdfRecord() != null){
               RecordData rd;
               try{
                  rd = data.getRecord().getStdfRecord().getData();
               }
               catch(Exception ex){
                  throw new RuntimeException(ex);
               }
               rd.setField(fieldName, textArea.getText());
            }
            else
               textArea.setText(origValue);
         }
      }
      //else
      //   System.err.println("focus lost without data: " + textArea.getText() + " [" + origValue + "]");
   }

   public void mouseDragged(MouseEvent e){
   }

   public void mouseMoved(MouseEvent e){
      int x = e.getX();
      int y = e.getY();

      FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
      String text = textArea.getText();
      int length = text.length();

      if(length > 0){
         int index = length - 1;
         int position = 0;

         for(int i = 0; i < length; i++){
            position += metrics.charWidth(text.charAt(i));
            if(x <= position){
               index = i;
               break;
            }
         }

         String resolution = null;

         if(resolution == null){
            //setBubbleText(text.substring(index, index + 1));
            setBubbleText(Integer.toString(index));
         }
         else
            setBubbleText(resolution);
      }
   }

   private void setBubbleText(String text){
      textArea.setToolTipText(text);
   }
}
