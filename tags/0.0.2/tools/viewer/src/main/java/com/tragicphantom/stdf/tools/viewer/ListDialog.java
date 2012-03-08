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
import javax.swing.table.*;

public abstract class ListDialog extends JDialog{
   protected JScrollPane jScrollPane = new JScrollPane();

   protected DefaultTableModel tableModel = new DefaultTableModel();
   protected TableSorter       sorter     = new TableSorter(tableModel);
   protected JTable            jTable     = new JTable(sorter);

   protected int width  = 400;
   protected int height = 500;

   public ListDialog(Frame dialogOwner, String title, int _width, int _height){
      super(dialogOwner, title, false);

      width  = _width;
      height = _height;

      jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      sorter.addMouseListenerToHeaderInTable(jTable);

      setSize(new Dimension(width, height));

      getContentPane().add(jScrollPane, BorderLayout.CENTER);
      jScrollPane.getViewport().add(jTable, null);
   }

   public void show(){
      setupList();
      super.show();
   }

   abstract protected void setupList();
}

// vim:ts=3:et:sw=3
