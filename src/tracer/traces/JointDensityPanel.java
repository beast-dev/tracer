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
import jam.framework.Exportable;

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
public class JointDensityPanel extends JPanel implements Exportable {

    private final SinglePairJointDensityPanel singlePairJointDensityPanel;
    private final GridJointDensityPanel gridJointDensityPanel;

    private TraceChartPanel currentPanel;

    /**
     * Creates new CorrelationPanel
     */
    public JointDensityPanel(final JFrame frame) {
        super();

        singlePairJointDensityPanel = new SinglePairJointDensityPanel(frame);
        gridJointDensityPanel = new GridJointDensityPanel(frame);

        setOpaque(false);
        setLayout(new BorderLayout());

        add(new JLabel("No data loaded"), BorderLayout.NORTH);
    }

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        if (traceLists != null && traceNames != null) {
            if ((traceLists.length == 2 && traceNames.size() == 1) ||
                    traceLists.length == 1 && traceNames.size() == 2) {

                singlePairJointDensityPanel.setTraces(traceLists, traceNames);
                setDensityPanel(singlePairJointDensityPanel, null);

            } else if (traceLists.length > 2 || traceNames.size() > 2) {

                gridJointDensityPanel.setTraces(traceLists, traceNames);
                setDensityPanel(gridJointDensityPanel, null);

            }
        } else {
            setDensityPanel(null, "Traces must be of the same type to display together");
        }
    }

    private void setDensityPanel(TraceChartPanel panel, String message) {
        currentPanel = panel;
        removeAll();
        if (currentPanel != null) {
            add(currentPanel, BorderLayout.CENTER);
        }
        if (message != null) {
            add(new JLabel(message), BorderLayout.NORTH);
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
