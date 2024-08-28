/*
 * SummaryStatisticsPanel.java
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

import dr.app.gui.chart.CalendarAxis;
import dr.inference.trace.*;
import jam.framework.Exportable;
import jam.table.TableRenderer;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


public class SummaryStatisticsPanel extends JPanel implements Exportable {

    static final String NAME_ROW = "name";
    static final String MEAN_ROW = "mean";
    static final String MODE_ROW = "mode";
    static final String STDEV_ROW = "stderr of mean";
    static final String STDEV = "stdev";
    static final String MODE_FREQ_ROW = "mode frequency";
    static final String MODE_PROB_ROW = "mode probability";
    static final String VARIANCE_ROW = "variance";
    //    static final String STDEV_VAR_ROW = "stderr of variance";
    static final String GEOMETRIC_MEAN_ROW = "geometric mean";
    static final String MEDIAN_ROW = "median";
    static final String LOWER_UPPER_ROW = "95% HPD interval";
    static final String CRED_SET_ROW = "95% credible set";
    static final String INCRED_SET_ROW = "5% non-credible set";
    static final String ACT_ROW = "auto-correlation time (ACT)";
    static final String ESS_ROW = "effective sample size (ESS)";
    static final String SUM_ESS_ROW = "effective sample size (sum of ESS)";
    static final String NUM_SAMPLES = "number of samples";
    static final String UNIQUE_VALUES = "unique values";
    static final String MIN_MAX = "value range";

    TraceList[] traceLists = null;
    java.util.List<String> traceNames = null;

    StatisticsModel statisticsModel;
    JTable statisticsTable = null;
    JScrollPane scrollPane1 = null;
    JPanel topPanel = null;
    JSplitPane splitPane1 = null;

    int dividerLocation = -1;

    FrequencyPanel frequencyPanel = null;
    IntervalsPanel intervalsPanel = null;
    JComponent currentPanel = null;

    public SummaryStatisticsPanel(final JFrame frame) {

        setOpaque(false);

        statisticsModel = new StatisticsModel();
        statisticsTable = new JTable(statisticsModel);

        statisticsTable.getColumnModel().getColumn(0).setCellRenderer(
                new TableRenderer(SwingConstants.RIGHT, new Insets(0, 4, 0, 4)));
        statisticsTable.getColumnModel().getColumn(1).setCellRenderer(
                new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4)));

        statisticsTable.addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){
                Point p = e.getPoint();
                int row = statisticsTable.rowAtPoint(p);

                if (row > -1 && row < statisticsTable.getRowCount()) {
                    try {
                        Object n = statisticsModel.getValueAt(row,0);
                        Object v = statisticsModel.getValueAt(row,1);
                        if (!v.equals("-")) {
                            statisticsTable.setToolTipText(n + " : " + v);
                        }
                    } catch (RuntimeException e1) {
                        //catch null pointer exception if mouse is over an empty line
                    }
                }

            }//end MouseMoved
        }); // end MouseMotionAdapter

        scrollPane1 = new JScrollPane(statisticsTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        topPanel = new JPanel(new BorderLayout(0, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
                new java.awt.Insets(0, 0, 6, 0)));
        topPanel.add(scrollPane1, BorderLayout.CENTER);

        frequencyPanel = new FrequencyPanel(frame);
        frequencyPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
                new java.awt.Insets(6, 0, 0, 0)));

        intervalsPanel = new IntervalsPanel(frame);
        intervalsPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
                new java.awt.Insets(6, 0, 0, 0)));

        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, frequencyPanel);
        splitPane1.setOpaque(false);
        splitPane1.setBorder(null);
//		splitPane1.setBorder(new BorderUIResource.EmptyBorderUIResource(
//								new java.awt.Insets(12, 12, 12, 12)));

        setLayout(new BorderLayout(0, 0));
        add(splitPane1, BorderLayout.CENTER);

        splitPane1.setDividerLocation(2000);

    }

    private void setupDividerLocation() {

        if (dividerLocation == -1 || dividerLocation == splitPane1.getDividerLocation()) {
            int h0 = topPanel.getHeight();
            int h1 = scrollPane1.getViewport().getHeight();
            int h2 = statisticsTable.getPreferredSize().height;
            dividerLocation = h2 + h0 - h1;

            splitPane1.setDividerLocation(dividerLocation);
        }
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {

        this.traceLists = traceLists;
        this.traceNames = traceNames;

        int divider = splitPane1.getDividerLocation();

        statisticsModel.fireTableStructureChanged();
        if (traceLists != null && traceNames != null) {
            if (traceLists.length == 1 && traceNames.size() == 1) {
                statisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

                currentPanel = frequencyPanel;
                frequencyPanel.setTraces(traceLists, traceNames);
                intervalsPanel.setTraces(null, null);
                splitPane1.setBottomComponent(frequencyPanel);
            } else {
                statisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                statisticsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
                for (int i = 1; i < statisticsTable.getColumnCount(); i++) {
                    statisticsTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                }

                currentPanel = intervalsPanel;
                frequencyPanel.setTraces(null, null);
                intervalsPanel.setTraces(traceLists, traceNames);
                splitPane1.setBottomComponent(intervalsPanel);
            }
        } else {
            currentPanel = statisticsTable;
            frequencyPanel.setTraces(null, null);
            splitPane1.setBottomComponent(frequencyPanel);
        }

        splitPane1.setDividerLocation(divider);

        statisticsTable.getColumnModel().getColumn(0).setCellRenderer(
                new TableRenderer(SwingConstants.RIGHT, new Insets(0, 4, 0, 4)));
        for (int i = 1; i < statisticsTable.getColumnCount(); i++) {
            statisticsTable.getColumnModel().getColumn(i).setCellRenderer(
                    new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4)));
        }

        setupDividerLocation();

        validate();
        repaint();
    }

    public JComponent getExportableComponent() {
        if (currentPanel instanceof Exportable) {
            return ((Exportable) currentPanel).getExportableComponent();
        }
        return currentPanel;
    }

    public String toString() {
        return statisticsModel.toString();
    }

    class StatisticsModel extends AbstractTableModel {

        String[] rowNamesNumbers = {MEAN_ROW, STDEV_ROW, STDEV, VARIANCE_ROW, MEDIAN_ROW, MIN_MAX,
                GEOMETRIC_MEAN_ROW, LOWER_UPPER_ROW, ACT_ROW, ESS_ROW, NUM_SAMPLES};
        String[] rowNamesCategorical = {MODE_ROW, MODE_FREQ_ROW, MODE_PROB_ROW, VARIANCE_ROW, MEDIAN_ROW, UNIQUE_VALUES,
                CRED_SET_ROW, INCRED_SET_ROW, ACT_ROW, ESS_ROW, NUM_SAMPLES};

        public StatisticsModel() {
        }

        private String mixedRowName(String firstRN, String secondRN) {
            return firstRN.equals(secondRN) ? firstRN : firstRN + " | " + secondRN;
        }

        public int getColumnCount() {
            if (traceLists != null && traceNames != null) {
                return (traceLists.length * traceNames.size()) + 1;
            } else {
                return 2;
            }
        }

        public int getRowCount() {
            return rowNamesNumbers.length;
        }

        public Object getValueAt(int row, int col) {

            TraceCorrelation tc = null;
            if (traceLists != null && traceNames != null && traceNames.size() > 0) {
                int colNew = col;
                if (col == 0) {
                    colNew++;
                }
                int n1 = (colNew - 1) / traceNames.size();
                int n2 = (colNew - 1) % traceNames.size();

                TraceList tl = traceLists[n1];
                int index = tl.getTraceIndex(traceNames.get(n2));
                tc = tl.getCorrelationStatistics(index);
            } else {
                return "-";
            }

            if (col == 0) {
                // row names
                if (tc != null) { // traceLists != null && traceNames != null
                    // mixed
                    for (TraceList traceList : traceLists) {
                        for (String traceName : traceNames) {
                            int index = traceList.getTraceIndex(traceName);
                            TraceDistribution td = traceList.getCorrelationStatistics(index);
                            if (td != null) {
                                if (!tc.getTraceType().isNumber() && td.getTraceType().isNumber())
                                    return mixedRowName(rowNamesCategorical[row], rowNamesNumbers[row]);
                                else if (tc.getTraceType().isNumber() && !td.getTraceType().isNumber())
                                    return mixedRowName(rowNamesNumbers[row], rowNamesCategorical[row]);
                            }
                        }
                    }

                    if (!tc.getTraceType().isNumber())
                        return rowNamesCategorical[row]; // only categorical
                }
                return rowNamesNumbers[row]; // only numeric
            }

            double value = 0.0;

            if (tc != null) {
                // if number of sample < MIN_SAMPLE, then return -
                if (tc.getSize() < LogFileTraces.MIN_SAMPLE && row < 9)
                    row = 11;

                if (tc.getTraceType().isNumber()) {
                    switch (row) {
                        case 0:
                            value = tc.getMean();
                            if (frequencyPanel.getChartSetupDialog().displayCalendarDates()) {
                                return TraceAnalysis.formattedNumber(value) + " (" + new CalendarAxis().format(value) + ")";
                            }
                            break;
                        case 1:
                            value = tc.getStdErrorOfMean();
                            break;
                        case 2:
                            value = tc.getStdError();
                            break;
                        case 3:
                            value = tc.getVariance();
                            break;
                        case 4:
                            value = tc.getMedian();
                            if (frequencyPanel.getChartSetupDialog().displayCalendarDates()) {
                                return TraceAnalysis.formattedNumber(value) + " (" + new CalendarAxis().format(value) + ")";
                            }
                            break;
                        case 5:
                            double min = tc.getMinimum();
                            double max = tc.getMaximum();
                            String result1 = "[" + TraceAnalysis.formattedNumber(max) + ", " + TraceAnalysis.formattedNumber(max) + "]";
                            if (frequencyPanel.getChartSetupDialog().displayCalendarDates()) {
                                result1 = result1 + " ([" + new CalendarAxis().format(min) + ", " + new CalendarAxis().format(min) + "])";
                            }
                            return result1;
                        case 6:
                            if (!tc.hasGeometricMean()) return "n/a";
                            value = tc.getGeometricMean();
                            if (frequencyPanel.getChartSetupDialog().displayCalendarDates()) {
                                return new CalendarAxis().format(value);
                            }
                            break;
                        case 7:
                            if (tc.isConstant()) return "n/a";
                            double lb = tc.getLowerHPD();
                            double ub = tc.getUpperHPD();
                            String result2 = "[" + TraceAnalysis.formattedNumber(lb) + ", " + TraceAnalysis.formattedNumber(ub) + "]";
                            if (frequencyPanel.getChartSetupDialog().displayCalendarDates()) {
                                result2 = result2 +  " ([" + new CalendarAxis().format(lb) + ", " + new CalendarAxis().format(ub) + "])";
                            }
                            return result2;
                        case 8:
                            if (tc.isConstant()) return "n/a";
                            value = tc.getACT();
                            break;
                        case 9:
                            if (tc.isConstant()) return "n/a";
                            value = tc.getESS();
                            // only need 1 dp for ESS
                            return TraceAnalysis.formattedNumber(value, 1);
                        case 10:
                            value = tc.getSize();
                            break;
                        case 11:
                            return "-";
                    } // END switch
                } else{
                    // categorical
                    switch (row) {
                        case 0:
                            return tc.valueToString(tc.getMode());
                        case 1:
                            return tc.getFrequencyOfMode();
                        case 2:
                            value = tc.getProbabilityOfMode();
                            break;
                        case 3:
                        case 4:
                            return "n/a";
                        case 5:
                            return tc.setToString(tc.getValueSet());
                        case 6:
                            return tc.setToString(tc.getCredibleSet());
                        case 7:
                            return tc.setToString(tc.getIncredibleSet());
                        case 8:
                            if (tc.isConstant()) return "n/a";
                            value = tc.getACT();
                            if (Double.isNaN(value)) return "n/a";
                            break;
                        case 9:
                            if (tc.isConstant()) return "n/a";
                            value = tc.getESS();
                            if (Double.isNaN(value)) return "n/a";
                            // only need 1 dp for ESS
                            return TraceAnalysis.formattedNumber(value, 1);
                        case 10:
                            value = tc.getSize();
                            break;
                        case 11:
                            return "-";
                    }
                }
            } else {
                return "-";
            }

            return TraceAnalysis.formattedNumber(value);
        }

        public String getColumnName(int column) {
            if (column == 0) return "Summary Statistic";
            if (traceLists != null && traceNames != null) {
                int n1 = (column - 1) / traceNames.size();
                int n2 = (column - 1) % traceNames.size();
                String columnName = "";
                if (traceLists.length > 1) {
                    columnName += traceLists[n1].getName();
                    if (traceNames.size() > 1) {
                        columnName += ": ";
                    }
                }
                if (traceNames.size() > 0) {
                    columnName += traceNames.get(n2);
                }
                return columnName;
            }
            return "-";
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(getColumnName(0));
            for (int j = 1; j < getColumnCount(); j++) {
                buffer.append("\t");
                buffer.append(getColumnName(j));
            }
            buffer.append("\n");

            for (int i = 0; i < getRowCount(); i++) {
                buffer.append(getValueAt(i, 0));
                for (int j = 1; j < getColumnCount(); j++) {
                    buffer.append("\t");
                    buffer.append(getValueAt(i, j));
                }
                buffer.append("\n");
            }

            return buffer.toString();
        }
    }

}
