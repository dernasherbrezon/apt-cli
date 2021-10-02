package ru.r2cloud.apt.cli.model;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

	@Parameter(names = "--url", description = "Url of APT repository. Supported 2 type of URLS: \"s3://\" and \"file://\". For example, s3://r2cloud/sub-folder", required = true)
	private String url;

	@Parameter(names = "--aws-region", description = "AWS S3 region for the specified bucket.")
	private String awsRegion = "us-east-1";

	@Parameter(names = "--component", description = "Component name. Example: stable or unstable or main", required = true)
	private String component;

	@Parameter(names = "--codename", description = "Distributive codename. Example: stretch or bionic", required = true)
	private String codename;

	@Parameter(names = "--gpg-keyname", description = "Keyname to use for sign. Optional. If empty, then do not sign. Example: A5A70917")
	private String keyname;

	@Parameter(names = "--gpg-passphrase-file", description = "Filename to read gpg passphrase. Example: ~/.secrets.txt")
	private String passphraseFile;

	@Parameter(names = "--gpg-executable", description = "Gpg command to use for sign. Optional. If empty, then default 'gpg' will be used. Example: /opt/homebrew/bin/gpg")
	private String gpgExecutable;

	@Parameter(names = "--timeout", description = "Timeout while querying APT repository in milliseconds. Example: 10000")
	private int timeout = 10000;

	@Parameter(names = "--help", description = "This help", help = true)
	private boolean help;

	public String getAwsRegion() {
		return awsRegion;
	}

	public void setAwsRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getCodename() {
		return codename;
	}

	public void setCodename(String codename) {
		this.codename = codename;
	}

	public String getKeyname() {
		return keyname;
	}

	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}

	public String getPassphraseFile() {
		return passphraseFile;
	}

	public void setPassphraseFile(String passphraseFile) {
		this.passphraseFile = passphraseFile;
	}

	public String getGpgExecutable() {
		return gpgExecutable;
	}

	public void setGpgExecutable(String gpgExecutable) {
		this.gpgExecutable = gpgExecutable;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

}
