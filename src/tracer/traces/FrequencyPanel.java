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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


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

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    /**
     * Creates new FrequencyPanel
     */
    public FrequencyPanel(final JFrame frame) {
        super(frame);
        traceChart = new DiscreteJChart(
                new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        traceChartPanel = new JChartPanel(traceChart, "","", "Frequency"); // xAxisTitle, yAxisTitle
        toolBar = createToolBar(currentSettings);

        setupMainPanel(false);
    }

    public JChartPanel getChartPanel() {
        return traceChartPanel;
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(frame, false, false, true, false,
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

    private JToolBar createToolBar(Settings settings) {
        JToolBar toolBar = super.createToolBar();


        JLabel label = (JLabel)createBinsComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());

        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.minimumBins);

        toolBar.add(createShowValuesCheckBox());

        return toolBar;
    }

    protected JCheckBox createShowValuesCheckBox() {
        JCheckBox checkBox = new JCheckBox("Show values");
        checkBox.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setupTraces();
                    }
                }
        );
        return checkBox;
    }


    public void setTrace(TraceList traceList, String traceName) {
        setTraces(new TraceList[] { traceList },
                Collections.singletonList(traceName));
        // binsCombo.setSelectedItem(currentSettings.minimumBins);

        setupTraces();
    }

    @Override
    protected void setupTraces() {

        removeAllPlots();

        FrequencyPlot plot = null;

        if (traceLists == null || traceLists[0] == null) {
            return;
        }

        TraceList traceList = traceLists[0];

        int traceIndex = traceList.getTraceIndex(traceNames.get(0));
        Trace trace = traceList.getTrace(traceIndex);
        TraceCorrelation td = traceList.getCorrelationStatistics(traceIndex);

        if (trace != null) {
            List<Double> values = traceList.getValues(traceIndex);
            TraceType traceType = trace.getTraceType();
            if (traceType.isContinuous()) {
                plot = new FrequencyPlot(values, currentSettings.minimumBins, td);

                plot.setPaints(BAR_PAINT, QUANTILE_PAINT);

                if (td != null) {
                    plot.setIntervals(td.getUpperHPD(), td.getLowerHPD());
                }
                getChartPanel().getChart().setXAxis(new LinearAxis());

            } else if (traceType.isIntegerOrBinary()) {
                plot = new FrequencyPlot(values, -1, td);

                if (td != null) {
//                    plot.setInCredibleSet(td);
                    plot.setIntervals(td.getUpperHPD(), td.getLowerHPD()); // Integer coloured by HPD not Credible set
                }

                getChartPanel().getChart().setXAxis(new DiscreteAxis(true, true));

            } else if (traceType.isCategorical()) {

                List<Integer> intValues = new ArrayList<Integer>();
                Map<Integer, Integer> categoryOrderMap = trace.getCategoryOrderMap();
                for (Double value : values) {
                    intValues.add(categoryOrderMap.get(value.intValue()));
                }

                plot = new FrequencyPlot(intValues, td);

                getChartPanel().getChart().setXAxis(new DiscreteAxis(trace.getCategoryLabelMap(), true, true));

                if (td != null) {
                    plot.setInCredibleSet(td);
                }
            } else {
                throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
            }

//            setXAxis(td);
            setYLabel(traceType, new String[]{"Frequency", "Count"});
            setBinsComponents(traceType);

            getChartPanel().getChart().addPlot(plot);
            getChartPanel().getChart().setOriginStyle(null, null);
        }
        setXLabel(traceList.getTraceName(traceIndex));
    }

    protected void setBinsComponents(TraceType traceType) {

        if (traceType.isContinuous()) {
//            labelBins.setVisible(true);
//            binsCombo.setVisible(true);
//            showValuesCheckBox.setVisible(false);

        } else if (traceType.isIntegerOrBinary()) {
//            labelBins.setVisible(false);
//            binsCombo.setVisible(false);
//            showValuesCheckBox.setVisible(true);

        } else if (traceType.isCategorical()) {
//            labelBins.setVisible(false);
//            binsCombo.setVisible(false);
//            showValuesCheckBox.setVisible(true);

        } else {
            throw new RuntimeException("Trace type is not recognized: " + traceType);
        }
    }


}
