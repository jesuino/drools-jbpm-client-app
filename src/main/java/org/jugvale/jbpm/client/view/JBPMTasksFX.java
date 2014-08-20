/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jugvale.jbpm.client.view;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jugvale.jbpm.client.controller.JBPMController;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * Does JBPM REST operation using JavaFX Tasks.
 *
 * @author wsiqueir
 */
public class JBPMTasksFX {

    private JBPMController controller;

    public void doTaskOperation() {
    }

    public void loadProcessTasks(ProcessInstanceLog p,
            Consumer<List<TaskSummary>> onSuccess, Consumer<String> onFail) {
          new Thread(new Task<List<TaskSummary>>() {

            @Override
            protected List<TaskSummary> call() throws Exception {
                return controller.tasksByProcessInstanceId(p.getId());
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess.accept(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable e = getException();
                e.printStackTrace();
                onFail.accept("Error: " + e.getMessage());
            }

        }).start();       
    }

    public void loadAllProcessInstances(Consumer<List<ProcessInstanceLog>> onSuccess, Consumer<String> onFail) {

        new Thread(new Task<List<ProcessInstanceLog>>() {

            @Override
            protected List<ProcessInstanceLog> call() throws Exception {
                return controller.allProccessInstances();
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess.accept(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable e = getException();
                e.printStackTrace();
                onFail.accept("Error: " + e.getMessage());
            }

        }).start();
    }

    public void createProcessInstance(String name, HashMap<String, Object> params, Consumer<String> onSuccess, Consumer<String> onFail) {
        new Thread(new Task<String>() {

            @Override
            protected String call() throws Exception {
                return controller.createProcessInstance(name, params);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess.accept(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable e = getException();
                e.printStackTrace();
                onFail.accept("Error: " + e.getMessage());
            }

        }).start();
    }

    public void loadAllUserTasks(Consumer<List<TaskSummary>> onSuccess, Consumer<String> onFail) {
        new Thread(new Task<List<TaskSummary>>() {

            @Override
            protected List<TaskSummary> call() throws Exception {
                return controller.allTasks();
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess.accept(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable e = getException();
                e.printStackTrace();
                onFail.accept("Error: " + e.getMessage());
            }

        }).start();
    }

}
