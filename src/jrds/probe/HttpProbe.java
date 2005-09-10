package jrds.probe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jrds.JrdsLogger;
import jrds.Probe;
import jrds.ProbeDesc;
import jrds.RdsHost;

import org.apache.log4j.Logger;


/**
 * @author bacchell
 *
 * TODO 
 */
public abstract class HttpProbe extends Probe  implements IndexedProbe {
	static final private Logger logger = JrdsLogger.getLogger(HttpResponseTimeRrd.class);
	private URL url;

	/**
	 * @param monitoredHost
	 */
	public HttpProbe(RdsHost monitoredHost, ProbeDesc pd, URL url) {
		super(monitoredHost, pd);
		this.url = url;
	}
	
	protected abstract Map parseLines(List lines);
	/* (non-Javadoc)
	 * @see com.aol.jrds.Probe#getNewSampleValues()
	 */
	public Map getNewSampleValues() {
		Map vars = null;
		BufferedReader  in;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			List lines = new ArrayList();
			String lastLine;
			while((lastLine = in.readLine()) != null)
				lines.add(lastLine);
			in.close();
			vars = parseLines(lines);
		} catch (IOException e) {
			logger.error("Unable to read url " + url + "because: " + e.getLocalizedMessage());
		}
		return vars;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrlAsString() {
		return getUrl().toString();
	}

	/**
	 * @return Returns the url.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}
	/**
	 * @see jrds.probe.IndexedProbe#getIndexName()
	 */
	public String getIndexName() {
		return url.toString();
	}
}
