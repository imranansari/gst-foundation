/*
 * Copyright 2010 FatWire Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fatwire.gst.foundation.taglib;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.jsp.JspException;

import COM.FutureTense.Interfaces.ICS;

import com.fatwire.assetapi.data.AssetId;
import com.fatwire.gst.foundation.facade.assetapi.asset.ScatteredAssetAccessTemplate;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Tony Field
 * @since 2011-09-20
 */
public final class GetTagsTag extends GsfSimpleTag {

    private String c;
    private long cid;
    private String name;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    @Override
    public void doTag() throws JspException, IOException {

        final ICS ics = getICS();
        final ScatteredAssetAccessTemplate t = new ScatteredAssetAccessTemplate(ics);
        // todo: medium: find in page context if it's there (due to presence of
        // gsf:root


        if (StringUtils.isBlank(c) || cid == 0) {
            String tag = (String) t.readCurrent("gsttag").get("gsttag");
            if (tag != null) {
                getJspContext().setAttribute(name, Arrays.asList(tag.split(",")));
            }
        } else {
            final AssetId id = t.createAssetId(c, cid);
            String tag = (String) t.read(id, "gsttag").get("gsttag");
            if (tag != null) {
                getJspContext().setAttribute(name, Arrays.asList(tag.split(",")));
            }
        }

        super.doTag();
    }

    /**
     * @param c the c to set
     */
    public void setC(final String c) {
        this.c = c;
    }

    /**
     * @param cid the cid to set
     */
    public void setCid(final String cid) {
        this.cid = Long.parseLong(cid);
    }

    /**
     * @param cid the cid to set
     */
    public void setCid(final long cid) {
        this.cid = cid;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
}