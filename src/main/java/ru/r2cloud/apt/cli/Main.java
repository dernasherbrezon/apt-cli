package ru.r2cloud.apt.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ru.r2cloud.apt.AptRepository;
import ru.r2cloud.apt.AptRepositoryImpl;
import ru.r2cloud.apt.FileTransport;
import ru.r2cloud.apt.GpgSigner;
import ru.r2cloud.apt.GpgSignerBC;
import ru.r2cloud.apt.Transport;
import ru.r2cloud.apt.cli.model.CleanupCommand;
import ru.r2cloud.apt.cli.model.CommandLineArgs;
import ru.r2cloud.apt.cli.model.DeleteCommand;
import ru.r2cloud.apt.cli.model.InitCommand;
import ru.r2cloud.apt.cli.model.SaveCommand;
import ru.r2cloud.apt.cli.model.SignCommand;
import ru.r2cloud.apt.cli.model.ValidateCommand;
import ru.r2cloud.apt.model.Architecture;
import ru.r2cloud.apt.model.DebFile;
import ru.r2cloud.apt.model.SignConfiguration;
import ru.r2cloud.apt.model.ValidationError;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] argv) {
		CleanupCommand cleanup = new CleanupCommand();
		DeleteCommand delete = new DeleteCommand();
		SaveCommand save = new SaveCommand();
		InitCommand init = new InitCommand();
		ValidateCommand validate = new ValidateCommand();
		SignCommand sign = new SignCommand();
		CommandLineArgs args = new CommandLineArgs();
		JCommander parser = JCommander.newBuilder().addObject(args).addCommand("init", init).addCommand("save", save).addCommand("cleanup", cleanup).addCommand("delete", delete).addCommand("validate", validate).addCommand("sign", sign).build();
		try {
			parser.parse(argv);
		} catch (ParameterException e) {
			LOG.error(e.getMessage());
			parser.usage();
			System.exit(-1);
		}
		if (args.isHelp()) {
			parser.usage();
			return;
		}

		URI uri = null;
		try {
			uri = new URI(args.getUrl());
		} catch (URISyntaxException e) {
			LOG.error("invalid url specified: {}", args.getUrl(), e);
			System.exit(-1);
			return;
		}

		Transport transport = null;
		if (uri.getScheme().equalsIgnoreCase("s3")) {
			String path = uri.getPath();
			if (path.length() > 1 && (path.charAt(0) == '/')) {
				path = path.substring(1);
			}
			transport = new AwsTransport(uri.getHost(), args.getAwsRegion(), path, args.getTimeout(), null);
		} else if (uri.getScheme().equalsIgnoreCase("file")) {
			String basedirPath = uri.getAuthority() + uri.getPath();
			try {
				transport = new FileTransport(basedirPath);
			} catch (IOException e) {
				LOG.error("invalid file path specified: {}", basedirPath, e);
				System.exit(-1);
				return;
			}
		} else {
			// scheme should be validated by jcommander above
			System.exit(-1);
			return;
		}

		GpgSigner signer = null;
		if (args.getKeyname() != null) {
			SignConfiguration config = new SignConfiguration();
			config.setKeyname(args.getKeyname());
			config.setGpgCommand(args.getGpgExecutable());
			if (args.getPassphraseFile() != null) {
				try (InputStream is = new BufferedInputStream(new FileInputStream(args.getPassphraseFile()))) {
					config.setPassphrase(new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8).trim());
				} catch (IOException e) {
					LOG.error("unable to read passphrase file: {}", args.getPassphraseFile(), e);
					System.exit(-1);
					return;
				}
			}
			if (args.getGpgArguments() != null) {
				config.setGpgArguments(args.getGpgArguments());
			}
			config.setHashAlgorithm(args.getGpgHashAlgorithm());
			config.setSecretKeyFilename(args.getGpgKeyfile());
			try {
				signer = new GpgSignerBC(config);
			} catch (Exception e) {
				LOG.error("unable to initialize GPG signer", e);
				System.exit(-1);
				return;
			}
		}

		AptRepository aptMan = new AptRepositoryImpl(args.getCodename(), args.getComponent(), signer, transport);
		try {
			if (parser.getParsedCommand().equals("save")) {
				List<File> matchedFiles = Util.match(save.getPatterns(), new File("."));
				List<DebFile> files = new ArrayList<>();
				for (File cur : matchedFiles) {
					try {
						files.add(new DebFile(cur));
					} catch (ArchiveException e) {
						LOG.info("skipping: {} not a deb file", cur.getAbsolutePath(), e);
					}
				}
				if (!matchedFiles.isEmpty() && files.isEmpty()) {
					// most likely invalid/unsupported format of .deb files
					// fail in that case
					LOG.error("can't process any deb file");
					System.exit(-1);
				}
				aptMan.saveFiles(files);
			} else if (parser.getParsedCommand().equals("init")) {
				aptMan.init(convertArchs(init.getArchs()));
			} else if (parser.getParsedCommand().equals("cleanup")) {
				aptMan.cleanup(cleanup.getKeepLast());
			} else if (parser.getParsedCommand().equals("delete")) {
				if (delete.getPackages() != null && !delete.getPackages().isEmpty()) {
					aptMan.deletePackages(new HashSet<>(delete.getPackages()));
				}
				if (delete.getArchs() != null && !delete.getArchs().isEmpty()) {
					aptMan.deleteArchitectures(convertArchs(delete.getArchs()));
				}
			} else if (parser.getParsedCommand().equals("validate")) {
				List<ValidationError> errors = aptMan.validate();
				if (errors.isEmpty()) {
					LOG.info("Repository is valid");
				} else {
					for (ValidationError cur : errors) {
						LOG.info("{}: {}", cur.getCode().toString(), cur.getMessage());
					}
					LOG.info("Repository is NOT valid");
				}
			} else if (parser.getParsedCommand().equals("sign")) {
				aptMan.sign();
			} else {
				LOG.error("unknown command: {}", parser.getParsedCommand());
				parser.usage();
				System.exit(-1);
			}
		} catch (IOException e) {
			LOG.error("unable to run command: {}", parser.getParsedCommand(), e);
			System.exit(-1);
		}

	}

	private static Architecture[] convertArchs(List<String> init) {
		Architecture[] architectures = new Architecture[init.size()];
		for (int i = 0; i < init.size(); i++) {
			architectures[i] = Architecture.valueOf(init.get(i).toUpperCase(Locale.UK));
		}
		return architectures;
	}

}
