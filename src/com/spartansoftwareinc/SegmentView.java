package com.spartansoftwareinc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Table view containing the source and target segments extracted from the
 * opened file. Indicates attached LTS metadata as flags.
 */
public class SegmentView extends JScrollPane {

    protected JTable sourceTargetTable;
    private SegmentTableModel segments;
    private ListSelectionModel tableSelectionModel;
    private SegmentAttributeView attrView;
    private TableColumnModel tableColumnModel;
    protected LinkedList<Integer[]> rowHeights = new LinkedList<Integer[]>();

    public SegmentView(SegmentAttributeView attr) {
        attrView = attr;

        segments = new SegmentTableModel();
        sourceTargetTable = new JTable(segments);
        sourceTargetTable.getTableHeader().setReorderingAllowed(false);

        tableColumnModel = sourceTargetTable.getColumnModel();
        tableColumnModel.addColumnModelListener(new TableColumnModelListener() {

            @Override
            public void columnAdded(TableColumnModelEvent tcme) {}

            @Override
            public void columnRemoved(TableColumnModelEvent tcme) {}

            @Override
            public void columnMoved(TableColumnModelEvent tcme) {}

            @Override
            public void columnMarginChanged(ChangeEvent ce) {
                segments.updateRowHeights();
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent lse) {}
        });


        tableSelectionModel = sourceTargetTable.getSelectionModel();
        tableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSelectionModel.addListSelectionListener(new SegmentSelectionHandler());

        DefaultTableCellRenderer segNumAlign = new DefaultTableCellRenderer();
        segNumAlign.setHorizontalAlignment(JLabel.LEFT);
        segNumAlign.setVerticalAlignment(JLabel.TOP);
        sourceTargetTable.setDefaultRenderer(Integer.class, segNumAlign);
        sourceTargetTable.setDefaultRenderer(DataCategoryFlag.class,
                new DataCategoryFlagRenderer());
        sourceTargetTable.setDefaultRenderer(String.class,
                new SegmentTextRenderer());

        tableColumnModel.getColumn(0).setMinWidth(15);
        tableColumnModel.getColumn(0).setPreferredWidth(20);
        tableColumnModel.getColumn(0).setMaxWidth(50);
        int flagMinWidth = 15, flagPrefWidth = 15, flagMaxWidth = 20;
        for (int i = SegmentTableModel.NONFLAGCOLS;
             i < SegmentTableModel.NONFLAGCOLS+SegmentTableModel.NUMFLAGS; i++) {
            tableColumnModel.getColumn(i).setMinWidth(flagMinWidth);
            tableColumnModel.getColumn(i).setPreferredWidth(flagPrefWidth);
            tableColumnModel.getColumn(i).setMaxWidth(flagMaxWidth);
        }
        setViewportView(sourceTargetTable);
    }

    public void parseSegmentsFromFile() throws IOException {
        sourceTargetTable.clearSelection();
        clearSegments();
        attrView.clearTree();
        // TODO: Actually parse the file and retrieve segments/metadata.
        InputStream sampleEnglishDocStream =
                SegmentView.class.getResourceAsStream("sample_english.txt");
        BufferedReader sampleEnglishDoc =
                new BufferedReader(new InputStreamReader(sampleEnglishDocStream, "UTF-8"));

        InputStream sampleFrenchDocStream =
                SegmentView.class.getResourceAsStream("sample_french.txt");
        BufferedReader sampleFrenchDoc =
                new BufferedReader(new InputStreamReader(sampleFrenchDocStream, "UTF-8"));

        int documentSegNum = 1;
        String nextEnglishLine, nextFrenchLine;
        while ((nextEnglishLine = sampleEnglishDoc.readLine()) != null
                && (nextFrenchLine = sampleFrenchDoc.readLine()) != null) {
            Segment seg = new Segment(documentSegNum++, nextEnglishLine, nextFrenchLine);
            for (int i = 0; i < 5; i++) {
                double addChance = Math.random();
                if (addChance < 0.6) {
                    seg.addLQI(generateRandomIssue());
                }
            }
            segments.addSegment(seg);
            initializeRowHeight(seg);
        }

        // Adjust the segment number column width
        tableColumnModel.getColumn(
                segments.getColumnIndex(SegmentTableModel.COLSEGNUM))
                .setPreferredWidth(this.getFontMetrics(this.getFont())
                .stringWidth(" " + documentSegNum));

        setViewportView(sourceTargetTable);
    }

    public void initializeRowHeight(Segment seg) {
        Integer[] rowHeight = new Integer[SegmentTableModel.NONFLAGCOLS + SegmentTableModel.NUMFLAGS];

        int srcIdx = segments.getColumnIndex(SegmentTableModel.COLSEGSRC);
        int tgtIdx = segments.getColumnIndex(SegmentTableModel.COLSEGTGT);

        JTextArea textMeasure = new JTextArea();
        textMeasure.setLineWrap(true);
        textMeasure.setWrapStyleWord(true);

        int srcColWidth = tableColumnModel.getColumn(srcIdx).getWidth();
        textMeasure.setText(seg.getSource());
        textMeasure.setSize(new Dimension(srcColWidth, 1));
        rowHeight[srcIdx] = textMeasure.getPreferredSize().height;

        int tgtColWidth = tableColumnModel.getColumn(tgtIdx).getWidth();
        textMeasure.setText(seg.getTarget());
        textMeasure.setSize(new Dimension(tgtColWidth, 1));
        rowHeight[tgtIdx] = textMeasure.getPreferredSize().height;

        rowHeights.add(rowHeight);
    }

    private void clearSegments() {
        segments.deleteSegments();
    }

    private LanguageQualityIssue generateRandomIssue() {
        String[] types = {"terminology", "mistranslation", "omission",
            "untranslated", "addition", "duplication", "inconsistency",
            "grammar", "legal", "register", "locale-specific-content",
            "locale-violation", "style", "characters", "misspelling",
            "typographical", "formatting", "inconsistent-entities", "numbers",
            "markup", "pattern-problem", "whitespace", "internationalization",
            "length", "uncategorized", "other"};
        LanguageQualityIssue lqi = new LanguageQualityIssue();
        lqi.setType(types[(int) Math.floor(Math.random() * 26)]);
        lqi.setComment("testing");
        lqi.setSeverity((int) Math.round(Math.random() * 100));
        return lqi;
    }

    class SegmentSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.getMaxSelectionIndex() == lsm.getMinSelectionIndex() &&
                lsm.getMinSelectionIndex() >= 0) {
                attrView.setSelectedSegment(
                        segments.getSegment(lsm.getMinSelectionIndex()));
            } else {
                // TODO: Log non-single selection error
            }
        }
    }

    class SegmentTableModel extends AbstractTableModel {
        private LinkedList<Segment> segments = new LinkedList<Segment>();
        protected HashMap<String, Integer> colNameToIndex;
        protected HashMap<Integer, String> colIndexToName;
        private static final int NUMFLAGS = 5;
        private static final int NONFLAGCOLS = 3;
        private static final String COLSEGNUM = "#";
        private static final String COLSEGSRC = "source";
        private static final String COLSEGTGT = "target";

        public SegmentTableModel() {
            colNameToIndex = new HashMap<String, Integer>();
            colNameToIndex.put(COLSEGNUM, 0);
            colNameToIndex.put(COLSEGSRC, 1);
            colNameToIndex.put(COLSEGTGT, 2);
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
            if (columnIndex == getColumnIndex(COLSEGSRC) ||
                columnIndex == getColumnIndex(COLSEGTGT)) {
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
                return getSegment(row).getSource();
            }
            if (col == getColumnIndex(COLSEGTGT)) {
                return getSegment(row).getTarget();
            }
            Object ret = segments.get(row).getTopDataCategory(col - NONFLAGCOLS);
            return ret != null ? ret : new NullDataCategoryFlag();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void addSegment(Segment seg) {
            segments.add(seg);
        }

        public Segment getSegment(int row) {
            return segments.get(row);
        }

        private void deleteSegments() {
            segments.clear();
        }

        protected void updateRowHeights() {
            for (int row = 0; row < getRowCount(); row++) {
                FontMetrics font = sourceTargetTable.getFontMetrics(sourceTargetTable.getFont());
                int rowHeight = font.getHeight();
                for (int col = 0; col < getColumnCount(); col++) {
                    if (rowHeights.get(row)[col] != null) {
                        rowHeight = Math.max(rowHeight, rowHeights.get(row)[col]);
                    }
                }
                sourceTargetTable.setRowHeight(row, rowHeight);
            }
        }
    }

    public class SegmentTextRenderer extends JTextArea implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean hasFocus, int row, int col) {
            String text = (String) o;
            setLineWrap(true);
            setWrapStyleWord(true);
            setText(text);
            setBackground(isSelected ? jtable.getSelectionBackground() : jtable.getBackground());
            setForeground(isSelected ? jtable.getSelectionForeground() : jtable.getForeground());

            // Need to set width to force text area to calculate a pref height
            setSize(new Dimension(jtable.getColumnModel().getColumn(col).getWidth(), jtable.getRowHeight(row)));
            rowHeights.get(row)[col] = getPreferredSize().height;
            return this;
        }
    }

    public class DataCategoryFlagRenderer extends JLabel implements TableCellRenderer {

        public DataCategoryFlagRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object obj, boolean bln, boolean bln1, int row, int col) {
            DataCategoryFlag flag = (DataCategoryFlag) obj;
            setBackground(flag.getFlagBackgroundColor());
            setBorder(flag.getFlagBorder());
            setText(flag.getFlagText());
            setHorizontalAlignment(CENTER);
            return this;
        }
    }

    public class NullDataCategoryFlag implements DataCategoryFlag {

        @Override
        public Color getFlagBackgroundColor() {
            return null;
        }

        @Override
        public Border getFlagBorder() {
            return null;
        }

        @Override
        public String getFlagText() {
            return "";
        }

        @Override
        public String toString() {
            return "";
        }
    }
}