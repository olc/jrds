package jrds.mockobjects;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jrds.GraphDesc;
import jrds.Probe;
import jrds.ProbeDesc;
import jrds.RdsHost;
import junit.framework.Assert;

import org.junit.rules.TemporaryFolder;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;

public class Full {
	static final long SEED = 1909752002L;
	static final Random RANDOM = new Random(SEED);
	static final String FILE = "fullmock";

	static final long START = Util.getTimestamp(2003, 4, 1);
	static final long END = Util.getTimestamp(2003, 5, 1);
	static final int STEP = 300;

	static final int IMG_WIDTH = 500;
	static final int IMG_HEIGHT = 300;
	
	static public ProbeDesc getPd() {
		ProbeDesc pd = new ProbeDesc();
		
		Map<String, Object> dsMap = new HashMap<String, Object>();
		dsMap.put("dsName", "sun");
		dsMap.put("dsType", DsType.GAUGE);
		pd.add(dsMap);
		
		dsMap.clear();
		dsMap.put("dsName", "shade");
		dsMap.put("dsType", DsType.GAUGE);
		pd.add(dsMap);

		pd.setName(FILE);
		pd.setProbeName(FILE);

		return pd;
	}
	
	static public GraphDesc getGd() {
		GraphDesc gd = new GraphDesc();

		gd.add("sun", null, GraphDesc.LINE.toString(), "green", null, null, null, null, null, null, null);
		gd.add("shade", null, GraphDesc.LINE.toString(), "blue", null, null, null, null, null, null, null);
		gd.add("median", "sun,shade,+,2,/", GraphDesc.LINE.toString(), "magenta", null, null, null, null, null, null, null);
		gd.add("diff", "sun,shade,-,ABS,-1,*", GraphDesc.AREA.toString(), "yellow", null, null, null, null, null, null, null);

		gd.setGraphTitle("Temperatures in May 2003");
		gd.setVerticalLabel("temperature");
		return gd;
	}
	
	static public Probe<?,?> getProbe() {
		Probe<?,?> p = new Probe<String, Number>() {

			@Override
			public Map<String, Number> getNewSampleValues() {
				return Collections.emptyMap();
			}

			@Override
			public String getSourceType() {
				return "fullmoke";
			}
		};
		p.setPd(getPd());
		return p;

	}
	static public Probe<?,?> create(TemporaryFolder testFolder) {
		RdsHost host = new RdsHost("Empty");
		host.setHostDir(testFolder.getRoot());
		
		Probe<?,?> p = getProbe();
		p.setHost(host);
		
		Assert.assertTrue("Fail creating probe", p.checkStore());
		
		return p;
	}

	static public long fill(Probe<?,?> p) throws IOException {
		long start = System.currentTimeMillis() / 1000;
		long end = start + 3600 * 24 * 30;

		String rrdPath = p.getRrdName();
		// update database
		GaugeSource sunSource = new GaugeSource(1200, 20);
		GaugeSource shadeSource = new GaugeSource(300, 10);
		long t = start;
		RrdDb rrdDb = new RrdDb(rrdPath);
		Sample sample = rrdDb.createSample();

		while (t <= end + 86400L) {
			sample.setTime(t);
			sample.setValue("sun", sunSource.getValue());
			sample.setValue("shade", shadeSource.getValue());
			sample.update();
			t += RANDOM.nextDouble() * STEP + 1;
		}

		rrdDb.close();
		
		return t;
	}
	
	static class GaugeSource {
		private double value;
		private double step;

		GaugeSource(double value, double step) {
			this.value = value;
			this.step = step;
		}

		long getValue() {
			double oldValue = value;
			double increment = RANDOM.nextDouble() * step;
			if (RANDOM.nextDouble() > 0.5) {
				increment *= -1;
			}
			value += increment;
			if (value <= 0) {
				value = 0;
			}
			return Math.round(oldValue);
		}
	}

}
