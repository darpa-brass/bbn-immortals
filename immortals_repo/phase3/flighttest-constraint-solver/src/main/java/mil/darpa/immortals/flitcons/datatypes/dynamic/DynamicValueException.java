package mil.darpa.immortals.flitcons.datatypes.dynamic;

import java.util.Deque;
import java.util.LinkedList;

/**
 * This exception class is intended to allow easy "chaining" of nested exceptions, helping indicate where in a
 * graph or tree the faulty element actually is.
 */
public class DynamicValueException extends Exception {

	private final Deque<String> path = new LinkedList<>();

	public DynamicValueException(String pathHead, String value) {
		super(value);
		path.push(pathHead);
	}

	public void addPathParent(String parent) {
		path.push(parent);
	}

	@Override
	public String getMessage() {
		return String.join(".", path) + super.getMessage();
	}
}
