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
import dr.inference.trace.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;


/**
 * A panel that displays frequency distributions of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: IntervalsPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class IntervalsPanel extends TraceChartPanel {
    private static final int DEFAULT_KDE_BINS = 5000;

    private final JParallelChart intervalsChart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    private TraceChartPanel.Settings currentSettings = new TraceChartPanel.Settings();

    /**
     * Creates new IntervalsPanel
     */
    public IntervalsPanel(final JFrame frame) {
        super(frame);
        intervalsChart = new JParallelChart(false, new LinearAxis(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS));
        chartPanel = new JChartPanel(intervalsChart, "", "", ""); // xAxisTitle, yAxisTitle

        toolBar = createToolBar();

        setupMainPanel();
    }

    public JChartPanel getChartPanel() {
        return chartPanel;
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(frame, false, true, false, true,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected TraceChartPanel.Settings getSettings() {
        return null;
    }

    @Override
    protected JToolBar createToolBar() {
        JToolBar toolBar = super.createToolBar();

        toolBar.add(createSetupButton());
        
        JLabel label = createColourByComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());

        return toolBar;
    }

    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    protected void setupMainPanel() {
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        add(getToolBar(), BorderLayout.SOUTH);
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        super.setTraces(traceLists, traceNames);

        intervalsChart.removeAllPlots();

        if (traceLists == null || traceNames == null) {
            chartPanel.setXAxisTitle("");
            chartPanel.setYAxisTitle("");
            return;
        }

        setupTraces();
    }
    
    protected void setupTraces() {
        // return if no traces selected
        if (!removeAllPlots(false)) return;

        int i = 0;
        TraceType traceType = null;
        for (TraceList tl : traceLists) {
            for (String traceName : traceNames) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);
                Plot plot = null;

                if (trace != null) {
                    String name = tl.getTraceName(traceIndex);
                    if (traceLists.length > 1) {
                        name = tl.getName() + " - " + name;
                    }

                    java.util.List values = tl.getValues(traceIndex);

                    // set traceType here to avoid Exception from setYLabel
                    traceType = trace.getTraceType();
                    assert traceType.isContinuous();

//                    switch (currentSettings.type) {
//                        case VIOLIN:
                    double lower = trace.getTraceStatistics().getLowerHPD();
                    double upper = trace.getTraceStatistics().getUpperHPD();
                    plot = new ViolinPlot(true, 0.8, lower, upper, false, values, DEFAULT_KDE_BINS);
//                    plot = new ViolinPlot(0.8, values, DEFAULT_KDE_BINS);
                    plot.setName(name);
                    plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[i]);
//                    break;
//                    }

                    if (plot != null) {
                        getChartPanel().getChart().addPlot(plot);
                    }

                    // colourBy
                    if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE || currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE_AND_TRACE) {
                        i++;
                    }
                    if (i == currentSettings.palette.length) {
                        i = 0;
                    }
                }
            }
            if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE) {
                i++;
            } else if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE) {
                i = 0;
            }
            if (i >= currentSettings.palette.length) {
                i = 0;
            }
        }

        // swap in the correct chart panel
        BorderLayout layout = (BorderLayout)getLayout();
        remove(layout.getLayoutComponent(BorderLayout.CENTER));
        remove(layout.getLayoutComponent(BorderLayout.SOUTH));
        add(getChartPanel(), BorderLayout.CENTER);
        add(getToolBar(), BorderLayout.SOUTH);

//        setXLabelMultipleTraces();
//        if (currentSettings.type == ContinuousDensityPanel.Type.VIOLIN) {
            setYLabel("Value");
//        } else {
//            setYLabel(traceType, new String[]{"Density", "Probability"});
//        }
//        setLegend(currentSettings.legendAlignment);

        validate();
        repaint();
    }



//    public JComponent getExportableComponent() {
//        return chartPanel;
//    }
}
