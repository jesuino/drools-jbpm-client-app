package org.jugvale.jbpm.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoginModel {
	
	public StringProperty deploymentId = new SimpleStringProperty();
	public StringProperty username = new SimpleStringProperty();
	public StringProperty password  = new SimpleStringProperty();
	public StringProperty url = new SimpleStringProperty();
	
	public LoginModel(String strUserName, String strPassword, String strDeploymentdId, String strUrl) {
		this.username.set(strUserName);
		this.password.set(strPassword);
		this.url.set(strUrl);
		this.deploymentId.set(strDeploymentdId);
	}

}
