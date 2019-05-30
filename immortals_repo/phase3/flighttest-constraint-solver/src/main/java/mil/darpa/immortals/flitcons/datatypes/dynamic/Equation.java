package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Equation implements DuplicateInterface<Equation>, Comparator<Equation>, Comparable<Equation> {
	public final String Equation;

	public Equation(@Nonnull String equation) {
		this.Equation = equation;
	}

	public String toString() {
		return Equation;
	}

	@Override
	public Equation duplicate() {
		return new Equation(this.Equation);
	}

	@Override
	public int compareTo(Equation equation) {
		if (equation == null) {
			return 1;
		}
		return this.Equation.compareTo(equation.Equation);
	}

	@Override
	public int compare(mil.darpa.immortals.flitcons.datatypes.dynamic.Equation equation, mil.darpa.immortals.flitcons.datatypes.dynamic.Equation t1) {
		if (equation == null && t1 == null) {
			return 0;
		}

		if (equation == null) {
			return -1;
		}
		return equation.compareTo(t1);
	}

	public Set<String> getVariables() {
		Set<String> values = new HashSet<>();

		int startIdx = Equation.indexOf("@");

		while (startIdx >= 0) {
			int endIdx = Equation.indexOf(" ", startIdx + 1);
			if (endIdx < 0) {
				endIdx = Equation.length();
			}

			String value = Equation.substring(startIdx + 1, endIdx);
			values.add(value);
			startIdx = Equation.indexOf("@", endIdx);
		}
		return values;
	}
}
