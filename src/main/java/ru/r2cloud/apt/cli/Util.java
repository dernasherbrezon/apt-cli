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
			@SuppressWarnings("resource")
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**" + File.separator + curPattern);
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(new File(basedir.getAbsoluteFile(), "..").toPath(), matcher::matches)) {
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
