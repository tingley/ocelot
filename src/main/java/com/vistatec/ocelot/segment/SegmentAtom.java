package com.vistatec.ocelot.segment;

/**
 * An immutable unit of text/tag content.
 */
public interface SegmentAtom {
    /**
     * Length of this display data.
     * @return
     */
    int getLength();

    /**
     * This is display data.
     */
    String getData();

    String getTextStyle();
}
