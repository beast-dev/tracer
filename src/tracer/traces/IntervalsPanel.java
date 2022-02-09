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
import dr.inference.trace.Trace;
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;


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

    private final JParallelChart intervalsChart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;
    private JLabel showComboLabel;

    private Settings currentSettings = new Settings();

    /**
     * Creates new IntervalsPanel
     */
    public IntervalsPanel(final JFrame frame) {
        super(frame);
        intervalsChart = new JParallelChart(false, new LinearAxis2(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS));
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

        showComboLabel = createShowComboAndLabel();
        toolBar.add(showComboLabel);
        toolBar.add(showComboLabel.getLabelFor());

        return toolBar;
    }

    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    protected void setupTraces() {
        TraceType traceType = null;
        Set<String> categoryLabels = null;

        getChartPanel().getChart().removeAllPlots();

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

                // multiple integer traces only have violin plot
                showComboLabel.getLabelFor().setVisible(traceType.isContinuous());

                if (traceType != trace.getTraceType()) {
                    setMessage("Traces must be of the same type to visualize here.");
                    return;
                }

                if (trace.getTraceStatistics() == null) {
                    setMessage("Trace statistics are still being calculated.");
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
                            Axis yAxis = new DiscreteAxis(true, true);
                            getChartPanel().getChart().setYAxis(yAxis);

                            // TODO: setIntervals & setPaints not in AbstractPlot, a new child class?
                            int uniqueValueCount = trace.getUniqueValueCount();
                            if (uniqueValueCount < 2) {
//                                yAxis.addRange(0, 1); // why?

                                // only has 1 value, so use mean
                                double mean = trace.getTraceStatistics().getMean();

                                IntegerViolinPlot oneValuePlot = new IntegerViolinPlot(true, 0.6, mean, mean, false, trace.getFrequencyCounter());
                                oneValuePlot.setIntervals(mean, mean);
                                oneValuePlot.setName(name);
                                oneValuePlot.setLineStyle(new BasicStroke(0.5f), Color.black);
                                oneValuePlot.setPaints(BAR_PAINT, TAIL_PAINT);

                                plot = oneValuePlot;
                            } else {
                                double lower;
                                double upper;
                                if (uniqueValueCount == 2) {
                                    // has 2 values, use min and max
                                     lower = trace.getTraceStatistics().getMinimum();
                                     upper = trace.getTraceStatistics().getMaximum();

                                } else {
                                    // more than 2 values, use hpd, and ViolinPlot works
                                    lower = trace.getTraceStatistics().getLowerHPD();
                                    upper = trace.getTraceStatistics().getUpperHPD();
                                }
//                                    ViolinPlot violinPlot = new ViolinPlot(true, 0.6, lower, upper, false, tl.getValues(traceIndex));
//
////                                if (trace.getUniqueValueCount() > 2) {
//                                    // don't show hpds for binary traces...
//                                    violinPlot.setIntervals(lower, upper);
////                                }
//                                    violinPlot.setName(name);
//                                    violinPlot.setLineStyle(new BasicStroke(0.5f), Color.black);
//                                    violinPlot.setPaints(BAR_PAINT, TAIL_PAINT);
//
//                                    plot = violinPlot;

                                IntegerViolinPlot integerViolinPlot = new IntegerViolinPlot(true, 0.6, lower, upper, false, trace.getFrequencyCounter());
                                integerViolinPlot.setIntervals(lower, upper);
                                integerViolinPlot.setName(name);
                                integerViolinPlot.setLineStyle(new BasicStroke(0.5f), Color.black);
                                integerViolinPlot.setPaints(BAR_PAINT, TAIL_PAINT);

                                plot = integerViolinPlot;
                            }
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
