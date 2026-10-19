package com.example.orgmgt.config;

import com.example.flowengine.sdk.api.FlowDefinitionService;
import com.example.flowengine.sdk.api.FlowEngine;
import com.example.flowengine.sdk.model.FlowDefinition;
import com.example.flowengine.sdk.model.FlowNode;
import com.example.flowengine.sdk.model.Transition;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OrgMgtFlowTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(OrgMgtFlowTemplateConfig.class);

    private final FlowDefinitionService flowDefinitionService;
    private final FlowEngine flowEngine;

    public OrgMgtFlowTemplateConfig(FlowDefinitionService flowDefinitionService, FlowEngine flowEngine) {
        this.flowDefinitionService = flowDefinitionService;
        this.flowEngine = flowEngine;
    }

    @PostConstruct
    public void registerFlowTemplates() {
        log.info("Registering OrgMgt flow templates...");
        registerOrgChangeApprovalFlow();
        registerStaffingExemptionFlow();
        registerJobDescriptionApprovalFlow();
        log.info("OrgMgt flow templates registration completed");
    }

    private void registerOrgChangeApprovalFlow() {
        String flowId = "ORG-CHG-001";

        if (flowDefinitionService.getLatest(flowId) != null) {
            log.debug("Flow template {} already exists, skipping registration", flowId);
            return;
        }

        Map<String, FlowNode> nodes = new LinkedHashMap<>();
        nodes.put("START", createStartNode("Start"));
        nodes.put("DIRECT_SUPERVISOR", createUserTaskNode("Direct Supervisor Review", "directSupervisor"));
        nodes.put("DEPT_HEAD", createUserTaskNode("Department Head Approval", "deptHead"));
        nodes.put("HR_REVIEW", createUserTaskNode("HR Review", "hrReviewer"));
        nodes.put("FINAL_APPROVAL", createUserTaskNode("Final Approval", "finalApprover"));
        nodes.put("END", createEndNode("End"));

        Map<String, Transition[]> transitions = new LinkedHashMap<>();
        transitions.put("START", new Transition[]{createTransition("START", "DIRECT_SUPERVISOR", "SUBMIT")});
        transitions.put("DIRECT_SUPERVISOR", new Transition[]{
                createTransition("DIRECT_SUPERVISOR", "DEPT_HEAD", "APPROVE"),
                createTransition("DIRECT_SUPERVISOR", "END", "REJECT")
        });
        transitions.put("DEPT_HEAD", new Transition[]{
                createTransition("DEPT_HEAD", "HR_REVIEW", "APPROVE"),
                createTransition("DEPT_HEAD", "END", "REJECT")
        });
        transitions.put("HR_REVIEW", new Transition[]{
                createTransition("HR_REVIEW", "FINAL_APPROVAL", "APPROVE"),
                createTransition("HR_REVIEW", "DEPT_HEAD", "REJECT")
        });
        transitions.put("FINAL_APPROVAL", new Transition[]{
                createTransition("FINAL_APPROVAL", "END", "APPROVE"),
                createTransition("FINAL_APPROVAL", "HR_REVIEW", "REJECT")
        });

        FlowDefinition definition = new FlowDefinition();
        definition.setFlowId(flowId);
        definition.setFlowName("Organization Change Approval Process");
        definition.setVersion("1.0");
        definition.setModuleCode("ORG_MGT");
        definition.setFlowType("APPROVAL");
        definition.setDescription("Organization structure change approval workflow");
        definition.setStartNodeId("START");
        definition.setEndNodeIds(new String[]{"END"});
        definition.setNodes(nodes);
        definition.setTransitions(transitions);
        definition.setStatus("ACTIVE");
        definition.setCrossModuleEnabled(false);
        definition.setCreateTime(LocalDateTime.now());

        validateAndDeploy(flowId, definition);
    }

    private void registerStaffingExemptionFlow() {
        String flowId = "ORG-EXM-001";

        if (flowDefinitionService.getLatest(flowId) != null) {
            log.debug("Flow template {} already exists, skipping registration", flowId);
            return;
        }

        Map<String, FlowNode> nodes = new LinkedHashMap<>();
        nodes.put("START", createStartNode("Start"));
        nodes.put("DEPT_HEAD", createUserTaskNode("Department Head Review", "deptHead"));
        nodes.put("HR_HEAD", createUserTaskNode("HR Head Approval", "hrHead"));
        nodes.put("END", createEndNode("End"));

        Map<String, Transition[]> transitions = new LinkedHashMap<>();
        transitions.put("START", new Transition[]{createTransition("START", "DEPT_HEAD", "SUBMIT")});
        transitions.put("DEPT_HEAD", new Transition[]{
                createTransition("DEPT_HEAD", "HR_HEAD", "APPROVE"),
                createTransition("DEPT_HEAD", "END", "REJECT")
        });
        transitions.put("HR_HEAD", new Transition[]{
                createTransition("HR_HEAD", "END", "APPROVE"),
                createTransition("HR_HEAD", "DEPT_HEAD", "REJECT")
        });

        FlowDefinition definition = new FlowDefinition();
        definition.setFlowId(flowId);
        definition.setFlowName("Staffing Exemption Approval Process");
        definition.setVersion("1.0");
        definition.setModuleCode("ORG_MGT");
        definition.setFlowType("APPROVAL");
        definition.setDescription("Staffing standard exemption approval workflow");
        definition.setStartNodeId("START");
        definition.setEndNodeIds(new String[]{"END"});
        definition.setNodes(nodes);
        definition.setTransitions(transitions);
        definition.setStatus("ACTIVE");
        definition.setCrossModuleEnabled(false);
        definition.setCreateTime(LocalDateTime.now());

        validateAndDeploy(flowId, definition);
    }

    private void registerJobDescriptionApprovalFlow() {
        String flowId = "ORG-JD-001";

        if (flowDefinitionService.getLatest(flowId) != null) {
            log.debug("Flow template {} already exists, skipping registration", flowId);
            return;
        }

        Map<String, FlowNode> nodes = new LinkedHashMap<>();
        nodes.put("START", createStartNode("Start"));
        nodes.put("POSITION_MANAGER", createUserTaskNode("Position Manager Review", "positionManager"));
        nodes.put("HR_COMPENSATION", createUserTaskNode("HR Compensation Review", "hrCompensation"));
        nodes.put("FINAL_APPROVAL", createUserTaskNode("Final Approval", "finalApprover"));
        nodes.put("END", createEndNode("End"));

        Map<String, Transition[]> transitions = new LinkedHashMap<>();
        transitions.put("START", new Transition[]{createTransition("START", "POSITION_MANAGER", "SUBMIT")});
        transitions.put("POSITION_MANAGER", new Transition[]{
                createTransition("POSITION_MANAGER", "HR_COMPENSATION", "APPROVE"),
                createTransition("POSITION_MANAGER", "END", "REJECT")
        });
        transitions.put("HR_COMPENSATION", new Transition[]{
                createTransition("HR_COMPENSATION", "FINAL_APPROVAL", "APPROVE"),
                createTransition("HR_COMPENSATION", "POSITION_MANAGER", "REJECT")
        });
        transitions.put("FINAL_APPROVAL", new Transition[]{
                createTransition("FINAL_APPROVAL", "END", "APPROVE"),
                createTransition("FINAL_APPROVAL", "HR_COMPENSATION", "REJECT")
        });

        FlowDefinition definition = new FlowDefinition();
        definition.setFlowId(flowId);
        definition.setFlowName("Job Description Approval Process");
        definition.setVersion("1.0");
        definition.setModuleCode("ORG_MGT");
        definition.setFlowType("APPROVAL");
        definition.setDescription("Job description update approval workflow");
        definition.setStartNodeId("START");
        definition.setEndNodeIds(new String[]{"END"});
        definition.setNodes(nodes);
        definition.setTransitions(transitions);
        definition.setStatus("ACTIVE");
        definition.setCrossModuleEnabled(false);
        definition.setCreateTime(LocalDateTime.now());

        validateAndDeploy(flowId, definition);
    }

    private void validateAndDeploy(String flowId, FlowDefinition definition) {
        FlowDefinitionService.ValidationResult result = flowDefinitionService.validate(definition);
        if (result.valid()) {
            flowDefinitionService.create(definition);
            flowDefinitionService.deploy(flowId, "1.0");
            log.info("Successfully registered flow template: {}", flowId);
        } else {
            log.error("Failed to validate flow template {}: {}", flowId, String.join(", ", result.errors()));
        }
    }

    private FlowNode createStartNode(String name) {
        FlowNode node = new FlowNode();
        node.setNodeId("START");
        node.setNodeName(name);
        node.setNodeType("START");
        return node;
    }

    private FlowNode createUserTaskNode(String name, String assignee) {
        FlowNode node = new FlowNode();
        node.setNodeId(name.toUpperCase().replace(" ", "_"));
        node.setNodeName(name);
        node.setNodeType("USER_TASK");
        node.setAssignee(assignee);
        return node;
    }

    private FlowNode createEndNode(String name) {
        FlowNode node = new FlowNode();
        node.setNodeId("END");
        node.setNodeName(name);
        node.setNodeType("END");
        return node;
    }

    private Transition createTransition(String sourceNodeId, String targetNodeId, String name) {
        Transition transition = new Transition();
        transition.setTransitionId(sourceNodeId + "_" + targetNodeId + "_" + name);
        transition.setSourceNodeId(sourceNodeId);
        transition.setTargetNodeId(targetNodeId);
        transition.setName(name);
        return transition;
    }
}
