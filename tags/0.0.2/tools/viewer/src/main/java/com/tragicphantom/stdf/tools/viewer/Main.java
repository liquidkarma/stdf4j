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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class Main{
   public static final String DEFAULT_TITLE = "STDF Viewer";

   public static void main(String[] args){
      try{
         JFrame  mainFrame = new JFrame();
         Panel   panel     = new Panel();
         MenuBar menuBar   = new MenuBar(panel);

         mainFrame.setTitle(DEFAULT_TITLE);
         mainFrame.setJMenuBar(menuBar);
         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
         mainFrame.setIconImage((new ImageIcon(Main.class.getClassLoader().getResource("stdf.png"))).getImage());

         mainFrame.setVisible(true);

         panel.setArgs(args);
      }
      catch(Exception e){
         if(e.getMessage() != null && e.getMessage().indexOf("DISPLAY") >= 0){
            System.err.println("ERROR: Your display is not set correctly.");
            System.err.println("       Please check your DISPLAY environment variable.");
         }
         else
            e.printStackTrace();
      }
   }

   static{
      try{
         if(System.getProperties().getProperty("os.name").startsWith("Windows"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e){
      }
   }
}
