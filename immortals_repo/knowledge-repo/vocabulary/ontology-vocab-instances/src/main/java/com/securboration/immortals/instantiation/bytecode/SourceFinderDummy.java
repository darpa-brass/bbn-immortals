package com.securboration.immortals.instantiation.bytecode;

import java.io.IOException;

public class SourceFinderDummy extends SourceFinder{
	public SourceFinderDummy(String projectRoot, String repositoryUrl, String... sourceRoots) throws IOException {
		super("", "", "");
	}

	@Override
	public SourceInfo getSourceInfo(String classInternalName) {
		return super.getSourceInfo(classInternalName);
	}
}
