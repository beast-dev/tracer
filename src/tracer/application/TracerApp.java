/*
 * TracerApp.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package tracer.application;

import dr.app.util.OSType;
import dr.inference.trace.LogFileTraces;
import jam.framework.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;

public class TracerApp extends MultiDocApplication {

    public TracerApp(String nameString, String aboutString, Icon icon,
                     String websiteURLString, String helpURLString) {
        super(new TracerMenuBarFactory(), nameString, aboutString, icon, websiteURLString, helpURLString);

        addPreferencesSection(new GeneralPreferencesSection());
    }

    private static boolean lafLoaded = false;

    // Main entry point
    static public void main(String[] args) {
        // There is a major issue with languages that use the comma as a decimal separator.
        // To ensure compatibility between programs in the package, enforce the US locale.
        Locale.setDefault(Locale.US);

        if (OSType.isMac()) {
            System.setProperty("swing.aatext", "true");

            System.setProperty("apple.awt.graphics.UseQuartz", "true");
            System.setProperty("apple.awt.antialiasing", "true");
            System.setProperty("apple.awt.rendering", "VALUE_RENDER_QUALITY");

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.draggableWindowBackground", "true");
            System.setProperty("apple.awt.showGrowBox", "true");

            try {
                // set the VAqua Look and Feel in the UIManager
                // This is a more modern L&F than the default Mac one
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                                System.out.println(info.getName() + " - " + info.getClassName());
                                // Tracer Issue #172 Mac OS X - com.apple.laf.AquaLookAndFeel
                                if (info.getName().startsWith("Mac")) {
                                    UIManager.setLookAndFeel(info.getClassName());
                                    lafLoaded = true;
                                    System.out.println("Set look & feel to : " + info.getName() + " - " + info.getClassName());
                                }
                            }
//                            UIManager.setLookAndFeel("org.violetlib.aqua.AquaLookAndFeel");
//                            lafLoaded = true;
                        } catch (Exception e) {
                            System.err.println("Failed to load AquaLookAndFeel");
                        }
                    }
                });

            } catch (Exception ignored) {
            }

            UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
            UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
        }

        if (!lafLoaded) {

            try {
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            java.net.URL url = TracerApp.class.getResource("images/Tracer.png");
            Icon icon = null;

            if (url != null) {
                icon = new ImageIcon(url);
            }

            final String nameString = "Tracer";
            final String versionString = "v1.7.3";
            String aboutString = "<html><font face=\"helvetica,san-serif\"><center><p>MCMC Trace Analysis Tool<br>" +
                    "Version " + versionString + ", 2003-2023</p>" +
                    "<p>by<br>" +

                    "Andrew Rambaut, Alexei J. Drummond, Walter Xie, Guy Baele, and Marc A. Suchard</p>" +

                    "<p>Institute of Evolutionary Biology, University of Edinburgh<br>" +
                    "<a href=\"mailto:a.rambaut@ed.ac.uk\">a.rambaut@ed.ac.uk</a></p>" +

                    "<p>Department of Computer Science, University of Auckland<br>" +
                    "<a href=\"mailto:alexei@cs.auckland.ac.nz\">alexei@cs.auckland.ac.nz</a></p>" +

                    "<p>Departments of Biomathematics, Biostatistics and Human Genetics, UCLA<br>" +
                    "<a href=\"mailto:msuchard@ucla.edu\">msuchard@ucla.edu</a></p>" +

                    "<p>Available from the BEAST site:<br>" +
                    "<a href=\"http://beast.community/tracer\">http://beast.community/</a></p>" +
                    "<p>Source code distributed under the GNU LGPL:<br>" +
                    "<a href=\"http://github.com/beast-dev/tracer/\">http://github.com/beast-dev/tracer/</a></p>" +
                    "<p>Thanks for contributions from: Joseph Heled, Oliver Pybus & Benjamin Redelings</p>" +
                    "</center></font></html>";

            String websiteURLString = "http://beast.community/";
            String helpURLString = "http://beast.community/Tracer";

            TracerApp app = new TracerApp(nameString, aboutString, icon, websiteURLString, helpURLString);
            app.setDocumentFrameFactory(new DocumentFrameFactory() {
                public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
                    return new TracerFrame("Tracer");
                }
            });
            app.initialize();

            app.doNew();

            if (args.length > 0) {
                TracerFrame frame = (TracerFrame) app.getDefaultFrame();
                for (String fileName : args) {

                    File file = new File(fileName);
                    LogFileTraces[] traces = {new LogFileTraces(fileName, file)};

                    frame.processTraces(traces);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Fatal exception: " + e,
                    "Please report this to the authors",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
