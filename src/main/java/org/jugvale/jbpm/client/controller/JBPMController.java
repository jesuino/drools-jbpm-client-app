package org.jugvale.jbpm.client.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.model.LoginModel;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteRestRuntimeFactory;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

import com.sun.javafx.binding.StringFormatter;

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

	public void doTaskOperation(long taskId, TaskOperation operation,
			HashMap<String, Object> params, Consumer<String> onSuccess,
			Consumer<String> onFail) {
		TaskService service = engine.getTaskService();
		String user = loginModel.username.get();
		if (onFail == null)
			onFail = System.out::println;

		if (onSuccess == null)
			onSuccess = System.out::println;

		try {
			switch (operation) {
			case ACTIVATE:
				service.activate(taskId, user);
				break;
			case CLAIM:
				service.claim(taskId, user);
				break;
			case COMPLETE:
				service.complete(taskId, user, params);
				break;
			case EXIT:
				service.exit(taskId, user);
				break;
			case FAIL:
				service.fail(taskId, user, params);
				break;
			case START:
				service.start(taskId, user);
				break;
			default:
				onFail.accept("Task Operation not found");
				break;
			}
			onSuccess.accept(StringFormatter.format(
					"Success executing \"%s\" on task \"%d\".\n", operation,
					taskId).get());
		} catch (Exception e) {
			e.printStackTrace();
			onFail.accept(StringFormatter
					.format("Fail when executing \"%s\" on task \"%d\" with message %s: \n",
							operation, taskId, e.getMessage()).get());
		}
	}

	// TODO NOT SUPPORTED! Change to another method.
	public void createProcessInstance(String processName,
			HashMap<String, Object> params, Consumer<String> onSuccess,
			Consumer<String> onFail) {
		if (onFail == null)
			onFail = System.out::println;
		if (onSuccess == null)
			onSuccess = System.out::println;
		if (processName == null || processName.trim().isEmpty()) {
			onFail.accept("Please provide a valid process name");
			return;
		}
		try {
			engine.getKieSession().createProcessInstance(processName, params);
			onSuccess.accept(StringFormatter.format(
					"Instance of process \"%s\" created with success",
					processName).get());
		} catch (Exception e) {
			e.printStackTrace();
			onFail.accept(StringFormatter.format(
					"Fail when creating an instance of process \"%s\": %s",
					processName, e.getMessage()).get());
		}
	}

	public LoginModel getLoginModel() {
		return loginModel;
	}
}