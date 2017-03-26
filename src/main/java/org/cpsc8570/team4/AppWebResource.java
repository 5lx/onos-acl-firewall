/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cpsc8570.team4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.flow.*;
import org.onosproject.net.host.HostService;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.IdGenerator;
import org.onosproject.mastership.MastershipService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.onlab.util.Tools.nullIsNotFound;

/**
 * Sample web resource.
 */
//@Component(immediate = true)
//@Service
@Path("sample")
public class AppWebResource extends AbstractWebResource {


//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected CoreService coreService;
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected FlowRuleService flowRuleService;
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected HostService hostService;
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected MastershipService mastershipService;

    private ApplicationId appId;

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    public Response test() {
        ObjectNode node = mapper().createObjectNode().put("hello", "world,haheha");
        return ok(node).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRule(InputStream stream) throws URISyntaxException{
        JsonNode node;
        try{
            node = mapper().readTree(stream);
        } catch (IOException e){
            throw new IllegalArgumentException("Unable to parse ACL request", e);
        }

        // get data from json
        String srcIp = node.path("srcIp").asText();
        String dstIp = node.path("dstIp").asText();
        String action = node.path("action").asText();


        // get device id
        HostService hostService = get(HostService.class);
        Set<DeviceId> selectedDeviceIds = new HashSet<>();
        final Iterable<Host> hosts = hostService.getHosts();

//        if(hostService == null){
//        CoreService coreService = get(CoreService.class);
//        appId = coreService.registerApplication("onos-acl-firewall");
//        ObjectNode n = mapper().createObjectNode().put("abc", appId.toString());
//        return ok(n).build();
//        }

        for(Host h : hosts){
            for(IpAddress a : h.ipAddresses()){
                if(a.getIp4Address().toInt() == Ip4Prefix.valueOf(srcIp).address().toInt()){
                    selectedDeviceIds.add(h.location().deviceId());
                }
            }
        }

        // generate flow table
        CoreService coreService = get(CoreService.class);
        appId = coreService.registerApplication("onos-acl-firewall");
        FlowRuleService flowRuleService = get(FlowRuleService.class);
        if("deny".equals(action)){

            for(DeviceId deviceId : selectedDeviceIds){

                TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
                TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();
                FlowEntry.Builder flowEntry = DefaultFlowEntry.builder();

                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);
                selectorBuilder.matchIPSrc(Ip4Prefix.valueOf(srcIp));
                selectorBuilder.matchIPDst(Ip4Prefix.valueOf(dstIp));

                flowEntry.forDevice(deviceId);
                flowEntry.withSelector(selectorBuilder.build());
                flowEntry.withTreatment(treatment.build());
                flowEntry.withPriority(30000);
                flowEntry.fromApp(appId);

                flowEntry.makePermanent();
                flowRuleService.applyFlowRules(flowEntry.build());
            }

        }
        return ok(node).build();
    }

    @DELETE
    public Response clearAll(){
        FlowRuleService flowRuleService = get(FlowRuleService.class);
        CoreService coreService = get(CoreService.class);
        appId = coreService.registerApplication("onos-acl-firewall");
        flowRuleService.removeFlowRulesById(appId);
        return Response.noContent().build();
    }

}
