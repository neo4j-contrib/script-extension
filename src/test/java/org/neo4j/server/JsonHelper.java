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
package org.neo4j.server;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class JsonHelper {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap( String json ) {
        return (Map<String, Object>) readJson( json );
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> jsonToListOfRelationshipRepresentations( String json ) {
        return (List<Map<String, Object>>) readJson( json );
    }

    private static Object readJson( String json ) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue( json, Object.class );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public static Object jsonToSingleValue( String json ) {
        return readJson( json );
    }

    public static String createJsonFrom( Object data ) {
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = OBJECT_MAPPER.getJsonFactory()
                    .createJsonGenerator( writer ).useDefaultPrettyPrinter();
            OBJECT_MAPPER.writeValue( generator, data );
            writer.close();
            return writer.getBuffer().toString();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}