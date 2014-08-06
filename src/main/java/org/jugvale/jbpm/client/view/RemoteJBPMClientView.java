package org.jugvale.jbpm.client.view;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.controller.JBPMController;
import org.kie.api.task.model.TaskSummary;

/**
 * 
 * A view for the main remote capabilities of the jBPM API
 * 
 * @author william
 *
 */

// TODO: Finish  tasks functions
// TODO: Handle errors when dealing with tasks
// TODO: avoid tasks issues when invoking tasks functions
// TODO: make the tool allow us to create a process instance
// TODO: explore more methods of the API
// TODO: Check why the tasks are not receiving parameters
// TODO: Filter the table

public class RemoteJBPMClientView extends TabPane {

	private JBPMController controller;
	private TableView<TaskSummary> tblUserTasks;
	private TableView<TaskSummary> tblProcessTasks;
	// Holds the current selected process
	private ObjectProperty<ProcessInstanceLog> selectedProcess = new SimpleObjectProperty<>();
	// holds the current selected task
	private ObjectProperty<TaskSummary> selectedTask = new SimpleObjectProperty<>();
	// the menu for tasks actions
	ContextMenu taskContextMenu;
	private Label lblProcessTasks = new Label("");

	private TableView<ProcessInstanceLog> tblProcessesInstances;

	/**
	 * A property to indicate when we are doing heavy tasks
	 */
	private BooleanProperty loadingTasks = new SimpleBooleanProperty();

	public RemoteJBPMClientView() {
		build();
		settings();
	}

	private void settings() {
		setSide(Side.LEFT);
		setMinHeight(500);
		getTabs().forEach(t -> t.setClosable(false));
	}

	private void build() {
		createTasksContextMenu();
		Tab processTaskTab = createProcessTasksPane();
		getTabs().addAll(createProcessesPane(), createUserTasksPane(),
				processTaskTab);
		// when we select a process, we will change to the process tasks tab and
		// fill it with the user tasks
		processTaskTab.disableProperty().bind(selectedProcess.isNull());
		selectedProcess.addListener((obs, o, n) -> {
			getSelectionModel().select(processTaskTab);
			lblProcessTasks.setText("Tasks for process \""
					+ selectedProcess.get().getProcessId() + "\", instance "
					+ selectedProcess.get().getProcessInstanceId());
			loadProcessTask();
		});
	}

	private void createTasksContextMenu() {
		MenuItem menuStart = new MenuItem("Start");
		MenuItem menuComplete = new MenuItem("Complete");
		MenuItem menuActivate = new MenuItem("Activate");
		MenuItem menuClaim = new MenuItem("Claim");
		menuStart.setOnAction(e -> start());
		menuComplete.setOnAction(e -> complete());
		menuActivate.setOnAction(e -> activate());
		menuClaim.setOnAction(e -> claim());
		taskContextMenu = new ContextMenu(menuStart, menuComplete, menuActivate, menuClaim);
		// menus are disabled if the task is null
		taskContextMenu.getItems().forEach(
				i -> i.disableProperty().bind(selectedTask.isNull()));
	}

	public void setController(JBPMController controller) {
		this.controller = controller;
		loadData();
	}

	private void loadData() {
		loadUserTasks();
		loadAllProcessInstances();
	}

	@SuppressWarnings("unchecked")
	private Tab createProcessesPane() {
		tblProcessesInstances = new TableView<>();
		tblProcessesInstances.getColumns().addAll(
				makeProcessColumn("ID", "processId", 80),
				makeProcessColumn("Instance ID", "processInstanceId", 80),
				makeProcessColumn("Name", "processName", 100),
				makeProcessColumn("Version", "processVersion", 100),
				makeProcessColumn("Status", "status", 20),
				makeProcessColumn("Start Date", "start", 180),
				makeProcessColumn("End Date", "end", 180),
				makeProcessColumn("Outcome", "outcome", 100),
				makeProcessColumn("Duration", "duration", 100));
		Tab t = new Tab("Processes Instances");
		Button btnSelectProcess = new Button("Show process tasks");
		// enable this button only if we have anything selected on the table
		btnSelectProcess.disableProperty().bind(
				tblProcessesInstances.getSelectionModel()
						.selectedItemProperty().isNull());
		// when we select an item and click on the button, we will list its item
		// tasks
		btnSelectProcess.setOnAction(e -> {
			selectedProcess.set(tblProcessesInstances.getSelectionModel()
					.getSelectedItem());
		});
		HBox bottomBar = new HBox(20,
				refreshButton(this::loadAllProcessInstances), btnSelectProcess);

		t.setContent(new VBox(tblProcessesInstances, bottomBar));
		return t;
	}

	private Tab createProcessTasksPane() {
		tblProcessTasks = new TableView<>();
		tblProcessTasks.setContextMenu(taskContextMenu);
		tblProcessTasks.getColumns().addAll(createTasksColumns());
		Tab t = new Tab("Process Tasks");
		// everytime we selected a task
		tblProcessTasks
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> selectedTask.set(tblProcessTasks
								.getSelectionModel().getSelectedItem()));

		t.setContent(new VBox(10, lblProcessTasks, tblProcessTasks,
				refreshButton(this::loadProcessTask)));
		return t;
	}

	private Tab createUserTasksPane() {
		tblUserTasks = new TableView<>();
		tblUserTasks.setContextMenu(taskContextMenu);
		tblUserTasks.getColumns().addAll(createTasksColumns());
		Tab t = new Tab("User Tasks");
		t.setContent(new VBox(tblUserTasks, refreshButton(this::loadUserTasks)));
		// everytime we selected a task
		tblUserTasks
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> selectedTask.set(tblUserTasks
								.getSelectionModel().getSelectedItem()));

		return t;
	}

	@SuppressWarnings("unchecked")
	private ObservableList<TableColumn<TaskSummary, String>> createTasksColumns() {
		return FXCollections.observableArrayList(
				makeTaskColumn("ID", "id", 50),
				makeTaskColumn("Name", "name", 100),
				makeTaskColumn("Status", "status", 120),
				makeTaskColumn("Priority", "priority", 80),
				makeTaskColumn("Created on", "createdOn", 220),
				makeTaskColumn("Expiration Time", "expirationTime", 220));
	}

	private TableColumn<ProcessInstanceLog, String> makeProcessColumn(
			String colName, String property, double width) {
		return makeColumn(ProcessInstanceLog.class, colName, property, width);
	}

	private TableColumn<TaskSummary, String> makeTaskColumn(String colName,
			String property, double width) {
		return makeColumn(TaskSummary.class, colName, property, width);
	}

	private <T> TableColumn<T, String> makeColumn(Class<?> t, String colName,
			String property, double width) {
		TableColumn<T, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(property));
		col.setMinWidth(width);
		return col;
	}

	private void loadAllProcessInstances() {
		runLater(() -> tblProcessesInstances.getItems().setAll(
				controller.allProccessInstances()));
	}

	private void loadProcessTask() {
		long id = selectedProcess.get().getProcessInstanceId();
		runLater(() -> tblProcessTasks.getItems().setAll(
				controller.tasksByProcessInstanceId(id)));
	}

	private void loadUserTasks() {
		runLater(() -> tblUserTasks.getItems().setAll(controller.allTasks()));
	}

	private void start() {
		runLater(() -> controller.start(selectedTask.get().getId()));
		loadData();
	}

	private void complete() {
		runLater(() -> controller.complete(selectedTask.get().getId()));
		loadData();
	}

	private void activate() {
		runLater(() -> controller.activate(selectedTask.get().getId()));
		loadData();
	}

	private void claim() {
		runLater(() -> controller.claim(selectedTask.get().getId()));
		loadData();
	}

	private void runLater(Runnable r) {
		Platform.runLater(() -> {
			loadingTasks.set(true);
			r.run();
			loadingTasks.set(false);
		});
	}

	private Button refreshButton(Runnable r) {
		Button btnRefresh = new Button("Refresh");
		loadingTasks.addListener((obs, o, n) -> {
			btnRefresh.setText(n ? "Refresing..." : "Refresh");
		});
		btnRefresh.setOnAction(e -> {
			r.run();
		});
		return btnRefresh;
	}
}