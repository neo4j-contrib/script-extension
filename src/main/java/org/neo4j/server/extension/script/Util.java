/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.extension.script;

import org.apache.commons.configuration.Configuration;
import org.neo4j.server.configuration.Configurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tbaum
 * @since 16.06.11 21:03
 */
public class Util {

    public static final String CONFIG_BASE = "org.neo4j.server.extension.scripting.jruby";

    public static String locateGemHome(final Configuration configuration) {
        return getConfig(configuration, CONFIG_BASE + ".gemhome", "/../gems");
    }

    private static String getConfig(final Configuration configuration, final String key, final String s) {
        try {
            final String configValue = configuration.getString(key,
                    configuration.getString(Configurator.DATABASE_LOCATION_PROPERTY_KEY) + s);

            return new File(configValue).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String locateRackHome(final Configuration configuration) {
        return getConfig(configuration, CONFIG_BASE + ".jruby.rackhome", "/../rack");
    }

    public static List<String> listMountPoints(final Configuration configuration) {
        List<String> results = new ArrayList<String>();
        String[] mounts = configuration.getStringArray(CONFIG_BASE + ".mountpoints");
        for (String mount : mounts) {
            mount = mount.trim();

            if (!mount.startsWith("/")) {
                mount = "/" + mount;
            }

            if (mount.endsWith("/")) {
                mount = mount.substring(0, mount.length() - 1);
            }

            results.add(mount);
        }
        if (results.isEmpty()) {
            results.add("/dsr");
        }
        return results;
    }
}
