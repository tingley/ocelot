package com.spartansoftwareinc.vistatec.rwb.segment;

import com.spartansoftwareinc.vistatec.rwb.rules.DataCategoryFlag;
import com.spartansoftwareinc.vistatec.rwb.rules.NullDataCategoryFlag;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

/**
 * Table model representing the aligned source/target segments and the flags
 * shown in the SegmentView.
 */
public class SegmentTableModel extends AbstractTableModel {

    private SegmentController segmentController;
    private LinkedList<Segment> segments = new LinkedList<Segment>();
    protected HashMap<String, Integer> colNameToIndex;
    protected HashMap<Integer, String> colIndexToName;
    public static final int NUMFLAGS = 5;
    public static final int NONFLAGCOLS = 4;
    public static final String COLSEGNUM = "#";
    public static final String COLSEGSRC = "source";
    public static final String COLSEGTGT = "target";
    public static final String COLSEGTGTORI = "target ori";

    public SegmentTableModel(SegmentController segController) {
        segmentController = segController;
        colNameToIndex = new HashMap<String, Integer>();
        colNameToIndex.put(COLSEGNUM, 0);
        colNameToIndex.put(COLSEGSRC, 1);
        colNameToIndex.put(COLSEGTGT, 2);
        colNameToIndex.put(COLSEGTGTORI, 3);
        colIndexToName = new HashMap<Integer, String>();
        for (String key : colNameToIndex.keySet()) {
            colIndexToName.put(colNameToIndex.get(key), key);
        }
    }

    @Override
    public String getColumnName(int col) {
        return col < NONFLAGCOLS ? colIndexToName.get(col) : "";
    }

    public int getColumnIndex(String col) {
        return colNameToIndex.get(col);
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == getColumnIndex(COLSEGNUM)) {
            return Integer.class;
        }
        if (columnIndex == getColumnIndex(COLSEGSRC)
                || columnIndex == getColumnIndex(COLSEGTGT)
                || columnIndex == getColumnIndex(COLSEGTGTORI)) {
            return String.class;
        }
        return DataCategoryFlag.class;
    }

    @Override
    public int getRowCount() {
        return segments.size();
    }

    @Override
    public int getColumnCount() {
        return NONFLAGCOLS + NUMFLAGS;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == getColumnIndex(COLSEGNUM)) {
            return getSegment(row).getSegmentNumber();
        }
        if (col == getColumnIndex(COLSEGSRC)) {
            return getSegment(row).getSource().getCodedText();
        }
        if (col == getColumnIndex(COLSEGTGT)) {
            return getSegment(row).getTarget().getCodedText();
        }
        if (col == getColumnIndex(COLSEGTGTORI)) {
            return getSegment(row).getOriginalTarget().getCodedText();
        }
        Object ret = segmentController.getRuleConfig().getTopDataCategory(
                segments.get(row), col-NONFLAGCOLS);
        return ret != null ? ret : new NullDataCategoryFlag();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == colNameToIndex.get(COLSEGTGT);
    }

    public void addSegment(Segment seg) {
        segments.add(seg);
    }

    public Segment getSegment(int row) {
        return segments.get(row);
    }

    protected void deleteSegments() {
        segments.clear();
        segmentController.notifyDeletedSegments();
    }
}
