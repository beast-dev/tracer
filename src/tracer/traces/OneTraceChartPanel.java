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
import dr.inference.trace.TraceType;

import javax.swing.*;

/**
 * A shared code for the panel that displays a single trace in a plot.
 * {@link #setupTrace() abstract setupTrace} makes sure
 * the actions implemented in children classes updating the chart.
 * @see tracer.traces.FrequencyPanel
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Walter Xie
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public abstract class OneTraceChartPanel extends TraceChartPanel {

    public OneTraceChartPanel(final JFrame frame) {
        super(frame);
    }

    //++++++ setup traces +++++++
    protected TraceList traceList = null;
    protected String traceName = null;

    public abstract void setTrace(TraceList traceList, String traceName);

    /**
     * Find the <code>Settings</code> given a <code>traceName</code> in <code>settingsMap</code>.
     * If <code>traceName</code> not in <code>settingsMap</code>, then create a new <code>Settings</code>.
     *
     * @param traceList
     * @param traceName
//     * @param settingsMap
//     * @return <code>Settings</code>
     */
    protected void initSettings(TraceList traceList, String traceName) {//, Map<String, Settings> settingsMap) {
        this.traceList = traceList;
        this.traceName = traceName;

////        Settings settings = settingsMap.get(traceName);
////        if (settings == null) {
////            settings = new Settings();
////            settingsMap.put(traceName, settings);
////        }
//        return settings;
    }

    //++++++ setup chart +++++++
    protected abstract void setupTrace();

    /**
     * If no traces selected, return false, else return true.
     * Usage: <code>if (!rmAllPlots()) return;</code>
     *
     * @return boolean
     */
    protected boolean rmAllPlots() {
        getTraceChart().removeAllPlots();

        if (traceList == null || traceName == null) {
            chartPanel.setXAxisTitle("");
            chartPanel.setYAxisTitle("");
            return false;
        }
        return true;
    }

    protected void setXLab(int traceIndex) {
        chartPanel.setXAxisTitle(traceList.getTraceName(traceIndex));
    }

    // for Frequency panel only ?
    protected void setBinsComponents(TraceType traceType) {

        if (traceType.isContinuous()) {
            labelBins.setVisible(true);
            binsCombo.setVisible(true);
            showValuesCheckBox.setVisible(false);

        } else if (traceType.isIntegerOrBinary()) {
            labelBins.setVisible(false);
            binsCombo.setVisible(false);
            showValuesCheckBox.setVisible(true);

        } else if (traceType.isCategorical()) {
            labelBins.setVisible(false);
            binsCombo.setVisible(false);
            showValuesCheckBox.setVisible(true);

        } else {
            throw new RuntimeException("Trace type is not recognized: " + traceType);
        }
    }

}
