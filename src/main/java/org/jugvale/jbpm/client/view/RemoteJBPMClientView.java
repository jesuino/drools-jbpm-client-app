package org.jugvale.jbpm.client.view;

import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.controller.JBPMController;
import org.jugvale.jbpm.client.controller.TaskOperation;
import static org.jugvale.jbpm.client.view.ControlsFactory.processColumns;
import static org.jugvale.jbpm.client.view.ControlsFactory.taskColumns;
import static org.jugvale.jbpm.client.view.ControlsFactory.refreshButton;
import static org.jugvale.jbpm.client.view.ControlsFactory.title;
import static org.jugvale.jbpm.client.view.ControlsFactory.warning;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * A view for the main remote capabilities of the jBPM API
 *
 * @author william
 *
 */
// TODO: avoid tasks issues when invoking tasks functions (disable the menus that can't be accessed)
// TODO: parameters when doing certains op with tasks
// TODO: parameters when creating process instances
// TODO: explore more methods of the API
// TODO: Check why the tasks are not receiving parameters
// TODO: improve process list view
// TODO: create charts with the process data
// TODO: Create a context menu for process as well
// TOPO: migrate to JBPMTasksFX and create some visual thing to let user know things are being loaded
public class RemoteJBPMClientView extends TabPane {

    private JBPMController controller;
    JBPMTasksFX tasks;

    private ObservableList<ProcessInstanceLog> allProcessInstances;
    private ObservableList<TaskSummary> allTasksUsers;
    private ObservableList<TaskSummary> processTasks;
    private ObjectProperty<TaskSummary> selectedTask;
    private ObjectProperty<ProcessInstanceLog> selectedProcess;

    ContextMenu taskContextMenu;
    private StringProperty strTaskOperationResult;
    private StringProperty strProcessCreation;

    public RemoteJBPMClientView() {
        initialize();
        build();
        settings();
    }

    private void initialize() {
        allProcessInstances = FXCollections.observableArrayList();
        allTasksUsers = FXCollections.observableArrayList();
        processTasks = FXCollections.observableArrayList();
        strTaskOperationResult = new SimpleStringProperty();
        selectedTask = new SimpleObjectProperty<>();
        strProcessCreation = new SimpleStringProperty();
        selectedProcess = new SimpleObjectProperty<>();
        // when we change the selected Process, we must change the tasks 
        selectedProcess.addListener((chg, o, n) -> loadProcessTasks(n));

    }

    private void settings() {
        setSide(Side.LEFT);
        setMinHeight(600);        
        getTabs().forEach(t -> t.setClosable(false));
    }

    private void build() {
        createTasksContextMenu();
        Tab processTaskTab = createProcessTasksPane();
        getTabs().addAll(welcomeTab(), createTab(),
                createProcessesInstancePane(), processTaskTab,
                createUserTasksPane());
    }

    private Tab welcomeTab() {
        Tab t = new Tab("Welcome");
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
        VBox v = new VBox(10, title("BPM 6 Client"), lblSummary);
        v.setTranslateX(20);
        v.setTranslateY(20);
        t.setContent(v);
        return t;
    }

    private Tab createTab() {
        Tab t = new Tab("Create");
        VBox content = new VBox(10);
        HBox hbCreateProcessInstance = new HBox(10);
        HBox hbCreateTask = new HBox(10);
        Label lblTitle = title("Create tasks and process instances");
        Label lblProcessCreationMessage = warning("");
        Label lblTaskCreationMessage = warning("");
        lblProcessCreationMessage.textProperty().bind(strProcessCreation);
        Button btnCreateProcessInstance = new Button("Create");
        TextField txtProcessName = new TextField();

        hbCreateProcessInstance.getChildren().setAll(
                new Label("Process name:"), txtProcessName, btnCreateProcessInstance);
        hbCreateTask.getChildren().addAll(new Label("New Task name:"),
                new TextField(), new Button("Create"));

        content.getChildren().addAll(lblTitle,
                new Label("Create Process Instance"), hbCreateProcessInstance, lblProcessCreationMessage,
                new Separator(Orientation.HORIZONTAL),
                new Label("Create Task"), hbCreateTask, lblTaskCreationMessage);
        content.setTranslateX(20);
        content.setTranslateY(20);
        t.setContent(content);
        btnCreateProcessInstance.setOnAction(e -> createProcessInstance(txtProcessName.getText()));
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
        if (selectedTask.get() == null) {
            return;
        }
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
        loadProcessTasks(selectedProcess.get());
    }

    private Tab createProcessesInstancePane() {
        TableView<ProcessInstanceLog> tblProcessesInstances = new TableView<>();
        FilteredList<ProcessInstanceLog> tblData = new FilteredList<>(
                allProcessInstances, e -> true);
        ComboBox<Integer> cmbStatus = new ComboBox<>(
                FXCollections.observableArrayList(null, 1, 2, 3));
        tblProcessesInstances.getColumns().addAll(processColumns());
        tblProcessesInstances.setItems(tblData);
        Tab t = new Tab("Processes Instances");
        cmbStatus
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, o, n) -> {
                            tblData.setPredicate(p -> n == null ? true : p
                                    .getStatus() == n);
                        });
        HBox bottomBar = new HBox(20,
                refreshButton(this::loadAllProcessInstances), new Label(
                        "Status"), cmbStatus);
        t.setContent(new VBox(10, title("Process Active Instances"),
                tblProcessesInstances, bottomBar));
        return t;
    }

    private Tab createProcessTasksPane() {
        ListView<ProcessInstanceLog> lstProcesses = new ListView<>(
                allProcessInstances);
        TableView<TaskSummary> tblProcessTasks = new TableView<>();
        Label lblTaskMsg = warning("");
        Tab t = new Tab("Process Tasks");
        tblProcessTasks.setContextMenu(taskContextMenu);
        tblProcessTasks.getColumns().addAll(taskColumns());
        tblProcessTasks.setItems(processTasks);
        lblTaskMsg.textProperty().bind(strTaskOperationResult);
        tblProcessTasks.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedTask.set(n);
        });
        lstProcesses.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedProcess.set(n);
            }
        });
        t.setContent(new VBox(10, title("Tasks by process"), new HBox(10,
                lstProcesses, tblProcessTasks),
                refreshButton(this::loadAllProcessInstances), lblTaskMsg));
        return t;
    }

    private Tab createUserTasksPane() {
        TableView<TaskSummary> tblUserTasks = new TableView<>();
        ComboBox<Status> cmbStatus = new ComboBox<>();
        FilteredList<TaskSummary> tblData = new FilteredList<>(allTasksUsers,
                e -> true);
        cmbStatus.getItems().add(null);
        cmbStatus.getItems().addAll(Status.values());
        tblUserTasks.setContextMenu(taskContextMenu);
        tblUserTasks.getColumns().addAll(taskColumns());
        tblUserTasks.setItems(tblData);
        tblUserTasks
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, o, n)
                        -> selectedTask.set(tblUserTasks
                                .getSelectionModel().getSelectedItem()));
        cmbStatus
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, o, n) -> {
                    tblData.setPredicate(e -> n == null ? true : n
                            .name().equals(e.getStatus().name()));
                });
        Label lblTaskError = warning("");
        lblTaskError.textProperty().bind(strTaskOperationResult);
        Tab t = new Tab("User Tasks");
        HBox tools = new HBox(10, refreshButton(this::loadAllUserTasks),
                new Label("Status"), cmbStatus);
        t.setContent(new VBox(title("Your Tasks"), tblUserTasks, tools,
                lblTaskError));
        return t;
    }

    // TODO Move controller iteractions to JBPMTasksFX
    private void loadProcessTasks(ProcessInstanceLog p) {
        if (p != null) {
            runLater(() -> {
                processTasks.setAll(controller.tasksByProcessInstanceId(p.getId()));
            });
        }
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
                strProcessCreation.set(s);
                loadAllProcessInstances();
            }, strProcessCreation::set);
        });
    }

    private void loadAllUserTasks() {
        runLater(() -> {
            allTasksUsers.setAll(controller.allTasks());
        });
    }

    private void runLater(Runnable r) {
        Platform.runLater(r::run);
    }
}
