package mil.darpa.immortals.flitcons.validation;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueMultiplicity;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.apache.commons.io.FileUtils;
import org.drools.core.builder.conf.impl.DecisionTableConfigurationImpl;
import org.drools.decisiontable.DecisionTableProviderImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.DecisionTableInputType;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Set;

public abstract class DataValidator {

	public static String SOLVER_INPUT_USAGE_FILE = "validation-input-usage.json";
	public static String SOLVER_INPUT_REQUIREMENTS_FILE = "validation-input-requirements.json";
	public static String SOLVER_DAUINVENTORY_FILE = "validation-input-inventory.json";
	public static String SOLVER_OUTPUT_USAGE_FILE = "validation-output-usage.json";

	private static final Logger log = LoggerFactory.getLogger(DataValidator.class);

	private final File inputExcelFile;

	private final File outputDrlFile;

	private final Configuration.ValidationConfiguration validationConfiguration;

	private KieSession session;

	protected DataValidator(@Nullable File inputExcelFile, @Nullable File outputDrlFile) {
		this.inputExcelFile = inputExcelFile;
		this.outputDrlFile = outputDrlFile;
		this.validationConfiguration = Configuration.getInstance().validation;

	}

	protected synchronized ValidationDataContainer validate(@Nullable AgendaFilter filter, @Nonnull DynamicObjectContainer root) throws DynamicValueeException {
		init();
		ValidationDataContainer rval = ValidationDataContainer.createContainer(root, validationConfiguration);
		Set<ValidationData> vdcs = rval.getAllDataInHierarchy();

		for (ValidationData vdc : vdcs) {
			session.insert(vdc);
		}

		if (filter == null) {
			session.fireAllRules();
		} else {
			session.fireAllRules(filter);
		}
		return rval;
	}

	public void init() {
		try {
			Resource rs;

			if (inputExcelFile != null) {
				System.out.println("Loading rules from '" + inputExcelFile.getAbsolutePath() + "'");
				DecisionTableConfiguration configuration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
				configuration.setInputType(DecisionTableInputType.XLS);
				rs = ResourceFactory.newFileResource(inputExcelFile);
				DecisionTableProviderImpl decisionTableProvider = new DecisionTableProviderImpl();
				DecisionTableConfiguration dtc = new DecisionTableConfigurationImpl();
				String drl = decisionTableProvider.loadFromResource(rs, dtc);

				if (outputDrlFile != null) {
					FileUtils.writeStringToFile(outputDrlFile, drl, Charset.defaultCharset());
					log.info("Wrote DRL validation rules to '" + outputDrlFile.getAbsolutePath() + "'.");
				}

			} else {
				System.out.println("Loading rules from internal data");
				InputStream drl = DataValidator.class.getClassLoader().getResourceAsStream("CombinedValidationRules.drl");
				rs = ResourceFactory.newInputStreamResource(drl);
				rs.setSourcePath("/CombinedValidationRules.drl");
				rs.setResourceType(ResourceType.DRL);
			}

			KieServices kieServices = KieServices.Factory.get();
			KieFileSystem kfs = kieServices.newKieFileSystem().write(rs);
			KieBuilder kb = kieServices.newKieBuilder(kfs);
			kb.buildAll();

			Results results = kb.getResults();
			if (results.hasMessages(Message.Level.ERROR)) {
				StringBuilder err = new StringBuilder();
				for (Message msg : results.getMessages()) {
					err.append(msg.toString()).append("\n");
				}
				throw AdaptationnException.internal("DRL Creation errors:\n" + err.toString());
			}

			KieRepository kr = kieServices.getRepository();
			ReleaseId defaultReleaseId = kr.getDefaultReleaseId();
			KieContainer kc = kieServices.newKieContainer(defaultReleaseId);
			session = kc.newKieSession();

			session.setGlobal("SingleValue", DynamicValueMultiplicity.SingleValue);
			session.setGlobal("Set", DynamicValueMultiplicity.Set);
			session.setGlobal("Range", DynamicValueMultiplicity.Range);
		} catch (IOException e) {
			throw AdaptationnException.internal(e);
		}
	}
}
