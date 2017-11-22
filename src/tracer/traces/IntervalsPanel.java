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

    public final static Paint BAR_PAINT = new Color(0x2f8aa3);
    public final static  Paint TAIL_PAINT = new Color(0xd6bd58);

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
            chartSetupDialog = new ChartSetupDialog(getFrame(), false, true, false, true,
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
        
//        JLabel label = createColourByComboAndLabel();
//        toolBar.add(label);
//        toolBar.add(label.getLabelFor());

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
        if (!removeAllPlots()) {
            return;
        }

        JLabel messageLabel = null;

        TraceType traceType = null;
        for (TraceList tl : getTraceLists()) {
            for (String traceName : getTraceNames()) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);

                if (traceType == null) {
                    traceType = trace.getTraceType();
                }

                if (traceType != trace.getTraceType()) {
                    messageLabel = new JLabel("<html><div style='text-align: center;'>Traces must be of the same type to visualize here</div></html>");
                }
            }
        }

        BorderLayout layout = (BorderLayout) getLayout();
        remove(layout.getLayoutComponent(BorderLayout.CENTER));
        remove(layout.getLayoutComponent(BorderLayout.SOUTH));

        if (messageLabel == null) {
            for (TraceList tl : getTraceLists()) {
                for (String traceName : getTraceNames()) {
                    int traceIndex = tl.getTraceIndex(traceName);
                    Trace trace = tl.getTrace(traceIndex);
                    Plot plot = null;

                    if (trace != null) {
                        String name = tl.getTraceName(traceIndex);
                        if (getTraceLists().length > 1) {
                            name = tl.getName() + " - " + name;
                        }

                        if (traceType.isContinuous()) {
//                    switch (currentSettings.type) {
//                        case VIOLIN:
                            double lower = trace.getTraceStatistics().getLowerHPD();
                            double upper = trace.getTraceStatistics().getUpperHPD();
                            double lowerTail = trace.getTraceStatistics().getMinimum();
                            double upperTail = trace.getTraceStatistics().getMaximum();
                            double mean = trace.getTraceStatistics().getMean();

//                    plot = new ViolinPlot(true, 0.8, lower, upper, false, values, DEFAULT_KDE_BINS);
                            BoxPlot boxPlot = new BoxPlot(true, 0.6, lower, upper, lowerTail, upperTail, mean);
                            boxPlot.setName(name);
                            boxPlot.setLineStyle(new BasicStroke(1.0f), BAR_PAINT);
                            boxPlot.setMeanLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), BAR_PAINT);

                            plot = boxPlot;
//                    break;
//                    }
                        } else if (traceType.isDiscrete()) {

                            // TODO: how to show multiple discrete traces?
                            // Stacked column charts?
                            // Histogram violins for integers?
                            if (traceType.isCategorical()) {
                            } else {
                            }

                            plot = null;
                        } else {
                            throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
                        }
                    }

                    if (plot != null) {
                        getChartPanel().getChart().addPlot(plot);
                    }

                    // colourBy
//                    if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE || currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE_AND_TRACE) {
//                        i++;
//                    }
//                    if (i == currentSettings.palette.length) {
//                        i = 0;
//                    }
                }
//            if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE) {
//                i++;
//            } else if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE) {
//                i = 0;
//            }
//            if (i >= currentSettings.palette.length) {
//                i = 0;
//            }
            }

            // swap in the correct chart panel
            add(getChartPanel(), BorderLayout.CENTER);
            add(getToolBar(), BorderLayout.SOUTH);

//        setXLabelMultipleTraces();
//        if (currentSettings.type == ContinuousDensityPanel.Type.VIOLIN) {
            setYLabel("Value");
//        } else {
//            setYLabel(traceType, new String[]{"Density", "Probability"});
//        }
//        setLegend(currentSettings.legendAlignment);
        } else {
            add(messageLabel, BorderLayout.CENTER);
        }

        validate();
        repaint();
    }



//    public JComponent getExportableComponent() {
//        return chartPanel;
//    }
}
