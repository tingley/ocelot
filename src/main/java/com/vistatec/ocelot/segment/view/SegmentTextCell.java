/*
 * Copyright (C) 2013-2015, VistaTEC or third-party contributors as indicated
 * by the @author tags or express copyright attribution statements applied by
 * the authors. All third-party contributions are distributed under license by
 * VistaTEC.
 *
 * This file is part of Ocelot.
 *
 * Ocelot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ocelot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, write to:
 *
 *     Free Software Foundation, Inc.
 *     51 Franklin Street, Fifth Floor
 *     Boston, MA 02110-1301
 *     USA
 *
 * Also, see the full LGPL text here: <http://www.gnu.org/copyleft/lesser.html>
 */
package com.vistatec.ocelot.segment.view;

import com.vistatec.ocelot.segment.model.SegmentVariant;
import com.vistatec.ocelot.segment.model.SegmentVariantSelection;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.InputMethodEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vistatec.ocelot.ui.ClipboardHelpers;

/**
 * Representation of source/target segment text in segment table view.
 * Handles the style of the text with Inline tags and the link between
 * the editor behavior and the underlying data structure.
 */
public class SegmentTextCell extends JTextPane implements ClipboardOwner {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = LoggerFactory.getLogger(SegmentTextCell.class);
    public static final String tagStyle = "tag", regularStyle = "regular",
            insertStyle = "insert", deleteStyle = "delete", enrichedStyle = "enriched", highlightStyle="highlight", currHighlightStyle="currHighlight";
    private SegmentVariant v;
    private int row;
    
    private boolean inputMethodChanged;

    // Shared styles table
    private static final StyleContext styles = new StyleContext();
    static {
        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = styles.addStyle(regularStyle, style);

        Style s = styles.addStyle(tagStyle, regular);
        StyleConstants.setBackground(s, Color.LIGHT_GRAY);

        Style insert = styles.addStyle(insertStyle, s);
        StyleConstants.setForeground(insert, Color.BLUE);
        StyleConstants.setUnderline(insert, true);

        Style delete = styles.addStyle(deleteStyle, insert);
        StyleConstants.setForeground(delete, Color.RED);
        StyleConstants.setStrikeThrough(delete, true);
        StyleConstants.setUnderline(delete, false);
        
        Style highlight = styles.addStyle(highlightStyle, regular);
        StyleConstants.setBackground(highlight, Color.yellow);
        
        Style currHighlight = styles.addStyle(currHighlightStyle, regular);
        StyleConstants.setBackground(currHighlight, Color.green);
    }

    /**
     * Create a dummy cell for the purposes of cell sizing.  This cell
     * doesn't contain the style information and isn't linked to any of
     * the control logic.
     * @return dummy cell
     */
    public static SegmentTextCell createDummyCell() {
        return new SegmentTextCell();
    }

    /**
     * Create an empty cell for the purpose of holding live content. This
     * cell contains style information and is linked to the document.
     * @return real cell
     */
    public static SegmentTextCell createCell() {
        return new SegmentTextCell(styles);
    }

    /**
     * Create an empty cell holding the specified content. This
     * cell contains style information and is linked to the document.
     * @param v
     * @param raw
     * @param isBidi whether the cell contains bidi content
     * @return
     */
    public static SegmentTextCell createCell(SegmentVariant v, int row, boolean raw, boolean isBidi) {
        return new SegmentTextCell(v, row, raw, isBidi);
    }

    private SegmentTextCell(StyleContext styleContext) {
        super(new DefaultStyledDocument(styleContext));
        setEditController();
        addCaretListener(new TagSelectingCaretListener());
    }

    private SegmentTextCell() {
        super();
    }

    private SegmentTextCell(SegmentVariant v, int row, boolean raw, boolean isBidi) {
        this(styles);
        setVariant(v, raw);
        setRow(row);
        setBidi(isBidi);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setBidi(boolean isBidi) {
        if (isBidi) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
    }

    /**
     * A caret listener that detects selections that encompass
     * only part of tags and automatically expand the selection
     * to include full tags.  This produces cascading CaretUpdate
     * events, but the cycle should stop after a single additional
     * update.
     */
    class TagSelectingCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            if (e.getDot() != e.getMark()) {
                int origStart = Math.min(e.getDot(), e.getMark());
                int origEnd = Math.max(e.getDot(), e.getMark());
                int start = v.findSelectionStart(origStart);
                int end = v.findSelectionEnd(origEnd);
                if (start != origStart) {
                    setSelectionStart(start);
                }
                if (end != origEnd) {
                    setSelectionEnd(end);
                }
            }
        }
    }

    public final void setEditController() {
        StyledDocument styledDoc = getStyledDocument();
        if (styledDoc instanceof AbstractDocument) {
            AbstractDocument doc = (AbstractDocument)styledDoc;
            doc.setDocumentFilter(new SegmentFilter());
        }
    }

    public final void setDisplayCategories() {
        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyledDocument styleDoc = this.getStyledDocument();
        Style regular = styleDoc.addStyle(regularStyle, style);

        Style highlight = styleDoc.addStyle(highlightStyle, regular);
        StyleConstants.setBackground(highlight, Color.yellow);
        
        Style currHighlight = styleDoc.addStyle(currHighlightStyle, regular);
        StyleConstants.setBackground(currHighlight, Color.green);
        
        Style s = styleDoc.addStyle(tagStyle, regular);
        StyleConstants.setBackground(s, Color.LIGHT_GRAY);

        Style insert = styleDoc.addStyle(insertStyle, s);
        StyleConstants.setForeground(insert, Color.BLUE);
        StyleConstants.setUnderline(insert, true);

        Style delete = styleDoc.addStyle(deleteStyle, insert);
        StyleConstants.setForeground(delete, Color.RED);
        StyleConstants.setStrikeThrough(delete, true);
        StyleConstants.setUnderline(delete, false);
        
        Style enriched = styleDoc.addStyle(enrichedStyle, regular);
        StyleConstants.setForeground(enriched, Color.BLUE);
        StyleConstants.setUnderline(enriched, true);
        
    }

    public void setTextPane(List<String> styledText) {
        StyledDocument doc = this.getStyledDocument();
        try {
            for (int i = 0; i < styledText.size(); i += 2) {
                doc.insertString(doc.getLength(), styledText.get(i),
                        doc.getStyle(styledText.get(i + 1)));
            }
        } catch (BadLocationException ex) {
            LOG.error("Error rendering text", ex);
        }
    }

    public SegmentVariant getVariant() {
        return this.v;
    }

    public final void setVariant(SegmentVariant v, boolean raw) {
        this.v = v;
        if (v != null) {
            setTextPane(v.getStyleData(raw));
        }
        else {
            setTextPane(new ArrayList<String>());
        }
    }

    public void setTargetDiff(List<String> targetDiff) {
        setTextPane(targetDiff);
    }

    @Override
    public void copy() {
        ClipboardHelpers.copyToClipboard(new SegmentVariantSelection(getRow(),
                getVariant().createCopy(), getSelectionStart(), getSelectionEnd()), this);
        // XXX Currently this lets you select part of a tag.  Seems weird, no?
        // TODO also handle 'cut'
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            System.out.println("Lost ownership of " + contents.getTransferData(ClipboardHelpers.SEGMENT_VARIANT_SELECTION_FLAVOR));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // TODO: use getAvailableDataFlavors
        try {
            SegmentVariantSelection selection = (SegmentVariantSelection)clipboard.getData(ClipboardHelpers.SEGMENT_VARIANT_SELECTION_FLAVOR);
            System.out.println("Pasting " + selection);
            if (row != selection.getRow()) {
                System.out.println("Pasting to a different row...");
            }
            else {
                System.out.println("Pasting from within same row..");
                // XXX Move this code into a different place
                // XXX This will flatten codes to text, which is probably wrong... should
                // I just ignore those codes?
                String pasted = selection.getVariant().getDisplayText().substring(
                        selection.getSelectionStart(), selection.getSelectionEnd());
                int pastedLength = selection.getSelectionEnd() - selection.getSelectionStart();
                // Get indexes in the selected text
                // I will need to make sure it's insertable, etc
                System.out.println("Pasting '" + pasted + "'" + " into row " + getRow() + " at [" + 
                        getSelectionStart() + ", " + getSelectionEnd() + "]");

                getVariant().replaceSelection(getSelectionStart(), getSelectionEnd(), selection);
                int cursorPos = getSelectionEnd() + pastedLength;
                setTextPane(getVariant().getStyleData(false));
                setSelectionStart(cursorPos);
                setSelectionEnd(cursorPos);
            }
            // TODO: need to set it somehow
            // Cases
            // - If this is not the same row, just paste the plain text
            // - If this is the same row, we can paste placeholders, as long as we remove the other ones
            //    --- How does this work?
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.text.JTextComponent#processInputMethodEvent(java.awt.event
     * .InputMethodEvent)
     */
    @Override
    protected void processInputMethodEvent(InputMethodEvent e) {
        /*
         * Some keyboards, such as Traditional Chinese keyboard, trigger the
         * INPUT_METHOD_TEXT_CHANGED event while typing text. This event causes
         * the remove method in the DocumentFilter to be invoked, resulting in
         * some characters erroneously deleted. The inputMethodChanged field
         * value is set to true in case this event is triggered. This field is
         * then checked within the remove method, and the characters are
         * actually removed only if this field is false.
         */
        inputMethodChanged = e.getID() == InputMethodEvent.INPUT_METHOD_TEXT_CHANGED;
        super.processInputMethodEvent(e);
    }



	/**
     * Handles edit behavior in segment text cell.
     */
    public class SegmentFilter extends DocumentFilter {

        // This is also called when initially populating the table,
        // as swing will try to "remove" the old contents.
        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {

            if (v != null) {
                // Disallow tag deletions
                if (!v.containsTag(offset, length)) {
                    // Remove from cell editor
                    super.remove(fb, offset, length);
    
                    if(!inputMethodChanged){
	                    // Remove from underlying segment structure
	                    deleteChars(offset, length);
                    }
                }
            }
            else {
                // TODO: why does this correct the spacing issue?
                super.remove(fb, offset, length);
            }
            inputMethodChanged = false;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str,
                AttributeSet a) throws BadLocationException {
            if (length > 0) {
                if (!v.containsTag(offset, length)) {
                    // Remove from cell editor
                    super.replace(fb, offset, length, str, a);

                    // Remove from underlying segment structure
                    v.modifyChars(offset, length, str);
                }
            } else {
                if (v.canInsertAt(offset)) {
                    // Insert string into cell editor.
                    super.replace(fb, offset, length, str, a);

                    insertChars(str, offset);
                }
            }
            inputMethodChanged = false;

        }

        public void deleteChars(int offset, int charsToRemove) {
            v.modifyChars(offset, charsToRemove, null);
        }

        public void insertChars(String insertText, int offset) {
            v.modifyChars(offset, 0, insertText);
        }
    }
}
