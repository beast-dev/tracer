/*
 * IntervalsPanel.java
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
import dr.inference.trace.TraceDistribution;
import dr.inference.trace.TraceList;

import javax.swing.*;
import java.awt.*;


/**
 * A panel that displays frequency distributions of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: IntervalsPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class IntervalsPanel extends NTracesChartPanel {

//    private JIntervalsChart intervalsChart = new JIntervalsChart(new LinearAxis());
//    private JChartPanel chartPanel = new JChartPanel(intervalsChart, null, "", "");

    /**
     * Creates new IntervalsPanel
     */
    public IntervalsPanel(final JFrame frame) {
        super(frame);
        traceChart = new BoxPlotChart(new LinearAxis());
        initJChartPanel("", ""); // xAxisTitle, yAxisTitle

        JToolBar toolBar = setupToolBar(frame);
        addMainPanel(toolBar);
    }

    @Override
    protected BoxPlotChart getTraceChart() {
        return (BoxPlotChart) traceChart;
    }

    @Override
    protected JToolBar setupToolBar(JFrame frame) {
        setOpaque(false);
        setMinimumSize(new Dimension(300, 150));
        return null;
    }

    protected void addMainPanel(JToolBar toolBar) {
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        super.setTraces(traceLists, traceNames);

        getTraceChart().removeAllIntervals();

        if (traceLists == null || traceNames == null) {
            chartPanel.setXAxisTitle("");
            chartPanel.setYAxisTitle("");
            return;
        }

        setupTraces();
    }

    @Override
    protected void setupTraces() {
        for (TraceList traceList : traceLists) {
            for (String traceName : traceNames) {
                int index = traceList.getTraceIndex(traceName);
                TraceDistribution td = traceList.getCorrelationStatistics(index);
                if (td != null) {
                    String name = "";
                    if (traceLists.length > 1) {
                        name = traceList.getName();
                        if (traceNames.size() > 1) {
                            name += ": ";
                        }
                    }
                    name += traceName;
                    if (td.getTraceType().isOrdinalOrBinary())
                        getTraceChart().addBoxPlots(name, td.getMedian(), td.getQ1(), td.getQ3(),
                                td.getMinimum(), td.getMaximum());
                    else
                        getTraceChart().addIntervals(name, td.getMean(), td.getUpperHPD(), td.getLowerHPD(), false);
                }
            }
        }

        setXLabMultiTraces();

        validate();
        repaint();
    }


//    public JComponent getExportableComponent() {
//        return chartPanel;
//    }
}
