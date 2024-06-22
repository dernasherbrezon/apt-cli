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
import ru.r2cloud.apt.GpgSignerImpl;
import ru.r2cloud.apt.Transport;
import ru.r2cloud.apt.cli.model.CleanupCommand;
import ru.r2cloud.apt.cli.model.CommandLineArgs;
import ru.r2cloud.apt.cli.model.DeleteCommand;
import ru.r2cloud.apt.cli.model.InitCommand;
import ru.r2cloud.apt.cli.model.SaveCommand;
import ru.r2cloud.apt.model.Architecture;
import ru.r2cloud.apt.model.DebFile;
import ru.r2cloud.apt.model.SignConfiguration;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] argv) {
		CleanupCommand cleanup = new CleanupCommand();
		DeleteCommand delete = new DeleteCommand();
		SaveCommand save = new SaveCommand();
		InitCommand init = new InitCommand();
		CommandLineArgs args = new CommandLineArgs();
		JCommander parser = JCommander.newBuilder().addObject(args).addCommand("init", init).addCommand("save", save).addCommand("cleanup", cleanup).addCommand("delete", delete).build();
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
			if (config.getSecretKeyFilename() != null) {
				try {
					signer = new GpgSignerBC(config);
				} catch (Exception e) {
					LOG.error("unable to initialize GPG signer", e);
					System.exit(-1);
					return;
				}
			} else {
				signer = new GpgSignerImpl(config);
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
				Architecture[] architectures = new Architecture[init.getArchs().size()];
				for (int i = 0; i < init.getArchs().size(); i++) {
					architectures[i] = Architecture.valueOf(init.getArchs().get(i).toUpperCase(Locale.UK));
				}
				aptMan.init(architectures);
			} else if (parser.getParsedCommand().equals("cleanup")) {
				aptMan.cleanup(cleanup.getKeepLast());
			} else if (parser.getParsedCommand().equals("delete")) {
				aptMan.deletePackages(new HashSet<>(delete.getPackages()));
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

}
