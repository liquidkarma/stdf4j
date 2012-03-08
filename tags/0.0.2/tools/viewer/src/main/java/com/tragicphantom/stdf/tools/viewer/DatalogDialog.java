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

import java.awt.*;
import java.awt.event.*;

import java.util.List;

import com.tragicphantom.stdf.Record;

public class DatalogDialog extends JDialog implements ActionListener{
   private JTextArea   textArea    = new JTextArea();
   private JScrollPane jScrollPane = new JScrollPane(textArea);

   private JButton     closeButton = new JButton("Close");

   private int width  = 400;
   private int height = 500;

   public DatalogDialog(Frame dialogOwner){
      super(dialogOwner, "Datalog", false);

      setSize(new Dimension(width, height));

      getContentPane().add(jScrollPane, BorderLayout.CENTER);

      JPanel panel = new JPanel();
      closeButton.addActionListener(this);
      panel.add(closeButton);
      getContentPane().add(panel, BorderLayout.SOUTH);
   }

   public void show(){
      addDatalogData();
      super.show();
   }

   private void addDatalogData(){
      StringBuilder sb = new StringBuilder();

      List<Record> records = FileLoader.getAllSTDFRecordsOfType("DTR");
      for(Record record : records){
         try{
            Object textObj = record.getData().getField("TEXT_DAT");
            sb.append(textObj == null ? "" : textObj.toString())
              .append("\n");
         }
         catch(Exception e){
            // ignore
         }
      }

      textArea.setText(sb.toString());
   }

   public void actionPerformed(ActionEvent e){
      Object source = e.getSource();
      if(source.equals(closeButton))
         hide();
   }
}

// vim:ts=3:et:sw=3
