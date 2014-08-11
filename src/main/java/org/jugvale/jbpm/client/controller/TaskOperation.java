package org.jugvale.jbpm.client.controller;

import java.util.stream.Stream;

public enum TaskOperation {
	COMPLETE("Complete"), START("Start"), ACTIVATE("Activate"), CLAIM("Claim"), FAIL(
			"Fail"), EXIT("Exit");

	String name;

	private TaskOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	static public TaskOperation get(String other) {
		if (other == null || other.trim().isEmpty())
			return null;
		return Stream.of(values()).filter(v -> other.equals(v.toString())).findFirst()
				.get();
	}
}