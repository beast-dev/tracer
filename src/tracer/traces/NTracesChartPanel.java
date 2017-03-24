/*
 * DensityPanel.java
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

package tracer.traces;

import dr.app.gui.chart.DiscreteJChart;
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * A shared code for the panel that displays multi-traces in one plot.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Walter Xie
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public abstract class NTracesChartPanel extends TraceChartPanel {

    public NTracesChartPanel(final JFrame frame) {
        super(frame);
    }

    //++++++ setup traces +++++++
    protected TraceList[] traceLists = null;
    protected List<String> traceNames = null;

    public abstract void setTraces(TraceList[] traceLists, List<String> traceNames);

//    protected Settings initTraces(TraceList[] traceLists, List<String> traceNames) {
//        this.traceLists = traceLists;
//        this.traceNames = traceNames;
//
//        if (traceNames.size() > 0) {
//            // find the first settings for the one of the selected traces...
//            Settings settings = null;
//
//            for (String name : traceNames) {
//                settings = settingsMap.get(name);
//                if (settings != null) {
//                    break;
//                }
//            }
//            if (settings == null) {
//                // if none of the traces have settings yet, create and store one for the
//                // first selected trace
//                settings = new Settings();
//                settingsMap.put(traceNames.get(0), settings);
//            }
//            return settings;
//        }
//        return null;
//    }

    //++++++ setup chart +++++++

    /**
     * If no traces selected, return false, else return true.
     * Usage: <code>if (!rmAllPlots()) return;</code>
     *
     * @return boolean
     */
    protected boolean rmAllPlots() {
        traceChart.removeAllPlots();

        if (traceLists == null || traceNames == null || traceNames.size() == 0) {
            chartPanel.setXAxisTitle("");
            chartPanel.setYAxisTitle("");
            messageLabel.setText("No traces selected");
            add(messageLabel, BorderLayout.NORTH);
            return false;
        }

        remove(messageLabel);
        return true;
    }

    // not working, difficult to extract
//    protected void setColours(final Settings currentSettings) {
//        int i = 0;
//        for (TraceList tl : traceLists) {
//            for (String traceName : traceNames) {
//                int traceIndex = tl.getTraceIndex(traceName);
//                Trace trace = tl.getTrace(traceIndex);
//
//                if (trace != null) {
//                    // colourBy
//                    if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE || currentSettings.colourBy == ColourByOptions.COLOUR_BY_ALL) {
//                        i++;
//                    }
//                    if (i == paints.length) i = 0;
//                }
//            }
//            if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE) {
//                i++;
//            } else if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE) {
//                i = 0;
//            }
//            if (i == paints.length) i = 0;
//        }
//    }

    protected void setXAxis(TraceType traceType, Map<Integer, String> categoryDataMap) {
        if (! (traceChart instanceof DiscreteJChart) )
            throw new RuntimeException("traceChart has to be instanceof DiscreteJChart, " +
                    "using setXAxis(TraceType traceType, Map<Integer, String> categoryDataMap) !");

        if (traceType == TraceType.REAL) {
            ((DiscreteJChart) traceChart).setXAxis(false, categoryDataMap);

        } else if (traceType == TraceType.ORDINAL || traceType == TraceType.BINARY) {
            ((DiscreteJChart) traceChart).setXAxis(true, categoryDataMap);

        } else if (traceType == TraceType.CATEGORICAL) {
            // categoryDataMap has to be filled in before here using getIndexOfCategoricalValues
            ((DiscreteJChart) traceChart).setXAxis(false, categoryDataMap);

        } else {
            throw new RuntimeException("Trace type is not recognized: " + traceType);
        }
    }

    protected void setYLab(TraceType traceType, String[] yLabs) {
        if (yLabs.length !=2)
            throw new IllegalArgumentException("Y labs array must have 2 element !");

        if (traceType == TraceType.REAL) {
            chartPanel.setYAxisTitle(yLabs[0]);

        } else if (traceType == TraceType.ORDINAL || traceType == TraceType.BINARY) {
            chartPanel.setYAxisTitle(yLabs[1]);

        } else if (traceType == TraceType.CATEGORICAL) {
            chartPanel.setYAxisTitle(yLabs[1]);

        } else {
            throw new RuntimeException("Trace type is not recognized: " + traceType);
        }
    }

    protected void setXLab() {
        if (traceLists.length == 1) {
            chartPanel.setXAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            chartPanel.setXAxisTitle(traceNames.get(0));
        } else {
            chartPanel.setXAxisTitle("Multiple Traces");
        }
    }

}
