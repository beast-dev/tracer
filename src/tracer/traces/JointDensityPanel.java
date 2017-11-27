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

import dr.inference.trace.*;
import jam.framework.Exportable;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Guy Baele
 * @version $Id: CorrelationPanel.java,v 1.1.1.2 2006/04/25 23:00:09 rambaut Exp $
 */
public class JointDensityPanel extends JPanel implements Exportable {

    private final ContinuousJointDensityPanel continuousJointDensityPanel;
    private final DiscreteJointDensityPanel discreteJointDensityPanel;
    private final DiscreteContinuousJointDensityPanel discreteContinuousJointDensityPanel;
    private final GridJointDensityPanel gridJointDensityPanel;

    private TraceChartPanel currentPanel;

    /**
     * Creates new CorrelationPanel
     */
    public JointDensityPanel(final JFrame frame) {
        super();

        continuousJointDensityPanel = new ContinuousJointDensityPanel(frame);
        discreteJointDensityPanel = new DiscreteJointDensityPanel(frame);
        discreteContinuousJointDensityPanel = new DiscreteContinuousJointDensityPanel(frame);
        gridJointDensityPanel = new GridJointDensityPanel(frame);

        setOpaque(false);
        setLayout(new BorderLayout());

        add(new JLabel("No data loaded"), BorderLayout.NORTH);
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        removeAll();

        if (traceLists != null && traceNames != null && traceLists.length * traceNames.size() > 1) {
            if (traceLists.length * traceNames.size() == 2) {
                if ((getTrace(0, traceLists, traceNames).getTraceType().isDiscrete() && getTrace(1, traceLists, traceNames).getTraceType().isContinuous()) ||
                        (getTrace(1, traceLists, traceNames).getTraceType().isDiscrete() && getTrace(0, traceLists, traceNames).getTraceType().isContinuous())) {

                    discreteContinuousJointDensityPanel.setTraces(traceLists, traceNames);
                    setDensityPanel(discreteContinuousJointDensityPanel);

                } else if (getTrace(0, traceLists, traceNames).getTraceType().isDiscrete() && getTrace(1, traceLists, traceNames).getTraceType().isDiscrete()) {

                    discreteJointDensityPanel.setTraces(traceLists, traceNames);
                    setDensityPanel(discreteJointDensityPanel);

                } else if (getTrace(0, traceLists, traceNames).getTraceType().isContinuous() && getTrace(1, traceLists, traceNames).getTraceType().isContinuous()) {

                    continuousJointDensityPanel.setTraces(traceLists, traceNames);
                    setDensityPanel(continuousJointDensityPanel);

                }
            } else if (traceLists.length * traceNames.size() > 2) {

                gridJointDensityPanel.setTraces(traceLists, traceNames);
                setDensityPanel(gridJointDensityPanel);
            }
        } else {
            add(new JLabel("Selected two traces to visualize the joint density."), BorderLayout.CENTER);
            validate();
            repaint();
        }
    }

    protected Trace getTrace(int index, TraceList[] traceLists, java.util.List<String> traceNames) {
        int i = index / traceNames.size();
        int j = index % traceNames.size();

        TraceList traceList = traceLists[i];
        return traceList.getTrace(traceList.getTraceIndex(traceNames.get(j)));
    }


    private void setDensityPanel(TraceChartPanel panel) {
        currentPanel = panel;
        removeAll();
        if (currentPanel != null) {
            add(currentPanel, BorderLayout.CENTER);
        }
        validate();
        repaint();
    }

    public String toString() {
        return currentPanel.toString();
    }

    @Override
    public JComponent getExportableComponent() {
        return currentPanel.getExportableComponent();
    }

}
