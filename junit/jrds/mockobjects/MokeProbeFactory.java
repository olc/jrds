package jrds.mockobjects;

import java.util.Collections;
import java.util.Map;

import jrds.Probe;
import jrds.ProbeDesc;
import jrds.PropertiesManager;
import jrds.factories.GraphFactory;
import jrds.factories.ProbeFactory;

public class MokeProbeFactory extends ProbeFactory {
	static Map<String, ProbeDesc> probeDescMap = Collections.emptyMap();
	static GraphFactory gf = null;
	static PropertiesManager  pm = new PropertiesManager();
	static boolean legacymode = false;

	public MokeProbeFactory() {
		super(probeDescMap, gf, pm);
	}

	/* (non-Javadoc)
	 * @see jrds.factories.ProbeFactory#makeProbe(jrds.ProbeDesc, java.util.List)
	 */
	public Probe makeProbe(ProbeDesc pd) {
		return new MokeProbe();
	}
}
