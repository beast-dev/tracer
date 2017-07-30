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
import dr.inference.trace.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * A panel that displays frequency distributions of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FrequencyPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class FrequencyPanel extends TraceChartPanel {

    public final static Paint BAR_PAINT = new Color(0x2f8aa3);
    public final static  Paint QUANTILE_PAINT = new Color(0xd6bd58);

    private final JChart traceChart;
    private final JChartPanel traceChartPanel;

    private Settings currentSettings = new Settings();
//    private Map<String, Settings> settingsMap = new HashMap<String, Settings>();

    /**
     * Creates new FrequencyPanel
     */
    public FrequencyPanel(final JFrame frame) {
        super(frame);
        traceChart = new DiscreteJChart(
                new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        traceChartPanel = new JChartPanel(traceChart, "","", "Frequency"); // xAxisTitle, yAxisTitle
        JToolBar toolBar = setupToolBar(frame);
        addMainPanel(toolBar, false);
    }

    public JChartPanel getChartPanel() {
        return traceChartPanel;
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

                        currentSettings.chartSetupDialog.showDialog(getChartPanel().getChart());
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
        setTraces(new TraceList[] { traceList },
                Collections.singletonList(traceName));
        binsCombo.setSelectedItem(currentSettings.minimumBins);

        setupTrace();
    }


    protected void setupTrace() {

        removeAllPlots();

        FrequencyPlot plot = null;
        TraceList traceList = traceLists[0];

        if (traceList == null) {
            return;
        }

        int traceIndex = traceList.getTraceIndex(traceNames.get(0));
        Trace trace = traceList.getTrace(traceIndex);
        TraceCorrelation td = traceList.getCorrelationStatistics(traceIndex);

        if (trace != null) {
            List values = traceList.getValues(traceIndex);
            TraceType traceType = trace.getTraceType();
            if (traceType.isContinuous()) {
                plot = new FrequencyPlot(values, currentSettings.minimumBins, td);

                plot.setPaints(BAR_PAINT, QUANTILE_PAINT);

                if (td != null) {
                    plot.setIntervals(td.getUpperHPD(), td.getLowerHPD());
                }

            } else if (traceType.isIntegerOrBinary()) {
                plot = new FrequencyPlot(values, -1, td);

                if (td != null) {
//                    plot.setInCredibleSet(td);
                    plot.setIntervals(td.getUpperHPD(), td.getLowerHPD()); // Integer coloured by HPD not Credible set
                }

            } else if (traceType.isCategorical()) {

                plot = new FrequencyPlot(values, td); // convert into index inside constructor

                if (td != null) {
                    plot.setInCredibleSet(td);
                }
            } else {
                throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
            }

//            setXAxis(td);
            setYLabel(traceType, new String[]{"Frequency", "Count"});
            setBinsComponents(traceType);
            setChartSetupDialog(currentSettings);

            getChartPanel().getChart().addPlot(plot);
        }
        setXLabel(traceList.getTraceName(traceIndex));
    }

    protected void setBinsComponents(TraceType traceType) {

        if (traceType.isContinuous()) {
            labelBins.setVisible(true);
            binsCombo.setVisible(true);
            showValuesCheckBox.setVisible(false);

        } else if (traceType.isIntegerOrBinary()) {
            labelBins.setVisible(false);
            binsCombo.setVisible(false);
            showValuesCheckBox.setVisible(true);

        } else if (traceType.isCategorical()) {
            labelBins.setVisible(false);
            binsCombo.setVisible(false);
            showValuesCheckBox.setVisible(true);

        } else {
            throw new RuntimeException("Trace type is not recognized: " + traceType);
        }
    }

}
