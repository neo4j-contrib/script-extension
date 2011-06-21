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
package org.neo4j.server.extension.script.resources;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * @author tbaum
 * @since 21.06.11 11:13
 */
public class ServerResource {

    private final String property;

    public ServerResource(String property) {
        this.property = property;
    }

    public void delete(GraphDatabaseService gds) throws IOException {
        Transaction tx = gds.beginTx();
        try {
            gds.getReferenceNode().removeProperty(property);
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public boolean existInGraphDb(GraphDatabaseService gds) {
        return gds.getReferenceNode().hasProperty(property);
    }

    public String retrieve(final GraphDatabaseService gds) {
        try {
            return (String) gds.getReferenceNode().getProperty(property);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean store(String data, GraphDatabaseService gds) throws IOException {
        Transaction tx = gds.beginTx();
        try {
            gds.getReferenceNode().setProperty(property, data);
            tx.success();
        } finally {
            tx.finish();
        }
        return true;
    }
}
