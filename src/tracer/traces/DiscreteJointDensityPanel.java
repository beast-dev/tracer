/*
 * JointDensityPanel.java
 *
 * Copyright (c) 2002-2017 Alexei Drummond, Andrew Rambaut and Marc Suchard
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
import dr.inference.trace.TraceCorrelation;
import dr.inference.trace.TraceList;
import dr.stats.Variate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that displays correlation plots of 2 discrete traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Guy Baele
 * @version $Id: CorrelationPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class DiscreteJointDensityPanel extends TraceChartPanel {

    private JCheckBox defaultNumberFormatCheckBox = new JCheckBox("Use default number format");

    private JChart chart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    private Settings currentSettings = new Settings();

    /**
     * Creates new CorrelationPanel
     */
    public DiscreteJointDensityPanel(final JFrame frame) {
        super(frame);

        chart = new JChart(new LinearAxis2(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis2(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS));
        chartPanel = new JChartPanel(chart, "", "", ""); // xAxisTitle, yAxisTitle

        toolBar = createSinglePairToolBar(frame);
    }

    public JChartPanel getChartPanel() {
        return chartPanel;
    }

    public JChart getChart() {
        return getChartPanel().getChart();
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(getFrame(), true, true, true, true,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected Settings getSettings() {
        return currentSettings;
    }

    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    private JToolBar createSinglePairToolBar(final JFrame frame) {
        JToolBar toolBar = super.createToolBar();

//        toolBar.add(createSetupButton());

//        JLabel label = createShowComboAndLabel();
//        toolBar.add(label);
//        toolBar.add(label.getLabelFor());

        return toolBar;
    }

    @Override
    protected void setupTraces() {

        getChartPanel().getChart().removeAllPlots();

        TraceList traceList1 = null;
        TraceList traceList2 = null;
        int traceIndex1 = -1;
        int traceIndex2 = -1;

        String traceName1;
        String traceName2;

        if (getTraceLists().length == 2 && getTraceNames().size() == 1) {
            // two trace files and one trace
            traceList1 = getTraceLists()[0];
            traceList2 = getTraceLists()[1];
            traceName1 = traceList1.getName();
            traceName2 = traceList2.getName();
            traceIndex1 = traceList1.getTraceIndex(getTraceNames().get(0));
            traceIndex2 = traceList2.getTraceIndex(getTraceNames().get(0));
            traceName1 = traceName1 + " - " + traceList1.getTraceName(traceIndex1);
            traceName2 = traceName2 + " - " + traceList2.getTraceName(traceIndex2);
        } else if (getTraceLists().length == 1 && getTraceNames().size() == 2) {
            // one trace files and two trace
            traceList1 = getTraceLists()[0];
            traceList2 = getTraceLists()[0];
            traceIndex1 = traceList1.getTraceIndex(getTraceNames().get(0));
            traceIndex2 = traceList2.getTraceIndex(getTraceNames().get(1));
            traceName1 = traceList1.getTraceName(traceIndex1);
            traceName2 = traceList2.getTraceName(traceIndex2);
        } else {
            throw new RuntimeException("Should not reach here");
        }

        TraceCorrelation td1 = traceList1.getCorrelationStatistics(traceIndex1);
        TraceCorrelation td2 = traceList2.getCorrelationStatistics(traceIndex2);

        assert td1.getTraceType().isDiscrete() && td2.getTraceType().isDiscrete();

        // both are discrete
        chartPanel.add(chart, "Chart");
        createDiscreteBubblePlot(traceList1, traceIndex1, traceList2, traceIndex2);


        setXLabel(traceName1);
        setYLabel(traceName2);

    }

    private void createDiscreteBubblePlot(TraceList traceList1, int traceIndex1, TraceList traceList2, int traceIndex2) {
        List<Double> values1 = traceList1.getValues(traceIndex1);
        List<Double> values2 = traceList2.getValues(traceIndex2);
        int maxCount = Math.max(values1.size(), values2.size());
        int minCount = Math.min(values1.size(), values2.size());

        int sampleSize = minCount;

        int k = 0;
        List<Double> samples1 = new ArrayList<Double>();
        for (int i = 0; i < sampleSize; i++) {
            samples1.add(i, ((Number) values1.get(k)).doubleValue());
            k += minCount / sampleSize;
        }

        k = 0;
        List<Double> samples2 = new ArrayList<Double>();
        for (int i = 0; i < sampleSize; i++) {
            samples2.add(i, ((Number) values2.get(k)).doubleValue());
            k += minCount / sampleSize;
        }

        TraceCorrelation td1 = traceList1.getCorrelationStatistics(traceIndex1);
        TraceCorrelation td2 = traceList2.getCorrelationStatistics(traceIndex2);

        // set axis
        if (td1.getTraceType().isCategorical()) {
            getChart().setXAxis(new DiscreteAxis(traceList1.getTrace(traceIndex1).getCategoryLabelMap(), true, true));
        } else {
            // is integer
            getChart().setXAxis(new DiscreteAxis(true, traceList1.getTrace(traceIndex1).getUniqueValueCount() < 20));
        }
        if (td2.getTraceType().isCategorical()) {
            getChart().setYAxis(new DiscreteAxis(traceList2.getTrace(traceIndex2).getCategoryLabelMap(), true, true));
        } else {
            // is integer
            getChart().setYAxis(new DiscreteAxis(true, traceList2.getTrace(traceIndex2).getUniqueValueCount() < 20));
        }

        // add plot
        Plot plot;
        // samples1 samples2 are both ordinal
        plot = new TangHuLuPlot(samples1, samples2);
        getChart().setOriginStyle(null, null);
        getChart().addPlot(plot);

    }

    public String toString() {
        if (getChart().getPlotCount() == 0) {
            return "no plot available";
        }

        StringBuffer buffer = new StringBuffer();

        Plot plot = getChart().getPlot(0);
        Variate xData = plot.getXData();
        Variate yData = plot.getYData();

        buffer.append(chartPanel.getXAxisTitle());
        buffer.append("\t");
        buffer.append(chartPanel.getYAxisTitle());
        buffer.append("\n");

        for (int i = 0; i < xData.getCount(); i++) {
            buffer.append(String.valueOf(xData.get(i)));
            buffer.append("\t");
            buffer.append(String.valueOf(yData.get(i)));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    @Override
    public JComponent getExportableComponent() {
        return getChartPanel();
    }
}
