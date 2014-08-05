package org.jugvale.jbpm.client.controller;

import java.net.URL;
import java.util.List;

import org.jugvale.jbpm.client.model.LoginModel;
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
			return new JBPMController(newEngine, loginModel);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public List<TaskSummary> allTasks() {
		return allTasks(DEFAULT_LANGUAGE);
	}

	public List<TaskSummary> allTasks(String lang) {
		return engine.getTaskService().getTasksAssignedAsPotentialOwner(
				loginModel.username.get(), lang);
	}

	public LoginModel getLoginModel() {
		return loginModel;
	}
}