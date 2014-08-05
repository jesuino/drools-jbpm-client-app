package org.jugvale.jbpm.client.view;

import java.net.MalformedURLException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.jugvale.jbpm.client.controller.JBPMController;
import org.jugvale.jbpm.client.model.LoginModel;

public class LoginPane extends TitledPane {

	private TextField txtDeployment;
	private TextField txtUrl;
	private TextField txtUsername;
	private PasswordField txtPassword;
	private Label lblLoginError;

	private BooleanProperty tryingToLogin = new SimpleBooleanProperty();

	private Consumer<JBPMController> success;
	private Consumer<Throwable> failure;

	public LoginPane() {
		build();
	}

	public LoginPane(Consumer<JBPMController> success,
			Consumer<Throwable> failure) {
		super();
		this.success = success;
		this.failure = failure;
		build();
	}

	private void build() {
		GridPane loginPane = new GridPane();
		ProgressIndicator prg = new ProgressIndicator();
		loginPane.setHgap(10);
		loginPane.setVgap(15);
		Button btnLoginAction = new Button("Go");
		loginPane.add(new Label("URL"), 0, 0);
		loginPane.add(txtUrl = new TextField(
				"http://www.localhost:8080/bpm-console"), 1, 0);

		loginPane.add(new Label("Username"), 0, 1);
		loginPane.add(txtUsername = new TextField("bpmuser"), 1, 1);

		loginPane.add(new Label("Password"), 0, 2);
		loginPane.add(txtPassword = new PasswordField(), 1, 2);

		loginPane.add(new Label("Deployment"), 0, 3);
		loginPane.add(txtDeployment = new TextField("mydeployment"), 1, 3);

		loginPane.add(btnLoginAction, 0, 4, 2, 1);
		loginPane.add(lblLoginError = new Label(), 0, 5, 2, 1);
		GridPane.setHalignment(btnLoginAction, HPos.CENTER);
		btnLoginAction.setOnAction(e -> retrieveController());
		setText("Access BPMS");
		setContent(new StackPane(prg, loginPane));
		setCollapsible(false);
		lblLoginError.setMaxWidth(300);
		lblLoginError.setWrapText(true);
		setMaxSize(320, 300);
		prg.visibleProperty().bind(tryingToLogin);
		disableProperty().bind(tryingToLogin);
	}

	private void retrieveController() {
		String url = txtUrl.getText();
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		String deployment = txtDeployment.getText();

		if (checkEmpty(url, username, password, deployment)) {
			showError("Please fill all fields!!");
		} else {

			LoginModel model = new LoginModel(username, password, deployment,
					url);
			tryingToLogin.set(true);
			Task<JBPMController> taskLogin = new Task<JBPMController>() {
				@Override
				protected JBPMController call() throws Exception {
					return JBPMController.logIn(model);
				}

				@Override
				protected void succeeded() {
					System.out.println("SUCCESS");
					tryingToLogin.set(false);
					if (success != null)
						try {
							success.accept(this.get());
						} catch (Exception e) {
							showError(e.getMessage());
						}
				}

				@Override
				protected void failed() {
					tryingToLogin.set(false);
					Throwable e = this.getException();
					if (e instanceof MalformedURLException) {
						showError("Check the URL format...");
					} else {
						showError(e.getMessage());
					}
					if (failure != null)
						failure.accept(e);
				}
			};
			new Thread(taskLogin).start();
		}
	}

	private void showError(String errorMessage) {
		lblLoginError.setText(errorMessage);
	}

	private boolean checkEmpty(String... values) {
		return Stream.of(values).filter(s -> s.trim().isEmpty()).count() != 0;
	}

	public void setSuccess(Consumer<JBPMController> success) {
		this.success = success;
	}

	public void setFailure(Consumer<Throwable> failure) {
		this.failure = failure;
	}

}
