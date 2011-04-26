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

package com.fatwire.gst.foundation.controller.action;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import COM.FutureTense.Interfaces.ICS;

import com.fatwire.gst.foundation.controller.action.AnnotationInjector.Factory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spring configuration-based action mapping. The ICS variable "cmd" is mapped
 * to a class name in the configuration to a class.
 * <p/>
 * Objects are created via a {@link Factory}, that can be configured via the
 * <tt>factoryClassname</tt>. That class needs to have a constructor accepting
 * ICS.
 * 
 * @author Tony Field
 * @author Dolf Dijkstra
 * @since 2011-03-15
 */
public final class CommandActionLocator implements ActionLocator {
    protected static final Log LOG = LogFactory.getLog(CommandActionLocator.class.getPackage().getName());
    private static final String CMD_VAR = "cmd";
    private Map<String, Action> commandActionMap = new HashMap<String, Action>();

    private Constructor<Factory> constructor;

    protected String getVarName(){
        return CMD_VAR;
    }
    
    public Action getAction(ICS ics) {
        return getAction(ics, ics.GetVar(getVarName()));
    }

    public Action getAction(ICS ics, String name) {

        final Action action;
        if (StringUtils.isBlank(name)) {
            action = newDefaultAction();
            LOG.trace("No command specified. Returning default action: " + action.getClass().getName());
        } else {
            action = commandActionMap.get(name);
            if (action == null) {
                throw new RuntimeException("No action configured for cmd: " + name);
            }
            if (LOG.isTraceEnabled())
                LOG.trace("Command '" + name + "' maps to action " + action.getClass().getName());
        }

        // inject the required data into the action
        Factory factory = getFactory(ics);
        AnnotationInjector.inject(action, factory);

        return action;
    }

    protected Action newDefaultAction() {
        return new RenderPage();
    }

    protected Factory getFactory(ICS ics) {
        Object o = ics.GetObj(Factory.class.getName());
        if (o instanceof Factory)
            return (Factory) o;
        Factory factory = null;
        try {
            factory = getInjectionFactory(ics);
        } catch (Exception e) {
            LOG.warn(e);
        }
        if (factory == null)
            factory = new IcsBackedObjectFactory(ics);
        ics.SetObj(Factory.class.getName(), factory);
        return factory;
    }

    public void setActionMap(Map<String, Action> map) {
        LOG.debug("Configured action mapping with " + (map == null ? 0 : map.size() + " entries."));
        this.commandActionMap = map;
    }

    public void setDefaultAction(Action action) {
        LOG.info("Setting default action mapping to " + action.getClass().getName());
        // defaultAction = action;
    }

    protected final Factory getInjectionFactory(ICS ics) throws Exception {
        Factory factory = null;
        if (constructor != null) {
            factory = constructor.newInstance(new Object[] { ics });
        }
        return factory;
    }

    @SuppressWarnings("unchecked")
    private void findConstructor(String factoryClassname) throws ClassNotFoundException, NoSuchMethodException,
            SecurityException {
        if (factoryClassname != null) {
            Class<Factory> c = (Class<Factory>) Class.forName(factoryClassname);
            constructor = c.getConstructor(ICS.class);

        }
    }

    /**
     * @param factoryClassname the factoryClassname to set
     */
    public void setFactoryClassname(String factoryClassname) {
        try {
            findConstructor(factoryClassname);
        } catch (Exception e) {
            throw new IllegalArgumentException("factoryClassname: " + factoryClassname + " is illegal. "
                    + e.getMessage(), e);
        }
    }

}