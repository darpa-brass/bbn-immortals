package mil.darpa.immortals.orientdbserver;

import java.util.Deque;
import java.util.LinkedList;

public class NestedException extends Exception {

	private final Deque<String> path = new LinkedList<>();

	public NestedException(String pathHead, String value) {
		super(value);
		path.push(pathHead);
	}

	public void addPathParent(String parent) {
		path.push(parent);
	}

	@Override
	public String getMessage() {
		return String.join(".", path) + ": " + super.getMessage();
	}
}
