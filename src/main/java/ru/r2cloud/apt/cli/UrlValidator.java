package ru.r2cloud.apt.cli;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class UrlValidator implements IParameterValidator {

	private static final Set<String> SUPPORTED_PROTOCOLS = new HashSet<>();

	static {
		SUPPORTED_PROTOCOLS.add("s3");
		SUPPORTED_PROTOCOLS.add("file");
	}

	@Override
	public void validate(String name, String value) throws ParameterException {
		URI url;
		try {
			url = new URI(value);
		} catch (URISyntaxException e) {
			throw new ParameterException("invalid url specified: " + value);
		}
		if (!SUPPORTED_PROTOCOLS.contains(url.getScheme())) {
			throw new ParameterException("unsupported protocol: " + url.getScheme());
		}
	}
}
