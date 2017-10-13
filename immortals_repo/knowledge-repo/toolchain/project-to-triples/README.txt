IMMoRTALS Project To Triples Gradle Plugin:

Use:

The Gradle Plugin uses a significant amount of memory. You need to expand gradle past its default limits.
Where your parent gradle file is you need in your gradle.properties file:
org.gradle.jvmargs= -Xmx8g

in testing the plugin generally used ~4GB but better safe than sorry.

You need to have installed IMMoRTALS Project To Triples to your local repository for the following to work. It uses the plugin as it is installed in mavenLocal()
In the build.gradle file for a project for which you wish to apply the Project To Triples plugin you need to add this:

	buildscript {
	    repositories {
		mavenLocal()
	    }
	    dependencies {
	        classpath group: 'com.securboration', name: 'immortals-project-to-triples', version: 'r2.0.0'
	    }
	}

Then, also in the build.gradle file, you add this call:

	apply plugin: 'com.securboration.p2t'

Project To Triples attaches its functionality to the tail end of the build cycle ('afterEvaluate'), so order generally doesn't matter.

Output:
	In the directory that Gradle was called from there will be a series of .ttl files with the name format:
	[project-name]-output.ttl

	Each one corresponds to a gradle project that was analyzed.