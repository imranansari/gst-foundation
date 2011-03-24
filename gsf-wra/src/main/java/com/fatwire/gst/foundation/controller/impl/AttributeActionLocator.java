/*
 * Copyright 2011 FatWire Corporation. All Rights Reserved.
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
package com.fatwire.gst.foundation.controller.impl;

import COM.FutureTense.Interfaces.ICS;
import COM.FutureTense.Util.ftErrors;

import com.fatwire.gst.foundation.CSRuntimeException;
import com.fatwire.gst.foundation.controller.Action;
import com.fatwire.gst.foundation.controller.ActionLocator;
import com.fatwire.gst.foundation.controller.ICSAware;

/**
 * ActionLocator that looks at an attribute of a WRA and retrieves the 'gstaction' attribute, then determines
 * what to do.
 *
 * @author Tony Field
 * @since Mar 24, 2011
 */
final class AttributeActionLocator implements ActionLocator {
    public static final String GST_ACTION_ATTR_NAME = "gstaction";
    public static final String ACTION_TYPE_SPRING_BEAN_PREFIX = "spring-bean:";

    public Action getAction(ICS ics) {
        String attribute = getAttribute(ics);
        Action result = getAction(attribute);

        if (result == null) {
            throw new CSRuntimeException("No action configured for attribute: " + attribute, ftErrors.badparams);
        }
        if (ICSAware.class.isAssignableFrom(result.getClass())) {
            ((ICSAware) result).setICS(ics);
        }
        // todo: high implement other injections
        return null;
    }

    private String getAttribute(ICS ics) {
        return null; // todo: high: implement
    }

    private Action getAction(String attribute) {
        return null; // todo: high: implement
    }
}
