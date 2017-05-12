/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.adapter.api;



import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.BierTePath;



public interface BierTeBitstringWriter {

    ConfigurationResult writeBierTeBitstring(ConfigurationType type, String nodeId,BierTePath bierTePath);
}