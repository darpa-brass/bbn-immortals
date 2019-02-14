package com.securboration.immortals.ontology.bytecode;

public class Dependency
{
    public BytecodeArtifactCoordinate coordinate;
    public String type;
    public String classifier;
    public String scope;
    public String systemPath;
    public String optional;
    public DependencyExclusion[] exclusions;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<dependency>\n");
        sb.append("\t<groupId>"); sb.append(coordinate.getGroupId()); sb.append("</groupId>\n");
        sb.append("\t<artifactId>"); sb.append(coordinate.getArtifactId()); sb.append("</artifactId>\n");
        sb.append("\t<version>"); sb.append(coordinate.getVersion()); sb.append("</version>\n");

        if(type != null)
        {
            sb.append("\t<type>"); sb.append(type); sb.append("</type>\n");
        }

        if(classifier != null)
        {
            sb.append("\t<classifier>"); sb.append(classifier); sb.append("</classifier>\n");
        }

        if(scope != null)
        {
            sb.append("\t<scope>"); sb.append(scope); sb.append("</scope>\n");
        }

        if(systemPath != null)
        {
            sb.append("\t<systemPath>"); sb.append(systemPath); sb.append("</systemPath>\n");
        }

        if(optional != null)
        {
            sb.append("\t<optional>"); sb.append(optional); sb.append("</optional>\n");
        }

        if(exclusions != null)
        {
            sb.append("\t<exclusions>\n");
            for(DependencyExclusion d:exclusions){
                sb.append(d.toString());
            }
            sb.append("\n\t</exclusions>\n");
        }

        sb.append("</dependency>");

        return sb.toString();
    }
}
