/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jugvale.jbpm.client.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * Used to add all component creation
 *
 * @author wsiqueir
 */
public class ControlsFactory {

    private ControlsFactory() {
    }

    @SuppressWarnings("unchecked")
    public static ObservableList<TableColumn<TaskSummary, String>> taskColumns() {
        return FXCollections.observableArrayList(
                makeTaskColumn("ID", "id", 50),
                makeTaskColumn("Name", "name", 150),
                makeTaskColumn("Status", "status", 120),
                makeTaskColumn("Priority", "priority", 80),
                makeTaskColumn("Created on", "createdOn", 220),
                makeTaskColumn("Expiration Time", "expirationTime", 220));
    }

    public static ObservableList<TableColumn<ProcessInstanceLog, String>> processColumns() {
        return FXCollections.observableArrayList(
                makeProcessColumn("Process ID", "processId", 140),
                makeProcessColumn("ID", "processInstanceId", 20),
                makeProcessColumn("Name", "processName", 100),
                makeProcessColumn("Version", "processVersion", 80),
                makeProcessColumn("Status", "status", 20),
                makeProcessColumn("Start Date", "start", 180),
                makeProcessColumn("End Date", "end", 180),
                makeProcessColumn("Outcome", "outcome", 100),
                makeProcessColumn("Duration", "duration", 100));
    }

    private static TableColumn<ProcessInstanceLog, String> makeProcessColumn(
            String colName, String property, double width) {
        return makeColumn(ProcessInstanceLog.class, colName, property, width);
    }

    private static TableColumn<TaskSummary, String> makeTaskColumn(String colName,
            String property, double width) {
        return makeColumn(TaskSummary.class, colName, property, width);
    }

    private static <T> TableColumn<T, String> makeColumn(Class<?> t, String colName,
            String property, double width) {
        TableColumn<T, String> col = new TableColumn<>(colName);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(width);
        return col;
    }

    public static Label title(String text) {
        Label lblTitle = new Label(text);
        lblTitle.getStyleClass().add("tab-title");
        return lblTitle;
    }

    public static Label warning(String text) {
        Label lblWarning = new Label(text);
        lblWarning.getStyleClass().add("warning");
        return lblWarning;
    }

    public static Button refreshButton(Runnable r) {
        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> r.run());
        return btnRefresh;
    }
}
