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

/**
 * A shared code for the panel that displays a single trace in a plot
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Walter Xie
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public abstract class OneTraceChartPanel extends TraceChartPanel {


    /**
     * Creates new FrequencyPanel
     */
    public OneTraceChartPanel(final JFrame frame) {
        super(frame);
    }

    //++++++ setup traces +++++++
    protected TraceList traceList = null;
    protected String traceName = null;

    public abstract void setTrace(TraceList traceList, String traceName);

//    protected void initTrace(TraceList traceList, String traceName) {
//        this.traceList = traceList;
//        this.traceName = traceName;
//
//        Settings settings = settingsMap.get(traceName);
//        if (settings == null) {
//            settings = new Settings();
//            settingsMap.put(traceName, settings);
//        }
//        currentSettings = settings;
//    }

    //++++++ setup chart +++++++

    protected void setXLab(int traceIndex) {
        chartPanel.setXAxisTitle(traceList.getTraceName(traceIndex));
    }

}
