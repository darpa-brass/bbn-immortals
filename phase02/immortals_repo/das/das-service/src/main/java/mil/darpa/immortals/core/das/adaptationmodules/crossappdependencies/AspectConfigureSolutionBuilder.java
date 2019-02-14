package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.core.das.adaptationmodules.hddrass.Hacks;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineCrossAppApplicability;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by awellman@bbn.com on 6/29/18.
 */
public class AspectConfigureSolutionBuilder {

    private final Model m;
    private final CipherConfiguration configuration;
    private final String javaxDfuInstanceUri;
    private final String bcDfuInstanceUri;

    static Resource getPropertyByType(Model model, Res parentRes, Prop prop, Resource type) {
        Resource parentResource = model.getResource(parentRes.uri);
        Property property = model.getProperty(prop.toString());
        for (RDFNode obj : model.listObjectsOfProperty(parentResource, property).toList()) {
            if (obj.isResource()) {
                Resource objResource = obj.asResource();
                if (objResource.hasProperty(RDF.type, type)) {
                    return objResource;
                }
            }
        }
        return null;
    }

    private Resource getPropertyByType(Res parentResource, Prop property, Resource type) {
        return getPropertyByType(m, parentResource, property, type);
    }

    private Resource getResource(Res res) {
        return m.createResource(res.uri);
    }

    private List<Resource> getResourceInstances(Res resType) {
        return m.listResourcesWithProperty(RDF.type, m.getResource(resType.toString())).toList();
    }

    private Resource addResourceInstance(Res res) {
        Resource r = m.createResource(res.uri + "-" + UUID.randomUUID().toString());
        r.addProperty(RDF.type, getResource(res));
        return r;
    }

    private void addLiteralToResource(Resource resource, Prop prop, String val) {
        Property p = m.createProperty(prop.uri);
        resource.addLiteral(p, val);
    }

    public AspectConfigureSolutionBuilder(@Nonnull CipherConfiguration cipherConfiguration, @Nonnull Model existingModel, String javaxDfuInstanceUri, String bcDfuInstanceUri) {
        this.configuration = cipherConfiguration;
        this.javaxDfuInstanceUri = javaxDfuInstanceUri;
        this.bcDfuInstanceUri = bcDfuInstanceUri;
        this.m = existingModel;

        for (Prefix p : Prefix.values()) {
            m.setNsPrefix(p.name(), p.uri);
        }
    }

    public Resource addLooseResourceInstanceToResource(@Nonnull Resource resource, @Nonnull Prop prop, @Nonnull String looseResource) {
        Property p = m.getProperty(prop.uri);
        Resource r = m.createResource(looseResource);
        resource.addProperty(p, r);
        return r;
    }

    public Resource addResourceInstanceToResource(@Nonnull Resource resource, @Nonnull Prop prop, @Nonnull Res res) {
        Property p = m.getProperty(prop.uri);
        Resource r = getResource(res);
        Resource ri = m.createResource(res.uri + "-" + UUID.randomUUID().toString());
        ri.addProperty(RDF.type, r);
        resource.addProperty(p, ri);
        return ri;
    }

    public Resource addResourceToResource(@Nonnull Resource resource, @Nonnull Prop prop, @Nonnull Res res) {
        Property p = m.getProperty(prop.uri);
        Resource r = getResource(res);
        resource.addProperty(p, r);
        return r;
    }

    public synchronized Model buildSolution() throws Exception {
        Resource solution = addResourceInstance(Res.ASPECT_CONFIGURE_SOLUTION);
        // KRHACK: These hacks to get the instance URIs should not be necessary!
        addLooseResourceInstanceToResource(solution, Prop.HAS_CHOSEN_INSTANCE,
                configuration.isClientJavax() ? Hacks.getJavaxDfuInstanceUri() : Hacks.getBcDfuInstanceUri());

        Resource algBinding = addResourceInstanceToResource(solution, Prop.HAS_CONFIGURATION_BINDINGS, Res.CONFIGURATION_BINDING);
        addResourceToResource(algBinding, Prop.HAS_SEMANTIC_TYPE, Res.CIPHER_ALGORITHM);
        addLiteralToResource(algBinding, Prop.HAS_BINDING, configuration.getCipherAlgorithm().replaceAll("_", "-"));

        Resource paddingSchemeBinding = addResourceInstanceToResource(solution, Prop.HAS_CONFIGURATION_BINDINGS, Res.CONFIGURATION_BINDING);
        addResourceToResource(paddingSchemeBinding, Prop.HAS_SEMANTIC_TYPE, Res.PADDING_SCHEME);
        addLiteralToResource(paddingSchemeBinding, Prop.HAS_BINDING, configuration.getPaddingScheme().replaceAll("_", "-"));

        Resource chainingModeBinding = addResourceInstanceToResource(solution, Prop.HAS_CONFIGURATION_BINDINGS, Res.CONFIGURATION_BINDING);
        addResourceToResource(chainingModeBinding, Prop.HAS_SEMANTIC_TYPE, Res.CIPHER_CHAINING_MODE);
        addLiteralToResource(chainingModeBinding, Prop.HAS_BINDING, configuration.getCipherChainingMode().replaceAll("_", "-"));

        Resource keyLengthBinding = addResourceInstanceToResource(solution, Prop.HAS_CONFIGURATION_BINDINGS, Res.CONFIGURATION_BINDING);
        addResourceToResource(keyLengthBinding, Prop.HAS_SEMANTIC_TYPE, Res.CIPHER_KEY_LENGTH);
        addLiteralToResource(keyLengthBinding, Prop.HAS_BINDING, Integer.toString(configuration.getKeyLength()));

        return m;
    }
}
