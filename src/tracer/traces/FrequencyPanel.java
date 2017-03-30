/*
 * FrequencyPanel.java
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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;


/**
 * A panel that displays frequency distributions of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FrequencyPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class FrequencyPanel extends OneTraceChartPanel {

    private Settings currentSettings = new Settings();
//    private Map<String, Settings> settingsMap = new HashMap<String, Settings>();

    /**
     * Creates new FrequencyPanel
     */
    public FrequencyPanel(final JFrame frame) {
        super(frame);
        traceChart = new DiscreteJChart(
                new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        initJChartPanel("", "Frequency"); // xAxisTitle, yAxisTitle
        JToolBar toolBar = setupToolBar(frame);
        addMainPanel(toolBar, false);
    }

    protected DiscreteJChart getTraceChart() {
        return (DiscreteJChart) traceChart;
    }

    protected JToolBar setupToolBar(final JFrame frame) {
        JToolBar toolBar = super.setupToolBar(frame, currentSettings);

        addBins(toolBar);
        binsCombo.setSelectedItem(currentSettings.minimumBins);

        // +++++++ Listener ++++++++
        chartSetupButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (currentSettings.chartSetupDialog == null) {
                            currentSettings.chartSetupDialog = new ChartSetupDialog(frame, true, false,
                                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
                        }

                        currentSettings.chartSetupDialog.showDialog(getTraceChart());
                        validate();
                        repaint();
                    }
                }
        );

        binsCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        currentSettings.minimumBins = (Integer) binsCombo.getSelectedItem();
                        setupTrace();
                    }
                }
        );

        //        toolBar.add(showValuesCheckBox); //todo
        showValuesCheckBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {

                        validate();
                        repaint();
                    }
                }
        );

        return toolBar;
    }


    public void setTrace(TraceList traceList, String traceName) {
        initSettings(traceList, traceName);
        binsCombo.setSelectedItem(currentSettings.minimumBins);

        setupTrace();
    }


    protected void setupTrace() {

        if (!rmAllPlots()) return;

        FrequencyPlot plot = null;
        int traceIndex = traceList.getTraceIndex(traceName);
        Trace trace = traceList.getTrace(traceIndex);
        TraceCorrelation td = traceList.getCorrelationStatistics(traceIndex);

        if (trace != null) {
            List values = traceList.getValues(traceIndex);
            TraceType traceType = trace.getTraceType();
            if (traceType == TraceType.REAL) {
                plot = new FrequencyPlot(values, currentSettings.minimumBins, td);

                if (td != null) {
                    plot.setIntervals(td.getUpperHPD(), td.getLowerHPD());
                }

            } else if (traceType == TraceType.ORDINAL || traceType == TraceType.BINARY) {
                plot = new FrequencyPlot(values, -1, td);

                if (td != null) {
                    plot.setInCredibleSet(td);
                }

            } else if (traceType == TraceType.CATEGORICAL) {

                plot = new FrequencyPlot(values, td); // convert into index inside constructor

                if (td != null) {
                    plot.setInCredibleSet(td);
                }
            } else {
                throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
            }

            setXAxis(td);
            setYLab(traceType, new String[]{"Frequency", "Count"});
            setBinsComponents(traceType);
            setChartSetupDialog(currentSettings);

            getTraceChart().addPlot(plot);
        }
        setXLab(traceIndex);
    }

}
