package ru.r2cloud.apt.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class UtilTest {

	@Test
	public void testOneDirUpMatchFiles() throws Exception {
		List<String> patterns = new ArrayList<>();
		patterns.add("../rtl-sdr*deb");
		patterns.add("../librtlsdr-12.deb");
		List<File> result = Util.match(patterns, new File("./src/test/resources/rtl-sdr"));
		assertTrue(contains(result, "src/test/resources/rtl-sdr-12.deb"));
		assertTrue(contains(result, "src/test/resources/librtlsdr-12.deb"));
	}

	@Test
	public void testSameDirMatches() throws Exception {
		List<String> patterns = new ArrayList<>();
		patterns.add("./rtl-sdr*deb");
		patterns.add("./librtlsdr-12.deb");
		List<File> result = Util.match(patterns, new File("./src/test/resources/"));
		assertTrue(contains(result, "src/test/resources/rtl-sdr-12.deb"));
		assertTrue(contains(result, "src/test/resources/librtlsdr-12.deb"));
	}

	@Test
	public void testNoDirInThePattern() throws Exception {
		List<String> patterns = new ArrayList<>();
		patterns.add("rtl-sdr*deb");
		patterns.add("librtlsdr-12.deb");
		List<File> result = Util.match(patterns, new File("./src/test/resources/"));
		assertTrue(contains(result, "src/test/resources/rtl-sdr-12.deb"));
		assertTrue(contains(result, "src/test/resources/librtlsdr-12.deb"));
	}

	@Test
	public void testNoDirInThePattern2() throws Exception {
		List<String> patterns = new ArrayList<>();
		patterns.add("pom.xml");
		List<File> result = Util.match(patterns, new File("."));
		assertTrue(contains(result, "pom.xml"));
	}

	private static boolean contains(List<File> result, String suffix) throws Exception {
		for (File cur : result) {
			String canonized = cur.getCanonicalPath();
			if (canonized.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

}
