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
import dr.app.gui.util.CorrelationData;
import dr.inference.trace.*;
import dr.stats.Variate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Guy Baele
 * @version $Id: CorrelationPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class JointDensityPanel extends TraceChartPanel {

    private enum Type {
        BOXPLOT("Boxplot"),
        CORRELATION("Correlation");

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private final String name;
    }

    private TableScrollPane tableScrollPane = new TableScrollPane();

    private JComboBox categoryTableProbabilityCombo = new JComboBox(CategoryTableProbabilityType.values());
    private JCheckBox defaultNumberFormatCheckBox = new JCheckBox("Use default number format");

    private JCheckBox sampleCheckBox = new JCheckBox("Sample only");
    private JCheckBox pointsCheckBox = new JCheckBox("Draw as points");
    private JCheckBox translucencyCheckBox = new JCheckBox("Use translucency");

    private TraceList tl1 = null;
    private TraceList tl2 = null;
    private int traceIndex1 = -1;
    private int traceIndex2 = -1;

    private String name1;
    private String name2;

    private Type currentType;

    private JChart traceChart;
    private final JChartPanel chartPanel;

    private final JChart correlationChart;
    private final JChartPanel correlationChartPanel;
    private final CorrelationData correlationData;

    private ChartSetupDialog chartSetupDialog = null;

    private JToolBar correlationToolBar;
    private JToolBar boxPlotToolBar;

    public enum CategoryTableProbabilityType {
        JOINT_PROBABILITY("Joint Probability"), CONDITIONAL_PROBABILITY_X("Conditional Probability (?|row)"),
        CONDITIONAL_PROBABILITY_Y("Conditional Probability (?|column)"), COUNT("Count");

        CategoryTableProbabilityType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    /**
     * Creates new CorrelationPanel
     */
    public JointDensityPanel(final JFrame frame) {
        super(frame);
        // traceChart only used for Box Plot, ScatterPlot is added by addPlot()
        traceChart = new BoxPlotChart(new LinearAxis(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS),
                new LinearAxis(Axis.AT_MAJOR_TICK_MINUS, Axis.AT_MAJOR_TICK_PLUS));
        chartPanel = new JChartPanel(traceChart, "", "", ""); // xAxisTitle, yAxisTitle

        //correlationChart = new JGridChart();
        correlationChart = new JGridChart(1.0);
        correlationData = new CorrelationData();
        correlationChartPanel = new JChartPanel(correlationChart, "", "", "");

        this.currentType = Type.BOXPLOT;

        correlationToolBar = createCorrelationToolBar(frame);
        boxPlotToolBar = createBoxPlotToolBar(frame);

        setupMainPanel();
    }

    public JChartPanel getChartPanel() {
        if (currentType == Type.BOXPLOT) {
            return chartPanel;
        } else if (currentType == Type.CORRELATION) {
            return correlationChartPanel;
        } else {
            throw new IllegalArgumentException("Unknown chartpanel type");
        }
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(frame, true, true, true, true,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected TraceChartPanel.Settings getSettings() {
        return null;
    }

    @Override
    protected JToolBar getToolBar() {
        if (currentType == Type.BOXPLOT) {
            return boxPlotToolBar;
        } else if (currentType == Type.CORRELATION) {
            return correlationToolBar;
        } else {
            throw new IllegalArgumentException("Unknown chartpanel type");
        }
    }

    protected JChart getChart() {
        if (currentType == Type.BOXPLOT) {
            return traceChart;
        } else if (currentType == Type.CORRELATION) {
            return correlationChart;
        } else {
            throw new IllegalArgumentException("Unknown chart type");
        }
    }

    private JToolBar createCorrelationToolBar(final JFrame frame) {
        JToolBar toolBar = super.createToolBar();

        sampleCheckBox.setOpaque(false);
        sampleCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        // todo make 'samples only' unchecked as default for ordinal types
        sampleCheckBox.setSelected(true);
        toolBar.add(sampleCheckBox);

        pointsCheckBox.setOpaque(false);
        pointsCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        toolBar.add(pointsCheckBox);

        translucencyCheckBox.setOpaque(false);
        translucencyCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        toolBar.add(translucencyCheckBox);

        categoryTableProbabilityCombo.setOpaque(false);
        categoryTableProbabilityCombo.setFont(UIManager.getFont("SmallSystemFont"));
        toolBar.add(categoryTableProbabilityCombo);

        defaultNumberFormatCheckBox.setOpaque(false);
        defaultNumberFormatCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
        defaultNumberFormatCheckBox.setSelected(true);
        toolBar.add(defaultNumberFormatCheckBox);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        ActionListener listener = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                setupTraces();
            }
        };
        sampleCheckBox.addActionListener(listener);
        pointsCheckBox.addActionListener(listener);
        translucencyCheckBox.addActionListener(listener);
        categoryTableProbabilityCombo.addActionListener(listener);
        defaultNumberFormatCheckBox.addActionListener(listener);

        return toolBar;
    }

    private JToolBar createBoxPlotToolBar(final JFrame frame) {
        JToolBar toolBar = super.createToolBar();

        // toolbar empty at the moment...

        return toolBar;
    }

    public void setCombinedTraces() {
        getChartPanel().setXAxisTitle("");
        getChartPanel().setYAxisTitle("");
        messageLabel.setText("Can't show correlation of combined traces");
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        super.setTraces(traceLists, traceNames);

        if (traceLists != null && traceNames != null && traceLists.length == 2 && traceNames.size() == 1) {
            tl1 = traceLists[0];
            name1 = tl1.getName();
            tl2 = traceLists[1];
            name2 = tl2.getName();
            traceIndex1 = tl1.getTraceIndex(traceNames.get(0));
            traceIndex2 = tl2.getTraceIndex(traceNames.get(0));
            name1 = name1 + " - " + tl1.getTraceName(traceIndex1);
            name2 = name2 + " - " + tl2.getTraceName(traceIndex2);
        } else if (traceLists != null && traceNames != null && traceLists.length == 1 && traceNames.size() == 2) {
            tl1 = traceLists[0];
            tl2 = traceLists[0];
            traceIndex1 = tl1.getTraceIndex(traceNames.get(0));
            traceIndex2 = tl2.getTraceIndex(traceNames.get(1));
            name1 = tl1.getTraceName(traceIndex1);
            name2 = tl2.getTraceName(traceIndex2);
        } else {
            tl1 = null;
            tl2 = null;
        }

        setupTraces();
    }

    // it was private void setupChartOrTable()
    protected void setupTraces() {

        if (traceNames != null && traceNames.size() <= 2) {

            //getChartPanel().removeAll();
            if (currentType == Type.CORRELATION) {
                if (!removeAllPlots(false)) return;
            }

            currentType = Type.BOXPLOT;

            //when the list of selected statistics is shortened
            /*if (correlationData.numberOfEntries() > 2 || correlationData.numberOfEntries() == 0) {
                getChartPanel().removeAll();
                getChartPanel().add(traceChart);
            }*/
            ((BoxPlotChart) getChart()).removeAllIntervals();

            if (tl1 == null || tl2 == null) {
//            getChart().removeAllPlots();
                chartPanel.remove(tableScrollPane);

                chartPanel.setXAxisTitle("");
                chartPanel.setYAxisTitle("");
                messageLabel.setText("Select two statistics or traces from the table to view their correlation");
                return;
            }

            TraceCorrelation td1 = tl1.getCorrelationStatistics(traceIndex1);
            TraceCorrelation td2 = tl2.getCorrelationStatistics(traceIndex2);
            if (td1 == null || td2 == null) {
//            getChart().removeAllPlots();
                chartPanel.remove(tableScrollPane);

                chartPanel.setXAxisTitle("");
                chartPanel.setYAxisTitle("");
                messageLabel.setText("Waiting for analysis to complete");
                return;
            }

            messageLabel.setText("");

            if (!td1.getTraceType().isNumber() && !td2.getTraceType().isNumber()) {
                chartPanel.remove(getChart());
                chartPanel.add(tableScrollPane, "Table");

                sampleCheckBox.setVisible(false);
                pointsCheckBox.setVisible(false);
                translucencyCheckBox.setVisible(false);
                categoryTableProbabilityCombo.setVisible(true);
                defaultNumberFormatCheckBox.setVisible(true);

                Object[] rowNames = td1.getRange().toArray();
                Object[] colNames = td2.getRange().toArray();
                double[][] data = categoricalPlot(td1, td2);

                tableScrollPane.setTable(rowNames, colNames, data, defaultNumberFormatCheckBox.isSelected());

            } else {
                chartPanel.remove(tableScrollPane);
                chartPanel.add(getChart(), "Chart");
                //getChart().removeAllPlots();
                categoryTableProbabilityCombo.setVisible(false);
                defaultNumberFormatCheckBox.setVisible(false);

                if (td1.getTraceType().isCategorical()) {
                    mixedCategoricalPlot(td1, td2, false); // isFirstTraceListNumerical

                    sampleCheckBox.setVisible(false);
                    pointsCheckBox.setVisible(false);
                    translucencyCheckBox.setVisible(false);


                } else if (td2.getTraceType().isCategorical()) {
                    mixedCategoricalPlot(td2, td1, true); // isFirstTraceListNumerical

                    sampleCheckBox.setVisible(false);
                    pointsCheckBox.setVisible(false);
                    translucencyCheckBox.setVisible(false);

                    if (!td1.getTraceType().isIntegerOrBinary()) { // do not swap name for TangHuLu plot
                        String swapName = name1;
                        name1 = name2;
                        name2 = swapName;
                    }

                } else {
                    numericalPlot(td1, td2);

                    if (td1.getTraceType().isContinuous() && td2.getTraceType().isContinuous()) {
                        sampleCheckBox.setVisible(true);
                        pointsCheckBox.setVisible(true);
                        translucencyCheckBox.setVisible(true);
                    } else {
                        sampleCheckBox.setVisible(false);
                        pointsCheckBox.setVisible(false);
                        translucencyCheckBox.setVisible(false);
                    }

                    if (!td1.getTraceType().isIntegerOrBinary()) { // do not swap name for TangHuLu plot
                        String swapName = name1;
                        name1 = name2;
                        name2 = swapName;
                    }
                }
            }
            setXLabel(name1);
            setYLabel(name2);

        } else {

            if (currentType == Type.BOXPLOT) {
                getChartPanel().removeAll();
            }

            currentType = Type.CORRELATION;

            //TODO double check what the idea is behind these options
            categoryTableProbabilityCombo.setVisible(false);
            defaultNumberFormatCheckBox.setVisible(false);
            //currently not warning against traces with low ESS values
            messageLabel.setText("");

            if (!removeAllPlots(false)) return;

            //getChartPanel().removeAll();
            //getChartPanel().add(correlationChart);

            correlationData.clear();

            //int i = 0;
            TraceType traceType = null;
            for (TraceList tl : traceLists) {
                for (String traceName : traceNames) {

                    int traceIndex = tl.getTraceIndex(traceName);
                    Trace trace = tl.getTrace(traceIndex);
                    TraceCorrelation td = tl.getCorrelationStatistics(traceIndex);

                    if (trace != null) {
                        String name = tl.getTraceName(traceIndex);
                        if (traceLists.length > 1) {
                            name = tl.getName() + " - " + name;
                        }

                        List values = tl.getValues(traceIndex);

                        // set traceType here to avoid Exception from setYLabel
                        traceType = trace.getTraceType();
                        assert traceType.isContinuous();

                        //collect all traceNames and values while looping here
                        correlationData.add(name, values);
                    }
                }

                //add another routine here for the correlation plot, now that all the data has been collected
                //adding this here and not yet combining data for multiple .log files
                //TODO combine for multiple .log files once it's working for a single .log file
                for (String one : correlationData.getTraceNames()) {
                    for (String two : correlationData.getTraceNames()) {
                        Plot plot = new CorrelationPlot(two, correlationData.getDataForKey(one), correlationData.getDataForKey(two), pointsCheckBox.isSelected(), sampleCheckBox.isSelected(), translucencyCheckBox.isSelected());
                        //plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[0]);
                        getChartPanel().getChart().addPlot(plot);
                    }
                }

            }

            for (int p = 0; p < getChartPanel().getChart().getPlotCount(); p++) {
                ((CorrelationPlot)(getChartPanel().getChart().getPlot(p))).setTotalPlotCount(getChartPanel().getChart().getPlotCount());
            }

        }

        setupMainPanel();

        validate();
        repaint();
    }

    // td is categorical
    private void mixedCategoricalPlot(TraceDistribution tdCategorical, TraceDistribution tdNumerical,
                                      boolean isFirstTraceListNumerical) {
        List<String> categoryValues = tdCategorical.getRange();

        if (categoryValues == null || categoryValues.size() < 1) return;

//        int maxCount = Math.max(tl1.getStateCount(), tl2.getStateCount());
//        int minCount = Math.min(tl1.getStateCount(), tl2.getStateCount());
        // cannot use getStateCount(), because values.size() < getStateCount() if filter is applied
        List values1 = tl1.getValues(traceIndex1);
        List values2 = tl2.getValues(traceIndex2);
        int maxCount = Math.max(values1.size(), values2.size());
        int minCount = Math.min(values1.size(), values2.size());

        int sampleSize = minCount;

        List<Double> samples1 = new ArrayList<Double>(sampleSize);
        int k = 0;

        if (!isFirstTraceListNumerical) {
//            values1 = tl1.getValues(traceIndex1);
//        } else {
            values1 = tl2.getValues(traceIndex2);
        }

        for (int i = 0; i < sampleSize; i++) {
            samples1.add(i, ((Number) values1.get(k)).doubleValue());
            k += minCount / sampleSize;
        }

        List<String> samples2 = new ArrayList<String>(sampleSize);
        k = 0;

        if (!isFirstTraceListNumerical) {
//            values2 = tl2.getValues(traceIndex2);
//        } else {
            values2 = tl1.getValues(traceIndex1);
        }
        for (int i = 0; i < sampleSize; i++) {
            samples2.add(i, values2.get(k).toString());
            k += minCount / sampleSize;
        }

        // set x axis
        getChart().setXAxis(new DiscreteAxis(true, true));
        if (tdNumerical.getTraceType().isIntegerOrBinary()) {
            // samples1 is not real number
            getChart().setYAxis(new DiscreteAxis(true, true));

            List<Double> intData = tdCategorical.indexingData(samples2);
            ScatterPlot plot;
            if (isFirstTraceListNumerical)
                plot  = new TangHuLuPlot(samples1, intData);
            else
                plot  = new TangHuLuPlot(intData, samples1);
            getChart().addPlot(plot);
        } else {
            // samples1 is real number
            drawDiscreteBoxPlot(categoryValues, samples1, samples2);

        }

    }

    private void drawDiscreteBoxPlot(List<String> categoryValues, List<Double> samples1, List<String> samples2) {
        // separate samples into categoryTdMap
        Map<String, TraceDistribution> categoryTdMap = new HashMap<String, TraceDistribution>();
        ArrayList[] sepValues = new ArrayList[categoryValues.size()];
        for (int i = 0; i < categoryValues.size(); i++) {
            sepValues[i] = new ArrayList<Double>();
            for (int j = 0; j < samples2.size(); j++) {
                if (categoryValues.get(i).equals(samples2.get(j))) {
                    sepValues[i].add(samples1.get(j));
                }
            }

            if (sepValues[i].size() > 0) { // avoid RuntimeException: no value sent to statistics calculation
                TraceDistribution categoryTd = new TraceDistribution(sepValues[i], TraceType.REAL); // todo ?
                categoryTdMap.put(categoryValues.get(i), categoryTd);
            }
        }

        // categoryTdMap.size <= categoryValues.size because of sampling
        for (Map.Entry<String, TraceDistribution> entry : categoryTdMap.entrySet()) {
            TraceDistribution categoryTd = entry.getValue();
//                getChart().addIntervals(categoryValue, categoryTd.getMean(), categoryTd.getUpperHPD(), categoryTd.getLowerHPD(), false);
            ((BoxPlotChart)getChart()).addBoxPlots(entry.getKey(), categoryTd.getMedian(), categoryTd.getQ1(),
                    categoryTd.getQ3(), categoryTd.getMinimum(), categoryTd.getMaximum());
        }
    }

    private void numericalPlot(TraceCorrelation td1, TraceCorrelation td2) {
//        int maxCount = Math.max(tl1.getStateCount(), tl2.getStateCount());
//        int minCount = Math.min(tl1.getStateCount(), tl2.getStateCount());
        // cannot use getStateCount(), because values.size() < getStateCount() if filter is applied
        List values1 = tl1.getValues(traceIndex1);
        List values2 = tl2.getValues(traceIndex2);
        int maxCount = Math.max(values1.size(), values2.size());
        int minCount = Math.min(values1.size(), values2.size());

        int sampleSize = minCount;

        // sampling only required by scatter plot
        if (sampleCheckBox.isSelected() && td1.getTraceType().isContinuous() && td2.getTraceType().isContinuous()) {
            if (td1.getESS() < td2.getESS()) {
                sampleSize = (int) td1.getESS();
            } else {
                sampleSize = (int) td2.getESS();
            }
            if (sampleSize < 20) {
                sampleSize = 20;
                messageLabel.setText("One of the traces has an ESS < 20 so a sample size of 20 will be used");
            }
            if (sampleSize > 500) {
                messageLabel.setText("This plot has been sampled down to 500 points");
                sampleSize = 500;
            }
        }

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

        // set axis
        if (td1.getTraceType().isInteger()) {
            getChart().setXAxis(new DiscreteAxis(true, true));
        } else {
            getChart().setXAxis(new LinearAxis());
        }
        if (td2.getTraceType().isInteger()) {
            getChart().setYAxis(new DiscreteAxis(true, true));
        } else {
            getChart().setYAxis(new LinearAxis());
        }

        // add plot
        ScatterPlot plot;
        if (td1.getTraceType().isIntegerOrBinary() && td2.getTraceType().isIntegerOrBinary()) {
            // samples1 samples2 are both ordinal
            plot = new TangHuLuPlot(samples1, samples2);
            getChart().setOriginStyle(null, null);
            getChart().addPlot(plot);

        } else if (td1.getTraceType().isIntegerOrBinary()) {
            List<String> categoryValues = td1.getRange();
            if (categoryValues == null || categoryValues.size() < 1) return;

            List<String> stringList = new ArrayList<String>(samples1.size());
            for (Double ordinal : samples1) {
                stringList.add(String.valueOf(Math.round(ordinal)));
            }

            drawDiscreteBoxPlot(categoryValues, samples2, stringList);

        } else if (td2.getTraceType().isIntegerOrBinary()) {
            List<String> categoryValues = td2.getRange();
            if (categoryValues == null || categoryValues.size() < 1) return;

            List<String> stringList = new ArrayList<String>(samples2.size());
            for (Double ordinal : samples2) {
                stringList.add(String.valueOf(Math.round(ordinal)));
            }
            getChart().setXAxis(new DiscreteAxis(true, true));

            drawDiscreteBoxPlot(categoryValues, samples1, stringList);

        } else {
            // either samples1 or samples2 is real
            plot = new ScatterPlot(samples1, samples2);
            /*plot.setMarkStyle(pointsCheckBox.isSelected() ? Plot.POINT_MARK : Plot.CIRCLE_MARK, pointsCheckBox.isSelected() ? 1.0 : 3.0,
                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER),
                    new Color(16, 16, 64, translucencyCheckBox.isSelected() ? 32 : 255),
                    new Color(16, 16, 64, translucencyCheckBox.isSelected() ? 32 : 255));*/
            plot.setMarkStyle(Plot.CIRCLE_MARK, 3.0,
                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER),
                    new Color(16, 16, 64, 255),
                    new Color(16, 16, 64, 255));
            getChart().addPlot(plot);
        }
    }


    private double[][] categoricalPlot(TraceDistribution td1, TraceDistribution td2) {
        List<String> rowNames = td1.getRange();
        List<String> colNames = td2.getRange();

        double[][] data = new double[rowNames.size()][colNames.size()];

//        int maxCount = Math.max(tl1.getStateCount(), tl2.getStateCount());
//        int minCount = Math.min(tl1.getStateCount(), tl2.getStateCount());
        // cannot use getStateCount(), because values.size() < getStateCount() if filter is applied
        List values1 = tl1.getValues(traceIndex1);
        List values2 = tl2.getValues(traceIndex2);
        int maxCount = Math.max(values1.size(), values2.size());
        int minCount = Math.min(values1.size(), values2.size());

        int sampleSize = minCount;

        if (sampleSize <= 0) System.err.println("sampleSize cannot be 0. sampleSize = " + sampleSize);

        String samples1[] = new String[sampleSize];
        int k = 0;

        TraceType type = tl1.getTrace(traceIndex1).getTraceType();
        for (int i = 0; i < sampleSize; i++) {
            if (type.isInteger()) { // as Integer is stored as Double in Trace
                samples1[i] = Integer.toString( ((Number) values1.get(k)).intValue() );
            } else {
                samples1[i] = values1.get(k).toString();
            }
            k += minCount / sampleSize; // = 1 for non-continous vs non-continous
        }

        String samples2[] = new String[sampleSize];
        k = 0;

        type = tl2.getTrace(traceIndex2).getTraceType();
        for (int i = 0; i < sampleSize; i++) {
            if (type.isInteger()) { // as Integer is stored as Double in Trace
                samples2[i] = Integer.toString( ((Number) values2.get(k)).intValue() );
            } else {
                samples2[i] = values2.get(k).toString();
            }
            k += minCount / sampleSize;
        }

        // calculate count
        for (int i = 0; i < sampleSize; i++) {
            if (rowNames.contains(samples1[i]) && colNames.contains(samples2[i])) {
                data[rowNames.indexOf(samples1[i])][colNames.indexOf(samples2[i])] += 1;
            } else {
//                System.err.println("Not find row or column name. i = " + i);
            }
        }

        if (categoryTableProbabilityCombo.getSelectedItem() == CategoryTableProbabilityType.JOINT_PROBABILITY) {
            for (int r = 0; r < data.length; r++) {
                for (int c = 0; c < data[0].length; c++) {
                    data[r][c] = data[r][c] / sampleSize;
                }
            }
        } else if (categoryTableProbabilityCombo.getSelectedItem() == CategoryTableProbabilityType.CONDITIONAL_PROBABILITY_X) {
            for (int r = 0; r < data.length; r++) {
                double count = 0;
                for (int c = 0; c < data[0].length; c++) {
                    count = count + data[r][c];
                }
                for (int c = 0; c < data[0].length; c++) {
                    if (count != 0)
                        data[r][c] = data[r][c] / count;
                }
            }

        } else if (categoryTableProbabilityCombo.getSelectedItem() == CategoryTableProbabilityType.CONDITIONAL_PROBABILITY_Y) {
            for (int c = 0; c < data[0].length; c++) {
                double count = 0;
                for (int r = 0; r < data.length; r++) {
                    count = count + data[r][c];
                }
                for (int r = 0; r < data.length; r++) {
                    if (count != 0)
                        data[r][c] = data[r][c] / count;
                }
            }

        }
        // else COUNT

        return data;
    }


//    private double[] removeNaN(double[] sample) {
//        List<Double> selectedValuesList = new ArrayList<Double>();
//
//        for (int i = 0; i < sample.length; i++) {
//            if (sample[i] != Double.NaN) {
//                selectedValuesList.add(sample[i]);
//            }
//        }
//
//        double[] dest = new double[selectedValuesList.size()];
//        for (int i = 0; i < dest.length; i++) {
//            dest[i] = selectedValuesList.get(i).doubleValue();
//        }
//
//        return dest;
//    }

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
}
