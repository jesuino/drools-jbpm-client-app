package com.redhat.gss.bpms.view;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.geometry.HPos;
import java.util.stream.Stream;
import java.net.URL;
import javafx.beans.property.*;
import org.kie.services.client.api.RemoteRestRuntimeFactory;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.api.task.model.TaskSummary;
import javafx.scene.control.cell.*;

public class App extends Application{

	RemoteRuntimeEngine engine = null;
	Pane initialPane;

	private TextField txtDeployment;
	private TextField txtUrl;
	private TextField txtUsername;
	private PasswordField txtPassword;
	private Label lblLoginError;
	private TableView<TaskSummary> tblTasks;
	private BooleanProperty logged = new SimpleBooleanProperty();
	private StringProperty deploymentId = new SimpleStringProperty();
	private StringProperty user = new SimpleStringProperty();

	public static void main(String args[]){
		launch();	
	}
		
	@Override
	public void start(Stage stage){
		stage.setScene(new Scene(new StackPane(createInitialPane())));
		stage.setWidth(400);
		stage.setHeight(500);
		stage.setTitle("JavaFX 8 app");
		stage.show();
	}

	private TitledPane createInitialPane(){
		GridPane loginPane = new GridPane();
		loginPane.setHgap(10); loginPane.setVgap(15);
		Button btnLoginAction = new Button("Go");
		loginPane.add(new Label("URL"), 0, 0);
		loginPane.add(txtUrl = new TextField(), 1, 0);

		loginPane.add(new Label("Username"), 0, 1);
		loginPane.add(txtUsername = new TextField(), 1, 1);
		
		loginPane.add(new Label("Password"), 0, 2);
		loginPane.add(txtPassword = new PasswordField(), 1, 2);
	
		loginPane.add(new Label("Deployment"), 0, 3);
		loginPane.add(txtDeployment = new TextField(), 1, 3);
	
		loginPane.add(btnLoginAction, 0, 4, 2, 1);
		loginPane.add(lblLoginError = new Label(), 0, 5, 2, 1);	
		
		GridPane.setHalignment(btnLoginAction, HPos.CENTER);

		btnLoginAction.setOnAction(e -> retrieveEngine());
		TitledPane pnl = new TitledPane("Access BPMS", loginPane);
		pnl.setCollapsible(false);
		pnl.visibleProperty().bind(logged.not());
		return 	pnl;
	}

	private Node createDeploymentPane(){
		VBox pnlAllData = new VBox(10);
		Label lblDeployment = new Label();
		Button btnRefresh = new Button("Refresh");
		lblDeployment.textProperty().bind(deploymentId);
		Label lblTasks = new Label();
		lblTasks.textProperty().bind(new SimpleStringProperty("Tasks from User ").concat(user));
		createTaskTable();
		pnlAllData.getChildren().addAll(lblDeployment, lblTasks, btnRefresh, tblTasks);
		return pnlAllData;	
	}

	private void createTaskTable(){
		tblTasks = new TableView<>();
		tblTasks.getColumns().addAll(makeColumn("ID", "id"),makeColumn("Name", "name"),makeColumn("Status", "status"),makeColumn("Owner", "owner"),makeColumn("Priority", "priority"),makeColumn("Subject", "subject"));

	}

	private TableColumn<TaskSummary, String> makeColumn(String colName, String property){
		TableColumn<TaskSummary, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory(property));		
		return col;
	}

	private void retrieveEngine (){
		String errorMessage = "";
		String url = txtUrl.getText();
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		String deployment = txtDeployment.getText();

		if(checkEmpty(url, username, password, deployment)){
			errorMessage = "Please fill all fields!!";
		}
		else{
			try{
				engine = new RemoteRestRuntimeFactory(deployment,new URL(url),username, password).newRuntimeEngine();
				logged.setValue(true);	
				user.set(username);
				deploymentId.set(deployment);
			}
			catch(Exception e){
				errorMessage = e instanceof java.net.MalformedURLException ? "Check the URL format..." : e.getMessage();
				e.printStackTrace();
			}
		}
		lblLoginError.setText(errorMessage);
	}
	
	private boolean checkEmpty(String... values){
		return Stream.of(values).filter(s -> s.trim().isEmpty()).count() != 0;
	}
 }
