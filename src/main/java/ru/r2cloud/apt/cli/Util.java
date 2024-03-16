package ru.r2cloud.apt.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Util {

	private static final Logger LOG = LoggerFactory.getLogger(Util.class);

	static List<File> match(List<String> patterns, File basedir) {
		List<File> result = new ArrayList<>(patterns.size());
		for (String curPattern : patterns) {
			int separator = curPattern.lastIndexOf(File.separator);
			File patternDir = basedir;
			if (separator != -1) {
				patternDir = new File(basedir, curPattern.substring(0, separator));
				curPattern = curPattern.substring(separator + 1);
			}
			try {
				patternDir = patternDir.getCanonicalFile();
			} catch (IOException e) {
				LOG.error("can't normalize: {}", curPattern, e);
				continue;
			}
			@SuppressWarnings("resource")
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**" + File.separator + curPattern);
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(patternDir.toPath(), matcher::matches)) {
				dirStream.forEach(path -> {
					result.add(path.toFile());
				});
			} catch (IOException e) {
				LOG.error("can't read: {}", curPattern, e);
			}
		}
		return result;
	}

	private Util() {
		// do nothing
	}

}
