package com.securboration.immortals.ontology.bytecode;

public class DependencyExclusion
{
    public String artifactId;
    public String groupId;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\t\t<exclusion>\n");
        sb.append("\t\t\t<groupId>"); sb.append(groupId); sb.append("</groupId>\n");
        sb.append("\t\t\t<artifactId>"); sb.append(artifactId); sb.append("</artifactId>\n");
        sb.append("\t\t</exclusion>");

        return sb.toString();
    }
}
