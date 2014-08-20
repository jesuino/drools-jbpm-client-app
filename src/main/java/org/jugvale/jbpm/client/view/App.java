package org.jugvale.jbpm.client.view;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.jugvale.jbpm.client.controller.JBPMController;

public class App extends Application {

	private RemoteJBPMClientView jbpmClientView;
	private final BooleanProperty session = new SimpleBooleanProperty(false);

	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		LoginPane loginPane = new LoginPane(this::success, null);
		jbpmClientView = new RemoteJBPMClientView();
		VBox sessionBox = new VBox(10);
		Button btnEndSession = new Button("End Session");
		btnEndSession.setOnAction(e -> session.set(false));
		sessionBox.getChildren().addAll(jbpmClientView, btnEndSession);
		sessionBox.visibleProperty().bind(session);
		loginPane.visibleProperty().bind(session.not());
		Scene scene = new Scene(new StackPane(loginPane, sessionBox));
		scene.getStylesheets().add("./app.css");
		stage.setScene(scene);
		stage.setWidth(1100);
		stage.setHeight(680);
		stage.setTitle("A JavaFX Client for jBPM 6");
		stage.show();
	}

	private void success(JBPMController controller) {
		session.set(true);
		jbpmClientView.setController(controller);
	}
}