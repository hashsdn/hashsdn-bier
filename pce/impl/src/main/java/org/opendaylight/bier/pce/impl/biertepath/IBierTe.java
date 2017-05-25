/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.biertepath;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;


public interface IBierTe {

    String getBfirNodeId();

    List<BierLink> getPath();

    void writeDb();

    void removeDb();

    void destroy();

    void refreshPath(List<BierLink> tryToOverlapPath);
}
