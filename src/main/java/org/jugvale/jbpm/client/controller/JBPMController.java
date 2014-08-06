package org.jugvale.jbpm.client.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.model.LoginModel;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteRestRuntimeFactory;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * 
 * The class responsible to communicate to jbpm. TODO: extend it to communicate
 * via the pure REST API and JMS
 * 
 * @author william
 *
 */
public class JBPMController {

	final String DEFAULT_LANGUAGE = "en-UK";
	public static final List<Status> ALL_TASK_STATUS = Arrays.asList(Status
			.values());
	private RemoteRuntimeEngine engine;
	private LoginModel loginModel;

	private JBPMController(RemoteRuntimeEngine engine, LoginModel loginModel) {
		this.engine = engine;
		this.loginModel = loginModel;
	}

	public static JBPMController logIn(LoginModel loginModel) {
		RemoteRuntimeEngine newEngine = null;
		try {
			newEngine = new RemoteRestRuntimeFactory(
					loginModel.deploymentId.get(),
					new URL(loginModel.url.get()), loginModel.username.get(),
					loginModel.password.get()).newRuntimeEngine();
			System.out.println("Kie Session ID: "
					+ newEngine.getKieSession().getId());
			newEngine.getKieSession().getEntryPointId();
			return new JBPMController(newEngine, loginModel);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public List<TaskSummary> allTasks() {
		return allTasks(DEFAULT_LANGUAGE);
	}

	public boolean processExist(String processName) {
		AuditLogService s = engine.getAuditLogService();
		try {
			return s.findProcessInstances(processName) != null;			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}	
	}

	public List<ProcessInstanceLog> allProccessInstances() {
		AuditLogService s = engine.getAuditLogService();
		return s == null ? null : s.findProcessInstances();
	}

	public List<TaskSummary> allTasks(String lang) {
		return engine.getTaskService()
				.getTasksAssignedAsPotentialOwnerByStatus(
						loginModel.username.get(), ALL_TASK_STATUS, lang);
	}

	public List<TaskSummary> tasksByProcessInstanceId(long id) {
		return engine.getTaskService().getTasksByStatusByProcessInstanceId(id,
				ALL_TASK_STATUS, DEFAULT_LANGUAGE);
	}

	public void complete(long id) {
		HashMap<String, Object> params = new HashMap<>();
		params.put("nome", "João");
		engine.getTaskService().complete(id, loginModel.username.get(), params);
	}

	public void start(long id) {
		engine.getTaskService().start(id, loginModel.username.get());
	}

	public void activate(long id) {
		engine.getTaskService().activate(id, loginModel.username.get());
	}

	public void claim(long id) {
		engine.getTaskService().claim(id, loginModel.username.get());
	}

	public LoginModel getLoginModel() {
		return loginModel;
	}
}