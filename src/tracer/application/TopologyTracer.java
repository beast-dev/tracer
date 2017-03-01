/*
 * TopologyTracer.java
 *
 * Copyright (c) 2002-2017 Alexei Drummond, Andrew Rambaut and Marc Suchard
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

import dr.app.beast.BeastVersion;
import dr.evolution.io.Importer;
import dr.evolution.io.NewickImporter;
import dr.evolution.io.NexusImporter;
import dr.evolution.io.TreeImporter;
import dr.evolution.tree.Tree;
import dr.evolution.tree.TreeMetrics;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Guy Baele
 */
public class TopologyTracer {

    private static final String STATE = "state";
    private static final String RFDISTANCE = "RFdistance";

    public TopologyTracer(String treeFile, String outputFile) {

        try {

            BufferedReader reader = new BufferedReader(new FileReader(treeFile));

            String line = reader.readLine();

            TreeImporter importer;
            if (line.toUpperCase().startsWith("#NEXUS")) {
                importer = new NexusImporter(reader);
            } else {
                importer = new NewickImporter(reader);
            }

            //pick first tree as focal tree
            Tree focalTree = importer.importNextTree();

            ArrayList<Double> distances = new ArrayList<Double>();

            while (importer.hasTree()) {

                //no need to keep trees in memory
                Tree tree = importer.importNextTree();
                distances.add(TreeMetrics.getRobinsonFouldsDistance(focalTree, tree));

            }

            for (Double test : distances) {
                System.out.println(test);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BeastVersion version = new BeastVersion();
            writer.write("# BEAST " + version.getVersionString() + "\n");
            writer.write(STATE + "\t" + RFDISTANCE + "\n");

            int state = 0;
            for (Double test : distances) {
                writer.write(state + "\t" + test + "\n");
                state++;
            }

            writer.flush();
            writer.close();

        } catch (FileNotFoundException fnf) {
            System.err.println(fnf);
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (Importer.ImportException ime) {
            System.err.println(ime);
        }

    }

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new RuntimeException("Incorrect number of arguments: inputFile & outputFile required.");
        } else {
            TopologyTracer test = new TopologyTracer(args[0], args[1]);
        }

    }

}
