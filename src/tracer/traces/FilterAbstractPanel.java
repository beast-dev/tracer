/*
 * FilterAbstractPanel.java
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

import dr.inference.trace.Filter;

import javax.swing.*;

/**
 * @author Walter Xie
 */
public abstract class FilterAbstractPanel extends JPanel {
    Filter filter;

    abstract String[] getSelectedValues();

    // String[] selV = getSelectedValues();
    // if true, getSelectedValues() returns null equivalent values
    boolean containsNullValue(String[] selV) {
        if (selV == null || selV.length < 1) return true;
        for (String ob : selV) {
            if (ob == null || ob.equals("")) return true;
        }
        return false;
    }
//
//    FilterAbstractPanel getInstance(String[] a, String[] b, TraceFactory.TraceType traceType) {
//        if (traceType == TraceFactory.TraceType.DOUBLE) {
//            return new FilterNumericPanel(a, b);
//        } else {
//            return new FilterDiscretePanel(a, b);
//        }
//    }
}
