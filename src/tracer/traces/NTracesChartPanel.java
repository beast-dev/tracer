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

import dr.inference.trace.TraceList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A shared code for the panel that displays multi-traces in one plot.
 * * {@link #setupTraces() abstract setupTraces} makes sure
 * the actions implemented in children classes updating the chart.
 * @see tracer.traces.DensityPanel, @see tracer.traces.JointDensityPanel,
 * and @see tracer.traces.RawTracePanel.
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

    //++++++ setup chart +++++++
    /**
     * Setup trace(s) for chart, used by the actions implemented in children classes.
     */
    protected abstract void setupTraces();
    //    protected void setupTraces() {
//        if (!rmAllPlots()) return;
//
//        set...();
//
//        validate();
//        repaint();
//    }

    /**
     * If no traces selected, return false, else return true.
     * Usage: <code>if (!rmAllPlots()) return;</code>
     *
     * @return boolean
     */
    protected boolean rmAllPlots() {
        removeAllPlots();

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

    /**
     * to overwrite it to <code>removeAllTraces()</code> in <code>RawTracePanel</code>
     */
    protected void removeAllPlots() {
        getTraceChart().removeAllPlots();

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

    /**
     * set x labs using <code>setXAxisTitle</code> when x-axis allows multiple traces
     */
    protected void setXLabMultiTraces() {
        if (traceLists.length == 1) {
            chartPanel.setXAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            chartPanel.setXAxisTitle(traceNames.get(0));
        } else {
            chartPanel.setXAxisTitle("Multiple Traces");
        }
    }
    /**
     * set y labs using <code>setYAxisTitle</code> when y-axis allows multiple traces
     */
    protected void setYLabMultiTraces() {
        if (traceLists.length == 1) {
            chartPanel.setYAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            chartPanel.setYAxisTitle(traceNames.get(0));
        } else {
            chartPanel.setYAxisTitle("Multiple Traces");
        }
    }

}
