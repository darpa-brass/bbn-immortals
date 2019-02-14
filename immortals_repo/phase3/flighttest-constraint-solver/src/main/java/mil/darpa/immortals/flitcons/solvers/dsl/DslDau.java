package mil.darpa.immortals.flitcons.solvers.dsl;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Usage details utilized for matching up to a port
 */
public class DslDau {

	public String GloballyUniqueId;

	public List<DslPort> Port;

	/**
	 * Whether or not this DAU is flagged for replacement
	 */
	public Boolean BBNDauFlaggedForReplacement;

	public Integer BBNDauOpportunityCost;
	public Integer BBNDauMonetaryCost;

	public List<String> SupersededDauIds;

	public DslDau() {
	}
}
