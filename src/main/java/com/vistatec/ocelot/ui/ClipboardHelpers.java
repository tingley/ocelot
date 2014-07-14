package com.vistatec.ocelot.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.vistatec.ocelot.segment.model.SegmentVariant;
import com.vistatec.ocelot.segment.model.SegmentVariantSelection;

public class ClipboardHelpers {

    public static final DataFlavor SEGMENT_VARIANT_SELECTION_FLAVOR =
            new DataFlavor(SegmentVariant.class, "text/x-ocelot-segment; class=com.vistatec.ocelot.segment.SegmentVariantSelection");

    public static void copyToClipboard(SegmentVariantSelection variant, ClipboardOwner owner) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new SegmentVariantSelectionTransferable(variant), owner);
    }
    
    public static class SegmentVariantSelectionTransferable implements Transferable {
        private SegmentVariantSelection selection;
        public SegmentVariantSelectionTransferable(SegmentVariantSelection selection) {
            this.selection = selection;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor.equals(SEGMENT_VARIANT_SELECTION_FLAVOR)) {
                return selection;
            }
            if (flavor.equals(DataFlavor.stringFlavor)) {
                return selection.getVariant().getDisplayText()
                        .substring(selection.getSelectionStart(), selection.getSelectionEnd());
            }
            throw new IllegalArgumentException("Unrecognized DataFlavor: " + flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { SEGMENT_VARIANT_SELECTION_FLAVOR, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(SEGMENT_VARIANT_SELECTION_FLAVOR) ||
                   flavor.equals(DataFlavor.stringFlavor);
        }
    }
}
