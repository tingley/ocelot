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

import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.XLIFFParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.annotation.XLIFFToolAnnotation;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.filters.xliff.Parameters;
import net.sf.okapi.filters.xliff.XLIFFFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse XLIFF file for use in the workbench.
 * The Event list is used when writing out files through Okapi; updates to
 * the workbench segments must then be reflected(synchronized) in the proper Event.
 */
public class OkapiXLIFF12Parser implements XLIFFParser {
    private static Logger LOG = LoggerFactory.getLogger(OkapiXLIFF12Parser.class);
    private LinkedList<Event> events;
    private XLIFFFilter filter;
    private int documentSegmentNum;
    private String sourceLang, targetLang;

    public OkapiXLIFF12Parser() {}

    @Override
    public String getSourceLang() {
        return this.sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    @Override
    public String getTargetLang() {
        return this.targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public List<Event> getSegmentEvents() {
        return this.events;
    }

    @Override
    public List<OcelotSegment> parse(File xliffFile) throws IOException {
        events = new LinkedList<Event>();
        List<OcelotSegment> segments = new ArrayList<OcelotSegment>();
        documentSegmentNum = 1;

        List<String> locales = FileUtil.guessLanguages(xliffFile.getAbsolutePath());
        LocaleId sourceLocale = null, targetLocale = null;
        sourceLocale = (locales.size() >= 1) ?
                LocaleId.fromString(locales.get(0)) : LocaleId.EMPTY;
        // If it's multilingual XLIFF, we use the first one.
        targetLocale = (locales.size() >= 2) ?
                LocaleId.fromString(locales.get(1)) : LocaleId.EMPTY;

        FileInputStream is = new FileInputStream(xliffFile);
        RawDocument fileDoc = new RawDocument(is, "UTF-8", sourceLocale, targetLocale);
        this.filter = new XLIFFFilter();
        Parameters filterParams = new Parameters();
        filterParams.setAddAltTrans(true);
        this.filter.setParameters(filterParams);
        this.filter.open(fileDoc);
        OkapiXLIFF12SegmentHelper helper = new OkapiXLIFF12SegmentHelper(targetLocale);
        while(this.filter.hasNext()) {
            Event event = this.filter.next();
            events.add(event);

            if (event.isStartSubDocument()) {
                StartSubDocument fileElement = (StartSubDocument)event.getResource();
                XLIFFToolAnnotation toolAnn = fileElement.getAnnotation(XLIFFToolAnnotation.class);
                if (toolAnn == null) {
                    toolAnn = new XLIFFToolAnnotation();
                    fileElement.setAnnotation(toolAnn);
                }
                if (toolAnn.get("Ocelot") == null) {
                    toolAnn.add(new XLIFFTool("Ocelot", "Ocelot"), fileElement);
                }
                if (fileElement.getProperty("sourceLanguage") != null) {
                    String fileSourceLang = fileElement.getProperty("sourceLanguage").getValue();
                    if (getSourceLang() != null && !getSourceLang().equals(fileSourceLang)) {
                        LOG.warn("Mismatch between source languages in file elements");
                    }
                    setSourceLang(fileSourceLang);
                    fileDoc.setSourceLocale(LocaleId.fromString(fileSourceLang));
                }
                if (fileElement.getProperty("targetLanguage") != null) {
                    String fileTargetLang = fileElement.getProperty("targetLanguage").getValue();
                    if (getTargetLang() != null && !getTargetLang().equals(fileTargetLang)) {
                        LOG.warn("Mismatch between target languages in file elements");
                    }
                    setTargetLang(fileTargetLang);
                    fileDoc.setTargetLocale(LocaleId.fromString(fileTargetLang));
                    helper.setTargetLocale(fileDoc.getTargetLocale());
                }

            } else if (event.isTextUnit()) {
                ITextUnit tu = (ITextUnit) event.getResource();
                segments.add(helper.convertTextUnitToSegment(tu, documentSegmentNum++,
                                                             sourceLocale));
            }
        }
        is.close();
        return segments;
    }

    protected XLIFFFilter getFilter() {
        return this.filter;
    }
}
