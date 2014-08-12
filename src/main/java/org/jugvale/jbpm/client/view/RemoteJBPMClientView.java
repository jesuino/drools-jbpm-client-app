package org.jugvale.jbpm.client.view;

import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.controller.JBPMController;
import org.jugvale.jbpm.client.controller.TaskOperation;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;

/**
 * 
 * A view for the main remote capabilities of the jBPM API
 * 
 * @author william
 *
 */

// TODO: avoid tasks issues when invoking tasks functions
// TODO: parameters when doing certains op with tasks
// TODO: parameters when creating process
// TODO: explore more methods of the API
// TODO: Check why the tasks are not receiving parameters

public class RemoteJBPMClientView extends TabPane {

	private JBPMController controller;
	private TableView<TaskSummary> tblProcessTasks;

	private ObservableList<ProcessInstanceLog> allProcessInstances;
	private ObservableList<TaskSummary> allTasksUsers;
	// Holds the current selected process
	private ObjectProperty<ProcessInstanceLog> selectedProcess;
	// holds the current selected task
	private ObjectProperty<TaskSummary> selectedTask;
	// the menu for tasks actions
	ContextMenu taskContextMenu;
	private StringProperty strProcessInfo;
	private StringProperty strTaskOperationResult;
	private StringProperty strProcessCreationResult;

	public RemoteJBPMClientView() {
		initialize();
		build();
		settings();
	}

	private void initialize() {
		allProcessInstances = FXCollections.observableArrayList();
		allTasksUsers = FXCollections.observableArrayList();
		strProcessInfo = new SimpleStringProperty();
		strTaskOperationResult = new SimpleStringProperty();
		selectedTask = new SimpleObjectProperty<>();
		selectedProcess = new SimpleObjectProperty<>();
		strProcessCreationResult = new SimpleStringProperty();
	}

	private void settings() {
		setSide(Side.LEFT);
		setMinHeight(500);
		getTabs().forEach(t -> t.setClosable(false));
	}

	private void build() {
		createTasksContextMenu();
		Tab processTaskTab = createProcessTasksPane();
		getTabs().addAll(welcomeTab(), createTab(),
				createProcessesInstancePane(), processTaskTab,
				createUserTasksPane());
		// when we select a process, we will change to the process tasks tab and
		// fill it with the user tasks
		processTaskTab.disableProperty().bind(selectedProcess.isNull());
		selectedProcess.addListener((obs, o, n) -> {
			getSelectionModel().select(processTaskTab);
			strProcessInfo.set("Tasks for process \""
					+ selectedProcess.get().getProcessId() + "\", instance "
					+ selectedProcess.get().getProcessInstanceId());
			loadProcessTasks();
		});
	}

	private Tab welcomeTab() {
		// TODO: Move style to CSS
		// TODO: use constants for texts
		Tab t = new Tab("Welcome");
		Label lblTitle = new Label("BPM 6 Client");
		Label lblSummary = new Label(
				"This is an application to interact with a BPM  6 Installation. See below what you can do with this app.\n\n "
						+ "* Create task\n"
						+ "* Create Process Instance\n"
						+ "* List all Process Instances \n"
						+ "* List all Tasks\n"
						+ "* List a Process Instance Tasks\n"
						+ "* Manage Tasks");
		lblSummary.setWrapText(true);
		lblSummary.setFont(Font.font("Serif", FontPosture.ITALIC, 25));
		lblTitle.setFont(Font.font("Serif", FontWeight.BOLD, 40));
		VBox v = new VBox(10, lblTitle, lblSummary);
		v.setTranslateX(20);
		v.setTranslateY(20);
		t.setContent(v);
		return t;
	}

	// TODO: Add support to process parameters
	private Tab createTab() {
		Tab t = new Tab("Create");
		VBox content = new VBox(10);
		HBox hbCreateProcessInstance = new HBox(10);
		HBox hbCreateTask = new HBox(10);
		Label lblTitle = new Label("Create tasks and process instances");
		lblTitle.setFont(Font.font("Serif", FontWeight.BOLD, 25));
		Label lblErrorOnCreationProcess = new Label("");
		lblErrorOnCreationProcess.textProperty().bind(strProcessCreationResult);
		Button btnCreate = new Button("Create");
		TextField txtProcessName = new TextField();

		hbCreateProcessInstance.getChildren().setAll(
				new Label("Process name:"), txtProcessName, btnCreate);
		hbCreateTask.getChildren().addAll(new Label("New Task name:"),
				new TextField(), new Button("Create"));

		content.getChildren().addAll(lblTitle,
				new Label("Create Process Instance"), hbCreateProcessInstance,
				new Separator(Orientation.HORIZONTAL),
				new Label("Create Task"), hbCreateTask);
		content.setTranslateX(20);
		content.setTranslateY(20);
		t.setContent(content);
		btnCreate.setOnAction(e -> {
			createProcessInstance(txtProcessName.getText());
		});
		return t;
	}

	private void createTasksContextMenu() {
		taskContextMenu = new ContextMenu();
		Stream.of(TaskOperation.values()).map(TaskOperation::toString)
				.map(MenuItem::new).peek(taskContextMenu.getItems()::add)
				.peek(m -> m.setOnAction(this::taskMenuAction))
				.forEach(i -> i.disableProperty().bind(selectedTask.isNull()));
	}

	public void setController(JBPMController controller) {
		this.controller = controller;
		loadData();
	}

	private void taskMenuAction(ActionEvent e) {
		if (selectedTask.get() == null)
			return;
		MenuItem m = (MenuItem) e.getTarget();
		TaskOperation o = TaskOperation.get(m.getText());
		runLater(() -> {
			controller.doTaskOperation(selectedTask.get().getId(), o, null,
					s -> {
						strTaskOperationResult.set(s);
						loadData();
					}, strTaskOperationResult::set);
		});
	}

	private void loadData() {
		loadAllUserTasks();
		loadAllProcessInstances();
		loadProcessTasks();
	}

	@SuppressWarnings("unchecked")
	private Tab createProcessesInstancePane() {
		TableView<ProcessInstanceLog> tblProcessesInstances = new TableView<>();
		ComboBox<Integer> cmbStatus = new ComboBox<>(
				FXCollections.observableArrayList(null, 1, 2, 3));
		tblProcessesInstances.getColumns().addAll(
				makeProcessColumn("Process ID", "processId", 140),
				makeProcessColumn("ID", "processInstanceId", 20),
				makeProcessColumn("Name", "processName", 100),
				makeProcessColumn("Version", "processVersion", 80),
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
		cmbStatus
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> {
							tblProcessesInstances.getItems().clear();
							if (n == null)
								tblProcessesInstances.getItems().setAll(
										allProcessInstances);
							else
								allProcessInstances
										.stream()
										.filter(e -> e.getStatus() == n)
										.forEach(
												tblProcessesInstances
														.getItems()::add);
						});
		HBox bottomBar = new HBox(20,
				refreshButton(this::loadAllProcessInstances), btnSelectProcess,
				new Label("Status"), cmbStatus);
		t.setContent(new VBox(tblProcessesInstances, bottomBar));

		// HAHA old times without Lambda!
		allProcessInstances
				.addListener(new ListChangeListener<ProcessInstanceLog>() {
					@Override
					public void onChanged(
							javafx.collections.ListChangeListener.Change<? extends ProcessInstanceLog> c) {
						tblProcessesInstances.getItems().setAll(c.getList());
					}
				});
		return t;
	}

	private Tab createProcessTasksPane() {
		tblProcessTasks = new TableView<>();
		tblProcessTasks.setContextMenu(taskContextMenu);
		tblProcessTasks.getColumns().addAll(createTasksColumns());
		Tab t = new Tab("Process Tasks");
		tblProcessTasks
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> selectedTask.set(tblProcessTasks
								.getSelectionModel().getSelectedItem()));
		Label lblProcessTasks = new Label("");
		Label lblTaskMsg = new Label("");
		lblTaskMsg.textProperty().bind(strTaskOperationResult);
		lblProcessTasks.textProperty().bind(strProcessInfo);
		t.setContent(new VBox(10, lblProcessTasks, tblProcessTasks,
				refreshButton(this::loadProcessTasks), lblTaskMsg));
		return t;
	}

	private Tab createUserTasksPane() {
		TableView<TaskSummary> tblUserTasks = new TableView<>();
		ComboBox<Status> cmbStatus = new ComboBox<>();
		cmbStatus.getItems().add(null);
		cmbStatus.getItems().addAll(Status.values());
		tblUserTasks.setContextMenu(taskContextMenu);
		tblUserTasks.getColumns().addAll(createTasksColumns());
		// everytime we selected a task
		tblUserTasks
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> selectedTask.set(tblUserTasks
								.getSelectionModel().getSelectedItem()));

		allTasksUsers.addListener(new ListChangeListener<TaskSummary>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TaskSummary> c) {
				tblUserTasks.getItems().setAll(c.getList());
			}
		});

		cmbStatus
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(obs, o, n) -> {
							tblUserTasks.getItems().clear();
							if (n == null)
								tblUserTasks.getItems()
										.setAll(allTasksUsers);
							else
								allTasksUsers
										.stream()
										.filter(s -> n.name().equals(
												s.getStatus().name()))
										.forEach(tblUserTasks.getItems()::add);
						});
		Label lblTaskError = new Label("");
		lblTaskError.textProperty().bind(strTaskOperationResult);
		Tab t = new Tab("User Tasks");
		HBox tools = new HBox(10, refreshButton(this::loadAllUserTasks),
				new Label("Status"), cmbStatus);
		t.setContent(new VBox(tblUserTasks, tools, lblTaskError));
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
		runLater(() -> {
			allProcessInstances.setAll(FXCollections
					.observableArrayList(controller.allProccessInstances()));
		});
	}

	private void createProcessInstance(String name) {
		runLater(() -> {
			controller.createProcessInstance(name, null, s -> {
				strProcessCreationResult.set(s);
				loadAllProcessInstances();
			}, strProcessCreationResult::set);

		});
	}

	private void loadProcessTasks() {
		if (selectedProcess.get() == null)
			return;
		long id = selectedProcess.get().getProcessInstanceId();
		runLater(() -> tblProcessTasks.getItems().setAll(
				controller.tasksByProcessInstanceId(id)));
	}

	private void loadAllUserTasks() {
		runLater(() -> allTasksUsers.setAll(FXCollections
				.observableArrayList(controller.allTasks())));
	}

	private void runLater(Runnable r) {
		Platform.runLater(() -> {
			r.run();
		});
	}

	private Button refreshButton(Runnable r) {
		Button btnRefresh = new Button("Refresh");
		btnRefresh.setOnAction(e -> {
			r.run();
		});
		return btnRefresh;
	}
}