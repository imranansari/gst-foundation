/*
 * Copyright 2008 FatWire Corporation. All Rights Reserved.
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
package com.fatwire.gst.foundation.facade.wra;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import COM.FutureTense.Interfaces.ICS;

import com.fatwire.assetapi.data.AssetData;
import com.fatwire.assetapi.data.AssetId;
import com.fatwire.cs.core.db.PreparedStmt;
import com.fatwire.cs.core.db.StatementParam;
import com.fatwire.gst.foundation.core.service.ICSLocatorSupport;
import com.fatwire.gst.foundation.facade.assetapi.AssetDataUtils;
import com.fatwire.gst.foundation.facade.assetapi.AttributeDataUtils;
import com.fatwire.gst.foundation.facade.sql.Row;
import com.fatwire.gst.foundation.facade.sql.SqlHelper;

/**
 * Dao for dealing with core fields in a WRA
 * todo: handle aliases cleanly
 *
 * @author Tony Field
 * @since Jul 21, 2010
 */
public class AliasCoreFieldDao {

    private final ICS ics;

    public AliasCoreFieldDao() {
        this.ics = new ICSLocatorSupport().getICS();
    }

    public AliasCoreFieldDao(ICS ics) {
        this.ics = ics;
    }

    private static final Log LOG = LogFactory.getLog(AliasCoreFieldDao.class);

    /**
     * Return an AssetData object containing the core fields found in an alias asset.
     * <p/>
     * Also includes selected metadata fields:
     * <ul>
     * <li>id</li>
     * <li>name</li>
     * <li>subtype</li>
     * <li>startdate</li>
     * <li>enddate</li>
     * <li>status</li>
     * </ul>
     *
     * @param id id of alias asset
     * @return AssetData containing core fields for Alias asset
     */
    public AssetData getAsAssetData(AssetId id) {
        return AssetDataUtils.getAssetData(id, "metatitle", "metadescription", "metakeyword", 
        		"h1title", "linktitle", "path", "template", "id", "name", "subtype", "startdate", 
        		"enddate", "status", "target", "target_url", "popup", "linktext", "linkimage");
    }

    /**
     * Method to test whether or not an asset is an Alias.
     * todo: optimize as this will be called at runtime
     *
     * @param id asset ID to check
     * @return true if the asset is a valid Alias asset, false if it is not
     */
    public boolean isAlias(AssetId id) {
        try {
            getAlias(id);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Return an alias asset bean given an input id.  Required fields must be set or an exception
     * is thrown.
     *
     * @param id asset id
     * @return Alias
     * @see #isAlias
     */
    public Alias getAlias(AssetId id) {
        AssetData data = getAsAssetData(id);
        AliasBeanImpl o = new AliasBeanImpl();
        // Wra fields
        o.setId(id);
        o.setName(AttributeDataUtils.getWithFallback(data, "name"));
        o.setDescription(AttributeDataUtils.asString(data.getAttributeData("description")));
        o.setSubtype(AttributeDataUtils.asString(data.getAttributeData("subtype")));
        o.setStatus(AttributeDataUtils.asString(data.getAttributeData("status")));
        o.setStartDate(AttributeDataUtils.asDate(data.getAttributeData("startdate")));
        o.setEndDate(AttributeDataUtils.asDate(data.getAttributeData("enddate")));
        o.setMetaTitle(AttributeDataUtils.getWithFallback(data, "metatitle"));
        o.setMetaDescription(AttributeDataUtils.getWithFallback(data, "metadescription"));
        o.setMetaKeyword(AttributeDataUtils.getWithFallback(data, "metakeyword"));
        o.setH1Title(AttributeDataUtils.getWithFallback(data, "h1title"));
        o.setLinkTitle(AttributeDataUtils.getWithFallback(data, "linktitle", "h1title"));
        o.setPath(AttributeDataUtils.getWithFallback(data, "path"));
        o.setTemplate(AttributeDataUtils.getWithFallback(data, "template"));
        // Alias fields
        o.setTarget(AttributeDataUtils.asAssetId(data.getAttributeData("target")));
        o.setTargetUrl(AttributeDataUtils.getWithFallback(data, "target_url"));
        o.setPopup(AttributeDataUtils.asInt(data.getAttributeData("popup")));
        o.setLinkText(AttributeDataUtils.getWithFallback(data, "linktext"));
        o.setLinkImage(AttributeDataUtils.asAssetId(data.getAttributeData("linkimage")));
        return o;
    }

    private static final String ASSETPUBLICATION_QRY = "SELECT p.name from Publication p, AssetPublication ap " + "WHERE ap.assettype = ? " + "AND ap.assetid = ? " + "AND ap.pubid=p.id";
    static final PreparedStmt AP_STMT = new PreparedStmt(ASSETPUBLICATION_QRY, Collections.singletonList("AssetPublication")); // todo:determine why publication cannot fit there.

    static {
        AP_STMT.setElement(0, "AssetPublication", "assettype");
        AP_STMT.setElement(1, "AssetPublication", "assetid");
    }

    public String resolveSite(String c, String cid) {
        final StatementParam param = AP_STMT.newParam();
        param.setString(0, c);
        param.setLong(1, Long.parseLong(cid));
        String result = null;
        for (Row pubid : SqlHelper.select(ics, AP_STMT, param)) {
            if (result != null) {
                LOG.warn("Found asset " + c + ":" + cid + " in more than one publication. It should not be shared; aliases are to be used for cross-site sharing.  Controller will use first site found: " + result);
            } else {
                result = pubid.getString("name");
            }
        }
        return result;
    }


}