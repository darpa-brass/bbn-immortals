package mil.darpa.immortals.core.das;

import java.io.Serializable;

public class SUTInformation implements Serializable {

	static {
		instance = new SUTInformation();
	}
	
	private SUTInformation() {}
	
	public static SUTInformation getInstance() {
		return instance;
	}
	
	public String getVersion() {
		return version;
	}

	public String getAbout() {
		return about;
	}
	
	private static final long serialVersionUID = 7476920203019342401L;
	
	private static SUTInformation instance;
	
	private final String version = "1.0";
    private final String about = "bbn.immortals";
}
