/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierConfigResult;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class ChannelChangeListenerTest extends AbstractDataBrokerTest {


    private ChannelConfigWriterMock channelConfigWriterMock;
    private ChannelChangeListener channelChangeListener;

    public void setUp() {
        channelConfigWriterMock = new ChannelConfigWriterMock();
        channelChangeListener = new ChannelChangeListener(getDataBroker(),channelConfigWriterMock);
    }

    private static class DataTreeModificationMock implements DataTreeModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public DataTreeIdentifier<Channel> getRootPath() {
            InstanceIdentifier<Channel> channelId = InstanceIdentifier.create(BierNetworkChannel.class)
                    .child(BierChannel.class, new BierChannelKey("flow:1")).child(Channel.class);
            return new DataTreeIdentifier<Channel>(
                    LogicalDatastoreType.CONFIGURATION, channelId);
        }

        @Override
        public DataObjectModification<Channel> getRootNode() {
            DataObjectModificationMock mock = new DataObjectModificationMock();
            mock.setChannelData(before, after, type);
            return mock;
        }
    }

    private static class DataObjectModificationMock implements DataObjectModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public ModificationType getModificationType() {
            return type;
        }

        @Override
        public Channel getDataBefore() {
            return before;
        }

        @Override
        public Channel getDataAfter() {
            return after;
        }

        @Override
        public PathArgument getIdentifier() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<Channel> getDataType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends ChildOf<? super Channel>> DataObjectModification<C> getModifiedChildContainer(
                Class<C> child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Augmentation<Channel> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                Class<C> augmentation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super Channel>,
             K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(
                Class<C> listItem, K listKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataObjectModification<? extends DataObject> getModifiedChild(
                PathArgument childArgument) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class ChannelConfigWriterMock implements ChannelConfigWriter {

        private List<Channel> channelList = new ArrayList<>();
        private List<Channel> channelEgressDelList = new ArrayList<>();

        @Override
        public BierConfigResult writeChannel(ConfigurationType type, Channel channel) {
            switch (type) {
                case ADD:
                    if (null != channel) {
                        channelList.add(channel);
                    } else {
                        return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                    }
                    break;
                case MODIFY:
                    if (null != channel && null != getChannelFromList(channel.getName())) {
                        deleteChannelFromList(channel.getName());
                        channelList.add(channel);
                    } else {
                        return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                    }
                    break;
                case DELETE:
                    if (null != channel && null != getChannelFromList(channel.getName())) {
                        deleteChannelFromList(channel.getName());
                    } else {
                        return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);
        }

        @Override
        public BierConfigResult writeChannelEgressNode(ConfigurationType type, Channel channel) {
            switch (type) {
                case ADD:
                    return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                case MODIFY:
                    return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                case DELETE:
                    if (null != channel) {
                        channelEgressDelList.add(channel);
                    } else {
                        return new BierConfigResult(BierConfigResult.ConfigurationResult.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);
        }

        public Channel getChannelFromList(String name) {
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }

        public Channel getChannelEgressDelFromList(String name) {
            for (Channel channel : channelEgressDelList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }

        public void deleteChannelFromList(String name) {
            if (null == name) {
                return;
            }
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    channelList.remove(channel);
                    return;
                }
            }
        }
    }

    private void addNodeToDatastore(String nodeId,int domainId,int subDomainId,int bfrId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("flow:1"))
                .child(BierNode.class,new BierNodeKey(nodeId));
        BierNodeBuilder bierNode = new BierNodeBuilder();
        bierNode.setNodeId(nodeId);

        List<SubDomain> subDomainList = new ArrayList<>();
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setBfrId(new BfrId(bfrId));
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder global = new BierGlobalBuilder();
        global.setSubDomain(subDomainList);

        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setBierGlobal(global.build());

        List<Domain> domainList = new ArrayList<>();
        domainList.add(domainBuilder.build());

        BierNodeParamsBuilder para = new BierNodeParamsBuilder();
        if (null != domainList && !domainList.isEmpty()) {
            para.setDomain(domainList);
        }
        bierNode.setBierNodeParams(para.build());

        tx.put(LogicalDatastoreType.CONFIGURATION, path, bierNode.build(), true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }


    private Channel constructChannel(String name,String srcIp,String dstGroup,int domainId,int subDomainId,
            short srcWild,short groupWild,int bfrId,String ingress,List<EgressNode> egressList) {
        ChannelBuilder  builder = new ChannelBuilder();
        builder.setName(name);
        builder.setKey(new ChannelKey(name));
        builder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        builder.setDstGroup(new IpAddress(new Ipv4Address(dstGroup)));
        builder.setDomainId(new DomainId(domainId));
        builder.setSubDomainId(new SubDomainId(subDomainId));
        builder.setSourceWildcard(srcWild);
        builder.setGroupWildcard(groupWild);
        builder.setIngressBfrId(new BfrId(bfrId));
        builder.setIngressNode(ingress);
        if (null != egressList && !egressList.isEmpty()) {
            builder.setEgressNode(egressList);
        }
        return builder.build();
    }

    private EgressNode constructEgressNode(int bfrId,String egress) {
        EgressNodeBuilder builder = new EgressNodeBuilder();
        builder.setEgressBfrId(new BfrId(bfrId));
        builder.setNodeId(egress);
        builder.setKey(new EgressNodeKey(egress));
        return builder.build();
    }

    private void assertChannelData(Channel expectChannel,Channel channelData) {
        Assert.assertEquals(expectChannel.getEgressNode(), channelData.getEgressNode());
        Assert.assertEquals(expectChannel.getIngressNode(),channelData.getIngressNode());
        Assert.assertEquals(expectChannel.getDomainId(),channelData.getDomainId());
        Assert.assertEquals(expectChannel.getDstGroup(),channelData.getDstGroup());
        Assert.assertEquals(expectChannel.getGroupWildcard(),channelData.getGroupWildcard());
        Assert.assertEquals(expectChannel.getSourceWildcard(),channelData.getSourceWildcard());
        Assert.assertEquals(expectChannel.getSrcIp(),channelData.getSrcIp());
        Assert.assertEquals(expectChannel.getSubDomainId(),channelData.getSubDomainId());
    }

    private Collection<DataTreeModification<Channel>> setChannelData(Channel before,
            Channel after, ModificationType type) {
        Collection<DataTreeModification<Channel>> collection = new ArrayList<>();
        DataTreeModificationMock mock = new DataTreeModificationMock();
        mock.setChannelData(before, after, type);
        collection.add(mock);
        return collection;
    }

    @Test
    public void channelListenerTset() {
        setUp();
        List<EgressNode> egressList = new ArrayList<>();
        egressList.add(constructEgressNode(5,"node-2"));
        Channel channel = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,4,"node-1",egressList);
        addNodeToDatastore("node-1",1,2,4);
        addNodeToDatastore("node-2",1,2,5);
        //Test Add channel
        channelChangeListener.onDataTreeChanged(setChannelData(null,channel,ModificationType.WRITE));
        assertChannelData(channel,channelConfigWriterMock.getChannelFromList(channel.getName()));

        //Test Modify channel
        Channel channelModify = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,3,
                (short)30,(short)40,4,"node-1",egressList);
        addNodeToDatastore("node-1",1,3,4);
        addNodeToDatastore("node-2",1,3,5);
        channelChangeListener.onDataTreeChanged(
                setChannelData(channel,channelModify,ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelModify,channelConfigWriterMock.getChannelFromList(channel.getName()));

        //Test Delete Egressnode
        Channel channelDeleteEgress = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,3,
                (short)30,(short)40,4,"node-1",null);
        channelChangeListener.onDataTreeChanged(
                setChannelData(channelModify,channelDeleteEgress,ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelDeleteEgress,channelConfigWriterMock.getChannelFromList(channelModify.getName()));
        assertChannelData(channelModify,channelConfigWriterMock.getChannelEgressDelFromList(channelModify.getName()));

        //Test Delete channel
        channelChangeListener.onDataTreeChanged(setChannelData(channel,null,ModificationType.DELETE));
        Assert.assertNull(channelConfigWriterMock.getChannelFromList(channel.getName()));
    }

}