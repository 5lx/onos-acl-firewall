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
import org.cpsc8570.team4.impl.AdvAclManager;
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
import java.util.List;
import java.util.Set;

import static org.onlab.util.Tools.nullIsNotFound;

@Path("sample")
public class AppWebResource extends AbstractWebResource {

    private ApplicationId appId;
    private String appName = "org.foo.app";

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    public Response test() {

        ObjectNode node = mapper().createObjectNode();
        AdvAclService advAclService = get(AdvAclService.class);
        List<AdvAclRule> rules = advAclService.getAdvAclRules();
        for(AdvAclRule rule : rules){
            node.put(rule.getId().toString(), rule.toString());
        }
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
        String srcIpStart = node.path("srcIpStart").asText();
        String srcIpEnd = node.path("srcIpEnd").asText();
        String dstIpStart = node.path("dstIpStart").asText();
        String dstIpEnd = node.path("dstIpEnd").asText();
        String action = node.path("action").asText();
        int dstPort = node.path("dstPort").asInt();
        String ipProto = node.path("ipProto").asText();

        AdvAclRule rule = new AdvAclRule(srcIpStart, srcIpEnd, dstIpStart,
                                         dstIpEnd, dstPort, ipProto, action);

        AdvAclService advAclService = get(AdvAclService.class);
        rule = advAclService.addAdvAclRule(rule);

        if(!rule.isLeagal()){
            return Response.noContent().build();
        }

        ObjectNode ret = mapper().createObjectNode();
        ret.put("rule", rule.toString());
        return ok(ret).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response clearRule(InputStream stream) throws URISyntaxException{
        JsonNode node;
        try{
            node = mapper().readTree(stream);
        } catch (IOException e){
            this.clearAll();
            return Response.noContent().build();
//            throw new IllegalArgumentException("Unable to parse ACL request", e);
        }
        long ruleId = node.path("ruleId").asLong();

        AdvAclService advAclService = get(AdvAclService.class);
        advAclService.clearByRuleId(AdvAclRuleId.valueOf(ruleId));
        return Response.noContent().build();
    }

    @DELETE
    public Response clearAll(){

        AdvAclService advAclService = get(AdvAclService.class);
        advAclService.clearAll();

        FlowRuleService flowRuleService = get(FlowRuleService.class);
        CoreService coreService = get(CoreService.class);
        appId = coreService.registerApplication(appName);
        flowRuleService.removeFlowRulesById(appId);

        return Response.noContent().build();
    }

}
