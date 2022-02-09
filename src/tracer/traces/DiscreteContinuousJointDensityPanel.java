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
import dr.inference.trace.*;
import dr.stats.Variate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Guy Baele
 * @version $Id: CorrelationPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class DiscreteContinuousJointDensityPanel extends TraceChartPanel {

    private JCheckBox defaultNumberFormatCheckBox = new JCheckBox("Use default number format");

    private JParallelChart parallelChart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    private Settings currentSettings = new Settings();

    /**
     * Creates new CorrelationPanel
     */
    public DiscreteContinuousJointDensityPanel(final JFrame frame) {
        super(frame);

        parallelChart = new JParallelChart(false, new LinearAxis2(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS));
        chartPanel = new JChartPanel(parallelChart, "", "", ""); // xAxisTitle, yAxisTitle

        toolBar = createToolBar(frame);
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
            chartSetupDialog = new ChartSetupDialog(getFrame(), false, false, true, true,
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

    private JToolBar createToolBar(final JFrame frame) {
        JToolBar toolBar = super.createToolBar();

        toolBar.add(createSetupButton());

        JLabel label = createShowComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());

        return toolBar;
    }

    @Override
    protected void setupTraces() {

        getChartPanel().getChart().removeAllPlots();

        assert getTraceCount() == 2;

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

        Trace trace1 = traceList1.getTrace(traceIndex1);
        Trace trace2 = traceList2.getTrace(traceIndex2);

        if (trace1.getTraceType().isDiscrete() && !trace2.getTraceType().isDiscrete() ||
                !trace1.getTraceType().isDiscrete() && trace2.getTraceType().isDiscrete()) {

            defaultNumberFormatCheckBox.setVisible(false);

            if (trace1.getTraceType().isDiscrete()) {
                createDiscreteContinuousPlot(traceList1, traceIndex1, traceList2, traceIndex2);
                setXLabel(traceName1);
                setYLabel(traceName2);
            } else {
                createDiscreteContinuousPlot(traceList2, traceIndex2, traceList1, traceIndex1);
                setXLabel(traceName2);
                setYLabel(traceName1);
            }

        } else {
            throw new RuntimeException("This panel only works with one discrete vs one continuous trace");
        }
    }

    /**
     * This plot creates a continuous parameter density, conditioned on the state of a discrete
     * trace.
     *
     * @param discreteTraceList
     * @param discreteTraceIndex
     * @param continuousTraceList
     * @param continuousTraceIndex
     */
    private void createDiscreteContinuousPlot(TraceList discreteTraceList, int discreteTraceIndex,
                                              TraceList continuousTraceList, int continuousTraceIndex) {

        Map<Integer, List<Double>> valueMap = new HashMap<Integer, List<Double>>();

        TraceCorrelation tc = discreteTraceList.getCorrelationStatistics(discreteTraceIndex);

        // TODO - this doesn't seem to work at present...
//        if (discreteTraceList.getTrace(discreteTraceIndex).getTraceType() != TraceType.CATEGORICAL) {
//            // if an integer discrete axis then allow for missing labels...
//            getChart().setXAxis(new DiscreteAxis(true, discreteTraceList.getTrace(discreteTraceIndex).getUniqueValueCount() < 20));
//        }

        for (int value : tc.getFrequencyCounter().getUniqueValues()) {
            valueMap.put(value, new ArrayList<Double>());
        }

        List<Double> discreteValues = discreteTraceList.getValues(discreteTraceIndex);
        List<Double> continuousValues = continuousTraceList.getValues(continuousTraceIndex);

        int i = 0;
        for (double value : discreteValues) {
            valueMap.get((int)value).add(continuousValues.get(i));
            i++;
        }

        Plot plot;

        for (int discreteValue : tc.getFrequencyCounter().getUniqueValues()) {
            List<Double> values = valueMap.get(discreteValue);
            TraceDistribution td = new TraceDistribution(values, TraceType.REAL);

            switch (currentSettings.show) {
                case VIOLIN:
                    ViolinPlot violinPlot = new ViolinPlot(true, 0.8, td.getLowerHPD(), td.getUpperHPD(), true, values);
                    violinPlot.setLineStyle(new BasicStroke(1.0f), getSettings().palette[0]);
                    plot = violinPlot;

                    break;
                case BOX_AND_WHISKER:
                    double lowerTail = td.getMinimum();
                    double upperTail = td.getMaximum();
                    double mean = td.getMean();

                    BoxPlot boxPlot = new BoxPlot(true, 0.6, td.getLowerHPD(), td.getUpperHPD(), lowerTail, upperTail, mean);
                    boxPlot.setLineStyle(new BasicStroke(1.0f), getSettings().palette[0]);
                    boxPlot.setMeanLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), getSettings().palette[0]);

                    plot = boxPlot;
                    break;
                default:
                    throw new RuntimeException("Unknown plot type");
            }


            if (discreteTraceList.getTrace(discreteTraceIndex).getTraceType() == TraceType.CATEGORICAL) {
                plot.setName(discreteTraceList.getTrace(discreteTraceIndex).getCategoryLabelMap().get(discreteValue));
            } else {
                plot.setName(Integer.toString(discreteValue));
            }

            getChart().addPlot(plot);
        }


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
