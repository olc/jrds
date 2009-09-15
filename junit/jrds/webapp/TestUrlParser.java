package jrds.webapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jrds.HostsList;
import jrds.Period;
import jrds.PropertiesManager;
import jrds.Tools;
import jrds.mockobjects.GetMoke;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUrlParser {
	static final Logger logger = Logger.getLogger(TestUrlParser.class);
	static private final Map<String, String[]> parameters = new HashMap<String, String[]>();
	static private final HttpServletRequest req = GetMoke.getRequest(parameters);
	static final private DateFormat fullISOFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static final private HostsList hl = new HostsList(new PropertiesManager());

	@BeforeClass
	static public void configure() throws IOException {
		Tools.configure();
		logger.setLevel(Level.ERROR);
		Tools.setLevel(new String[] {ParamsBean.class.getName() }, logger.getLevel());
	}

	@Test public void checkId() {
		parameters.clear();
		parameters.put("id", new String[] { "1" });
		ParamsBean pb = new ParamsBean(req, hl);
		Assert.assertEquals(1, pb.getId());
	}
	@Test public void checkSortedTrue() {
		parameters.clear();
		parameters.put("sort", new String[] { "1" });
		ParamsBean pb = new ParamsBean(req, hl);
		Assert.assertTrue(pb.isSorted());
	}
	@Test public void checkSortedFalseDefault() {
		parameters.clear();
		ParamsBean pb = new ParamsBean(req, hl);
		Assert.assertTrue(! pb.isSorted());
	}
	@Test public void checkSortedFalse() {
		parameters.clear();
		parameters.put("sort", new String[] { "0" });
		ParamsBean pb = new ParamsBean(req, hl);
		Assert.assertTrue(! pb.isSorted());
	}
	@Test public void checkParseDate1() throws ParseException {
		parameters.clear();
		parameters.put("begin", new String[] { "2007-01-01" });
		parameters.put("end", new String[] { "2007-12-31" });
		ParamsBean pb = new ParamsBean(req, hl);
		Period p = pb.getPeriod();

		Date begin = fullISOFORMAT.parse("2007-01-01T00:00:00");
		Date end = fullISOFORMAT.parse("2007-12-31T23:59:59");
		Assert.assertEquals(p.getBegin(), begin);
		Assert.assertEquals(p.getEnd(), end);
		Assert.assertEquals(0, p.getScale());
		
		String url = pb.makeObjectUrl("root", "", false);
		Assert.assertTrue(url.contains("begin=" + begin.getTime()));
		Assert.assertTrue(url.contains("end=" + end.getTime()));
		
		logger.trace(url);

	}
	@Test public void checkParseDate2() throws ParseException {
		parameters.clear();
		ParamsBean pb = new ParamsBean(req, hl);
		Period p = pb.getPeriod();

		Date now = new Date();

		Assert.assertTrue(p.getEnd().compareTo(now) >= 0);
		Assert.assertEquals(7, p.getScale());
		String url = pb.makeObjectUrl("root", "", false);
		logger.trace(url);
	}

	@Test public void checkParseDate3() throws ParseException {
		parameters.clear();
		parameters.put("scale", new String [] { "4"} );
		ParamsBean pb = new ParamsBean(req, hl);
		Period p = pb.getPeriod();

		Date now = new Date();
		Calendar calBegin = Calendar.getInstance();
		calBegin.setTime(now);
		calBegin.add(Calendar.HOUR, -4);
		Date begin = calBegin.getTime();

		long delta = p.getBegin().getTime() - begin.getTime();
		Assert.assertTrue(p.getEnd().compareTo(now) >= 0);
		Assert.assertTrue(delta < 10 && delta > -10);
		Assert.assertEquals(4, p.getScale());
		String url = pb.makeObjectUrl("root", "", false);
		logger.trace(url);
	}

	@Test public void checkUrl1() {
		parameters.clear();
		parameters.put("host", new String [] { "host"} );
		parameters.put("scale", new String [] { "2"} );
		parameters.put("max", new String [] { "2"} );
		parameters.put("min", new String [] { "2"} );
		ParamsBean pb = new ParamsBean(req, null);
		String url = pb.makeObjectUrl("root", "", false);
		Assert.assertTrue(url.contains("host=host"));
		Assert.assertTrue(url.contains("/root?"));
		Assert.assertTrue(url.contains("id=" + "".hashCode()));
		Assert.assertTrue(url.contains("scale=2"));
		Assert.assertTrue(url.contains("max=2"));
		Assert.assertTrue(url.contains("min=2"));
		Assert.assertFalse(url.contains("begin="));
		Assert.assertFalse(url.contains("end="));
	}
	@Test public void checkUrl2() {
		parameters.clear();
		parameters.put("max", new String [] { String.valueOf(Double.NaN)} );
		parameters.put("min", new String [] { String.valueOf(Double.NaN)} );
		ParamsBean pb = new ParamsBean(req, hl);
		jrds.Filter f = new jrds.FilterHost("host");
		String url = pb.makeObjectUrl("root", f, true);
		logger.trace(url);
		Assert.assertTrue(url.contains("host=host"));
		Assert.assertTrue(url.contains("/root?"));
		Assert.assertFalse(url.contains("id=" + f.hashCode()));
		Assert.assertTrue(url.contains("begin="));
		Assert.assertTrue(url.contains("end="));
		Assert.assertFalse(url.contains("scale="));
		Assert.assertTrue(url.contains("max="));
		Assert.assertTrue(url.contains("min="));
	}
	@Test public void checkUrl3() throws UnsupportedEncodingException {
		parameters.clear();
		jrds.Filter f = jrds.Filter.ALLHOSTS;
		String filterName = f.getName();
		parameters.put("filter", new String [] { filterName } );

		ParamsBean pb = new ParamsBean(req, hl);
		String url = pb.makeObjectUrl("root", f, true);
		Assert.assertTrue(url.contains("filter=" + URLEncoder.encode(filterName, "UTF-8")));
		Assert.assertTrue(url.contains("/root?"));
		Assert.assertFalse(url.contains("id=" + f.hashCode()));
		Assert.assertTrue(url.contains("begin="));
		Assert.assertTrue(url.contains("end="));
		Assert.assertFalse(url.contains("scale="));
	}

}