/*
 * Copyright © 2015 Mingming Chen and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.latency.impl.rev141210;

import java.util.concurrent.Executors;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.latency.impl.LatencyProvider;
//import org.opendaylight.latency.impl.Latencymain;
import org.opendaylight.openflowplugin.applications.lldpspeaker.LLDPSpeaker;
import org.opendaylight.openflowplugin.applications.lldpspeaker.NodeConnectorInventoryEventTranslator;
import org.opendaylight.openflowplugin.applications.lldpspeaker.OperationalStatusChangeService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.latency.rev150105.LatencyService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.LldpSpeakerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.latency.impl.rev141210.AbstractLatencyModule {
	private static final Logger LOG = LoggerFactory.getLogger(LatencyModule.class);
	public LatencyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LatencyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.latency.impl.rev141210.LatencyModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
    	PacketProcessingService packetProcessingService = getRpcRegistryDependency().getRpcService(PacketProcessingService.class);
        MacAddress macDestination = getAddressDestination();
        final LLDPSpeaker lldpSpeaker = new LLDPSpeaker(packetProcessingService, Executors.newSingleThreadScheduledExecutor(), macDestination);
        
        LatencyProvider latencyProvider = new LatencyProvider(packetProcessingService, getDataBrokerDependency(), lldpSpeaker);
        final NodeConnectorInventoryEventTranslator eventTranslator = new NodeConnectorInventoryEventTranslator(
                getDataBrokerDependency(), lldpSpeaker);

       //OperationalStatusChangeService operationalStatusChangeService = new OperationalStatusChangeService(lldpSpeaker);
        final BindingAwareBroker.RpcRegistration<LatencyService> latencyServiceRegistration =
                getRpcRegistryDependency().addRpcImplementation(LatencyService.class, latencyProvider);

        
        return new AutoCloseable() {
            @Override
            public void close() {
                LOG.trace("Closing LLDP speaker.");
                eventTranslator.close();
                lldpSpeaker.close();
                latencyServiceRegistration.close();
            }
        };
    }

}
