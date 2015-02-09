/*
 * Copyright (C) 2013, 2014, VistaTEC or third-party contributors as indicated
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
package com.vistatec.ocelot.segment.okapi;

import com.vistatec.ocelot.segment.XLIFFWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.skeleton.GenericSkeleton;

/**
 * Write out XLIFF files using Okapi's XLIFFSkeletonWriter.
 * Handles synchronization between workbench Segments and the Okapi Event list
 * retrieved from the XLIFFParser.
 */
public class OkapiXLIFF12Writer extends OkapiSegmentWriter implements XLIFFWriter {
    private OkapiXLIFF12Parser parser;

    public OkapiXLIFF12Writer(OkapiXLIFF12Parser xliffParser) {
        super();
        this.parser = xliffParser;
    }

    public OkapiXLIFF12Parser getParser() {
        return this.parser;
    }

    @Override
    public void save(File source) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        saveEvents(parser.getFilter(), parser.getSegmentEvents(),
                source.getAbsolutePath(), LocaleId.fromString(parser.getTargetLang()));
    }

    // HACK fix for OC-21.  As of M23, the XLIFF Filter doesn't properly manage
    // ITS namespace insertion for all cases, so we insert it into the <xliff> element
    // if one isn't already present.
    private boolean foundXliffElement = false;
    private static final Pattern XLIFF_ELEMENT_PATTERN = Pattern.compile("(.*<xliff)([^>]*)(>.*)");
    private static final Pattern ITS_NAMESPACE_PATTERN = Pattern.compile("xmlns(:[^=]+)?=\"" + Namespaces.ITS_NS_URI + "\"");

    @Override
    protected DocumentPart preprocessDocumentPart(DocumentPart dp) {
        if (foundXliffElement) return dp;

        String origSkel = dp.getSkeleton().toString();
        Matcher m = XLIFF_ELEMENT_PATTERN.matcher(origSkel);
        if (m.find()) {
            foundXliffElement = true;
            String xliffAttributes = m.group(2);
            Matcher attrM = ITS_NAMESPACE_PATTERN.matcher(xliffAttributes);
            // If we found the namespace, we don't need to change anything
            if (attrM.find()) {
                return dp;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(m.group(1));
            sb.append(m.group(2));
            sb.append(" xmlns:")
              .append(Namespaces.ITS_NS_PREFIX)
              .append("=\"")
              .append(Namespaces.ITS_NS_URI)
              .append("\" ");
            sb.append(m.group(3));
            GenericSkeleton newSkel = new GenericSkeleton(sb.toString());
            dp.setSkeleton(newSkel);
        }
        return dp;
    }
}
