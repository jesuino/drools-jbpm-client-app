package org.jugvale.jbpm.client.view;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.jugvale.jbpm.client.controller.JBPMController;
import org.kie.api.task.model.TaskSummary;

/**
 * 
 * A view for the main remote capabilities of the jBPM API
 * 
 * @author william
 *
 */
public class RemoteJBPMClientView extends Accordion {

	private JBPMController controller;
	private TitledPane pnlTasks;
	private TableView<TaskSummary> tblTasks;

	/**
	 * A property to indicate when we are doing heavy tasks
	 */
	private BooleanProperty loadingTasks = new SimpleBooleanProperty();

	public RemoteJBPMClientView() {
		build();
		settings();
		
	}

	private void settings() {
		setExpandedPane(pnlTasks);
		setMinHeight(500);
		
	}

	public void setController(JBPMController controller) {
		this.controller = controller;
		loadTasksFromServerData();
	}

	private void build() {
		getPanes().addAll(createTasksPane(), createManageTaskPane());
	}

	private TitledPane createManageTaskPane() {		
		return null;
	}

	@SuppressWarnings("unchecked")
	private TitledPane createTasksPane() {
		ProgressIndicator progress = new ProgressIndicator();
		tblTasks = new TableView<>();
		tblTasks.getColumns().addAll(makeColumn("ID", "id", 100),
				makeColumn("Name", "name", 100), makeColumn("Status", "status", 100),
				makeColumn("Priority", "priority", 100),
				makeColumn("Created on", "createdOn", 150),
				makeColumn("Expiration Time", "expirationTime", 150),
				makeColumn("Status", "status", 100));
		progress.visibleProperty().bind(loadingTasks);
		tblTasks.disableProperty().bind(loadingTasks);
		Button btnRefresh = new Button("Refresh");
		btnRefresh.setOnAction(e -> {
			loadTasksFromServerData();
		});
		return pnlTasks = new TitledPane("All tasks for the logged User",
				new VBox(btnRefresh, new StackPane(progress, tblTasks)));
	}

	private TableColumn<TaskSummary, String> makeColumn(String colName,
			String property, double width) {
		TableColumn<TaskSummary, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(property));
		col.setMinWidth(width);
		return col;
	}

	private void loadTasksFromServerData() {
		Platform.runLater(() -> {
			loadingTasks.set(true);
			tblTasks.getItems().setAll(controller.allTasks());
			loadingTasks.set(false);
		});
	}
}