package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import java.util.Comparator;

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
}
