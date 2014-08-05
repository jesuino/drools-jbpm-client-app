package org.jugvale.jbpm.client.view;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Accordion;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;

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
	private TableView<TaskSummary> tblTasks;

	/**
	 * A property to indicate when we are doing heavy tasks
	 */
	private BooleanProperty loadingTasks = new SimpleBooleanProperty();

	public RemoteJBPMClientView() {
		build();
	}

	public void setController(JBPMController controller) {
		this.controller = controller;
		loadInfoFromServerData();
	}

	private void build() {
		getPanes().addAll(createTasksPane());
	}

	@SuppressWarnings("unchecked")
	private TitledPane createTasksPane() {
		ProgressIndicator progress = new ProgressIndicator();
		tblTasks = new TableView<>();
		tblTasks.getColumns().addAll(makeColumn("ID", "id"),
				makeColumn("Name", "name"), makeColumn("Status", "status"),
				makeColumn("Owner", "owner"),
				makeColumn("Priority", "priority"),
				makeColumn("Subject", "subject"));
		progress.visibleProperty().bind(loadingTasks);
		tblTasks.disableProperty().bind(loadingTasks);
		return new TitledPane("All tasks for the logged User", tblTasks);
	}

	private TableColumn<TaskSummary, String> makeColumn(String colName,
			String property) {
		TableColumn<TaskSummary, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(property));
		return col;
	}

	private void loadInfoFromServerData() {
		Platform.runLater(() -> {
			loadingTasks.set(true);
			tblTasks.getItems().setAll(controller.allTasks());
			loadingTasks.set(false);
		});
	}
}