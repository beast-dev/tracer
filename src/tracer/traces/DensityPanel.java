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

import dr.app.gui.chart.*;
import dr.inference.trace.Trace;
import dr.inference.trace.TraceCorrelation;
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;
import dr.stats.Variate;
import jam.framework.Exportable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * A panel that displays density plots of traces
 *
 * @author Andrew Rambaut
 */
public class DensityPanel extends JPanel implements Exportable {

    private final ContinuousDensityPanel continuousDensityPanel;
    private final DiscreteDensityPanel discreteDensityPanel;

    private TraceChartPanel currentPanel;

    /**
     * Creates new FrequencyPanel
     */
    public DensityPanel(final JFrame frame) {
        continuousDensityPanel = new ContinuousDensityPanel(frame);
        discreteDensityPanel = new DiscreteDensityPanel(frame);

        setOpaque(false);
        setLayout(new BorderLayout());

        add(new JLabel("No data loaded"), BorderLayout.NORTH);
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {

        TraceType traceType = TraceType.REAL;

        if (traceLists != null) {
            TraceList traceList = traceLists[0];
            String traceName = traceNames.get(0);

            traceType = traceList.getTrace(traceList.getTraceIndex(traceName)).getTraceType();
        }

        if (traceType.isContinuous()) {
            setDensityPanel(continuousDensityPanel);
            continuousDensityPanel.setTraces(traceLists, traceNames);
        } else {
            setDensityPanel(discreteDensityPanel);
            discreteDensityPanel.setTraces(traceLists, traceNames);
        }

    }

    private void setDensityPanel(TraceChartPanel panel) {
        currentPanel = panel;
        removeAll();
        if (currentPanel != null) {
            add(currentPanel, BorderLayout.CENTER);
        }
    }

    public String toString() {
        return currentPanel.toString();
    }

    @Override
    public JComponent getExportableComponent() {
        return currentPanel.getExportableComponent();
    }
}
