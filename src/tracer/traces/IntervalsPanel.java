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
import java.awt.event.ActionEvent;
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

    private static final int DEFAULT_KDE_BINS = 5000;

    private enum ShowType {
        BOX_AND_WHISKER("box and whisker"),
        VIOLIN("violin");

        ShowType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private String name;
    }

    private final JParallelChart intervalsChart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    class Settings extends TraceChartPanel.Settings {
        ShowType show = ShowType.BOX_AND_WHISKER;
    }

    private Settings currentSettings = new Settings();

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
        return currentSettings;
    }

    @Override
    protected JToolBar createToolBar() {
        JToolBar toolBar = super.createToolBar();

        toolBar.add(createSetupButton());

        JLabel label = createShowComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());

        return toolBar;
    }

    private JLabel createShowComboAndLabel() {
        JLabel labelShow = new JLabel("Show:");
        final JComboBox showCombo = new JComboBox(ShowType.values());
        showCombo.setFont(UIManager.getFont("SmallSystemFont"));
        showCombo.setOpaque(false);
        labelShow.setFont(UIManager.getFont("SmallSystemFont"));
        labelShow.setLabelFor(showCombo);

        showCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentSettings.show = (ShowType)showCombo.getSelectedItem();
                        setupTraces();
                    }
                }
        );
        return labelShow;
    }


    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    protected void setupTraces() {
        TraceType traceType = null;
        Set<String> categoryLabels = null;

        for (TraceList tl : getTraceLists()) {
            for (String traceName : getTraceNames()) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);

                if (traceType == null) {
                    traceType = trace.getTraceType();
                }

                if (traceType == TraceType.CATEGORICAL) {
                    Set<String> labels = new HashSet<String>(trace.getCategoryLabelMap().values());
                    if (categoryLabels == null) {
                        categoryLabels = labels;
                    }
                    labels.retainAll(categoryLabels);
                    if (labels.size() == 0) {
                        setMessage("Categorical traces must have common values to visualize here.");
                        return;
                    }
                    categoryLabels.addAll(trace.getCategoryLabelMap().values());
                }

                if (traceType != trace.getTraceType()) {
                    setMessage("Traces must be of the same type to visualize here.");
                    return;
                }
            }
        }

//        BorderLayout layout = (BorderLayout) getLayout();
//        remove(layout.getLayoutComponent(BorderLayout.CENTER));
//        remove(layout.getLayoutComponent(BorderLayout.SOUTH));

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
                        double lower = trace.getTraceStatistics().getLowerHPD();
                        double upper = trace.getTraceStatistics().getUpperHPD();

                        switch (currentSettings.show) {
                            case VIOLIN:
                                plot = new ViolinPlot(true, 0.6, lower, upper, true, tl.getValues(traceIndex), DEFAULT_KDE_BINS);
                                plot.setLineStyle(new BasicStroke(1.0f), BAR_PAINT);
                                break;
                            case BOX_AND_WHISKER:
                                double lowerTail = trace.getTraceStatistics().getMinimum();
                                double upperTail = trace.getTraceStatistics().getMaximum();
                                double mean = trace.getTraceStatistics().getMean();

                                BoxPlot boxPlot = new BoxPlot(true, 0.6, lower, upper, lowerTail, upperTail, mean);
                                boxPlot.setLineStyle(new BasicStroke(1.0f), BAR_PAINT);
                                boxPlot.setMeanLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), BAR_PAINT);

                                plot = boxPlot;
                                break;
                        }

                        plot.setName(name);

                    } else if (traceType.isDiscrete()) {

                        if (traceType.isCategorical()) {
                            // TODO: how to show multiple categorical traces?
                            // Stacked column charts?
                            plot = null;
                        } else {
                            double lower = trace.getTraceStatistics().getLowerHPD();
                            double upper = trace.getTraceStatistics().getUpperHPD();
                            IntegerViolinPlot violinPlot = new IntegerViolinPlot(true, 0.6, lower, upper, false, trace.getFrequencyCounter());

                            if (trace.getUniqueValueCount() > 2) {
                                // don't show hpds for binary traces...
                                violinPlot.setIntervals(trace.getTraceStatistics().getLowerHPD(), trace.getTraceStatistics().getUpperHPD());
                            }

                            violinPlot.setName(name);
                            violinPlot.setLineStyle(new BasicStroke(0.5f), Color.black);
                            violinPlot.setPaints(BAR_PAINT, TAIL_PAINT);

                            Axis yAxis = new DiscreteAxis(true, true);
                            getChartPanel().getChart().setYAxis(yAxis);

                            if (trace.getUniqueValueCount() == 1) {
                                yAxis.addRange(0, 1);
                            }

                            plot = violinPlot;
                        }

                    } else {
                        throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
                    }
                }

                if (plot != null) {
                    getChartPanel().getChart().addPlot(plot);
                }

            }

            // swap in the correct chart panel
            add(getChartPanel(), BorderLayout.CENTER);
            add(getToolBar(), BorderLayout.SOUTH);

            setYLabel("Value");
        }
    }

}
