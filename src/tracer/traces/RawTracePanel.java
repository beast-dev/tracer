/*
 * RawTracePanel.java
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
import dr.stats.Variate;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A panel that displays information about traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: RawTracePanel.java,v 1.2 2006/11/30 17:39:29 rambaut Exp $
 */
public class RawTracePanel extends TraceChartPanel {

    private Settings currentSettings = new Settings();

    private JCheckBox burninCheckBox = new JCheckBox("Show Burn-in");
    private JCheckBox sampleCheckBox = new JCheckBox("Sample only");
    private JCheckBox linePlotCheckBox = new JCheckBox("Draw line plot");

    private JButton listenButton = new JButton("Listen");

    private JChart traceChart;
    private final JChartPanel chartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar toolBar;

    /**
     * Creates new RawTracePanel
     */
    public RawTracePanel(final JFrame frame) {
        super(frame);
        traceChart = new JTraceChart(new LinearAxis(Axis.AT_ZERO, Axis.AT_DATA), new LinearAxis());
        chartPanel = new JChartPanel(traceChart, "", "", ""); // xAxisTitle, yAxisTitle
        toolBar = createToolBar(currentSettings);
    }

    public JChartPanel getChartPanel() {
        return chartPanel;
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(getFrame(), false, true, true, true,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected Settings getSettings() {
        return currentSettings;
    }

    protected JTraceChart getChart() {
        return (JTraceChart) traceChart;
    }

    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    private JToolBar createToolBar(Settings settings) {
        JToolBar toolBar = super.createToolBar();

        burninCheckBox.setSelected(true);
        burninCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        burninCheckBox.setOpaque(false);
        toolBar.add(burninCheckBox);

        sampleCheckBox.setSelected(false);
        sampleCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        sampleCheckBox.setOpaque(false);
        toolBar.add(sampleCheckBox);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));
        linePlotCheckBox.setSelected(true);
        linePlotCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        linePlotCheckBox.setOpaque(false);
        toolBar.add(linePlotCheckBox);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        JLabel label = (JLabel)createLegendComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.legendAlignment);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        label = (JLabel)createColourByComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.colourBy.ordinal());

//        toolBar.add(listenButton);
//        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        burninCheckBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        setupTraces();
                    }
                }
        );

        sampleCheckBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        getChart().setUseSample(sampleCheckBox.isSelected());
                        validate();
                        repaint();
                    }
                }
        );

        linePlotCheckBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        getChart().setIsLinePlot(linePlotCheckBox.isSelected());
                        validate();
                        repaint();
                    }
                }
        );

        listenButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        int n = 0;
                        for (TraceList tl : getTraceLists()) {
                            for (String traceName : getTraceNames()) {
                                int traceIndex = tl.getTraceIndex(traceName);

                                Trace trace = tl.getTrace(traceIndex);
                                if (trace != null) {
                                    if (trace.getTraceType().isNumber()) {
                                        n++;
                                    }
                                }
                            }
                        }
                        Double[][] valueArrays = new Double[n][];
                        int k = 0;
                        for (TraceList tl : getTraceLists()) {
                            for (String traceName : getTraceNames()) {
                                int traceIndex = tl.getTraceIndex(traceName);

                                Trace trace = tl.getTrace(traceIndex);
                                if (trace != null) {
                                    if (trace.getTraceType().isNumber()) {
                                        List values = tl.getValues(traceIndex);
                                        Double[] ar = new Double[values.size()];
                                        values.toArray(ar);
                                        valueArrays[k] = ar;
                                        k++;
                                    }
                                }
                            }
                        }
                        toAudio(valueArrays);
                    }
                }
        );

        return toolBar;
    }

    @Override
    protected void setupTraces() {

        getChart().removeAllTraces();
                     
        TraceType traceType = null;
        Set<String> categoryLabels = null;

        List valuesX = new ArrayList();
        List valuesY = new ArrayList();

        for (TraceList tl : getTraceLists()) {
            long stateStart = tl.getBurnIn();
            long stateStep = tl.getStepSize();

            for (String traceName : getTraceNames()) {
                int traceIndex = tl.getTraceIndex(traceName);

                String name = tl.getTraceName(traceIndex);
                if (getTraceLists().length > 1) {
                    name = tl.getName() + " - " + name;
                }

                Trace trace = tl.getTrace(traceIndex);

                if (traceType == null) {
                    traceType = trace.getTraceType();
                }

                if (traceType != trace.getTraceType()) {
                    setMessage("Traces must be of the same type to visualize here.");
                    return;
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

                List values = tl.getValues(traceIndex);
                List burninValues = null;
                if (burninCheckBox.isSelected() && tl.getBurninStateCount() > 0) {
                    burninValues = tl.getBurninValues(traceIndex);
                }
                double[] minMax;
                if (trace.getTraceType().isNumber()) {

                    if (trace.getTraceType().isInteger()) {
                        //change Y axis to discrete
                        getChart().setYAxis(new DiscreteAxis(true, true));
                        getChart().getYAxis().setAxisFlags(Axis.AT_DATA, Axis.AT_DATA);

                        if (trace.getTraceType().isBinary()) {
                            getChart().getYAxis().setManualAxis(0, 1.0, 1.0, 0.0);
                            getChart().getYAxis().setManualRange(0.0, 1.0);
                            getChart().getYAxis().setRange(0.0, 1.0);
                        }
                    }
                    int selectedColour = currentSettings.cm.addTraceColour(tl.getFullName(), name, currentSettings.colourBy);
                    //System.out.println(tl.getName() + " ; " + name + " : " + selectedColour);
                    minMax = getChart().addTrace(name, stateStart, stateStep, values, burninValues, currentSettings.palette[selectedColour]);
                } else if (trace.getTraceType() == TraceType.CATEGORICAL) {
                    //change Y axis to discrete
                    getChart().setYAxis(new DiscreteAxis(trace.getCategoryLabelMap(), true, true));
                    int selectedColour = currentSettings.cm.addTraceColour(tl.getFullName(), name, currentSettings.colourBy);
                    //System.out.println(tl.getName() + " ; " + name + " : " + selectedColour);
                    minMax = getChart().addTrace(name, stateStart, stateStep, values, burninValues, currentSettings.palette[selectedColour]);
                } else {
                    throw new RuntimeException("Trace type is not recognized: " + trace.getTraceType());
                }
                valuesX.add(minMax[0]);
                valuesX.add(minMax[1]);
                valuesY.add(minMax[2]);
                valuesY.add(minMax[3]);

            }
        }
        if (getTraceLists().length > 1 || getTraceNames().size() > 1) {
            Variate.D xV = new Variate.D(valuesX);
            Variate.D yV = new Variate.D(valuesY);
            getChart().setRange(xV.getMin(), xV.getMax(), yV.getMin(), yV.getMax());
        }

        setXLabel("State");
        setYAxisLabel();
        setLegend(currentSettings.legendAlignment);

        validate();
        repaint();
    }

    public void traceRemoved() {
        currentSettings.cm.clear();
        //System.out.println("Resetting colour manager");
    }

//    public JComponent getExportableComponent() {
//        return chartPanel;
//    }

    public void toAudio(Double[][] values) {
        int volume = 128;

        int count = values[0].length;
//        float frequency = 44100;
        float frequency = 10000;
        float audioLength = 2; // 2 second clip
        byte[] buf;
        AudioFormat af;

        int repeats = (int) (audioLength * frequency / count);

        buf = new byte[values.length];
        af = new AudioFormat(frequency, 8, values.length, true, false);

        double[] minValues = new double[values.length];
        double[] ranges = new double[values.length];

        for (int k = 0; k < values.length; k++) {
            double maxValue = -Double.MAX_VALUE;
            double minValue = Double.MAX_VALUE;

            for (int i = 0; i < values.length; i++) {
                if (values[k][i] > maxValue) {
                    maxValue = values[k][i];
                }
                if (values[k][i] < minValue) {
                    minValue = values[k][i];
                }
            }
            minValues[k] = minValue;
            ranges[k] = maxValue - minValue;
        }

        SourceDataLine sdl = null;
        try {
            sdl = AudioSystem.getSourceDataLine(af);
            sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
//            for(int i=0; i < msecs*frequency/1000; i++){
            for (int i = 0; i < values[0].length; i++) {

                for (int k = 0; k < values.length; k++) {
                    double x = (values[k][i] - minValues[k]) / ranges[k];
                    buf[k] = (byte) (x * volume);
                }
//                if(addHarmonic) {
//                    double angle2 = (i)/(frequency/hz)*2.0*Math.PI;
//                    buf[1]=(byte)(Math.sin(2*angle2)*volume*0.6);
//                    sdl.write(buf,0,2);
//                } else {
                for (int j = 0; j < repeats; j++) {
                    sdl.write(buf, 0, values.length);
                }
//                }
            }
            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        if (getChart().getPlotCount() == 0) {
            return "no plot available";
        }

        StringBuffer buffer = new StringBuffer();

        //Plot plot = densityChart.getPlot(0);

        ArrayList<ArrayList> traceStates = new ArrayList<ArrayList>();
        ArrayList<ArrayList> traceValues = new ArrayList<ArrayList>();
        int maxLength = 0;

        for (int i = 0; i < getChart().getPlotCount(); i++) {
            Plot plot = getChart().getPlot(i);
            if (i > 0) {
                buffer.append("\t");
            }
            buffer.append("state");
            buffer.append("\t");
            buffer.append(plot.getName());

            traceStates.add(i, new ArrayList(getChart().getTraceStates(i)));
            traceValues.add(i, new ArrayList(getChart().getTraceValues(i)));
            if (traceStates.get(i).size() > maxLength) {
                maxLength = traceStates.get(i).size();
            }
        }
        buffer.append("\n");

        for (int i = 0; i < maxLength; i++) {
            if (traceStates.get(0).size() > i) {
                buffer.append(traceStates.get(0).get(i));
                buffer.append("\t");
                buffer.append(String.valueOf(traceValues.get(0).get(i)));
            } else {
                buffer.append("\t");
            }
            for (int j = 1; j < traceStates.size(); j++) {
                if (traceStates.get(j).size() > i) {
                    buffer.append("\t");
                    buffer.append(traceStates.get(j).get(i));
                    buffer.append("\t");
                    buffer.append(String.valueOf(traceValues.get(j).get(i)));
                } else {
                    buffer.append("\t\t");
                }
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }


}
