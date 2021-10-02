package ru.r2cloud.apt.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ru.r2cloud.apt.IOCallback;
import ru.r2cloud.apt.ResourceDoesNotExistException;
import ru.r2cloud.apt.Transport;
import ru.r2cloud.apt.model.RemoteFile;

public class AwsTransport implements Transport {

	private final String bucket;
	private final String basepath;
	private final AmazonS3 client;

	public AwsTransport(String bucket, String awsRegion, String basepath, int timeout, BasicAWSCredentials creds) {
		this.bucket = bucket;
		if (basepath != null && !basepath.equals("")) {
			if (!basepath.endsWith("/")) {
				this.basepath = basepath + "/";
			} else {
				this.basepath = basepath;
			}
		} else {
			this.basepath = "";
		}
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		if (creds != null) {
			builder = builder.withCredentials(new AWSStaticCredentialsProvider(creds));
		}
		ClientConfiguration config = new ClientConfiguration();
		config.setConnectionTimeout(timeout);
		builder = builder.withClientConfiguration(config);
		builder = builder.withRegion(awsRegion);
		client = builder.build();
	}

	@Override
	public void save(String path, File file) throws IOException {
		client.putObject(bucket, basepath + path, file);
	}

	@Override
	public void save(String path, IOCallback callback) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		callback.save(baos);
		baos.close();
		saveObject(path, baos);
	}

	@Override
	public void saveGzipped(String path, IOCallback callback) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream os = new GZIPOutputStream(baos)) {
			callback.save(os);
		}
		saveObject(path, baos);
	}

	private void saveObject(String path, ByteArrayOutputStream baos) {
		byte[] object = baos.toByteArray();
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(object.length);
		meta.setLastModified(new Date());
		client.putObject(bucket, basepath + path, new ByteArrayInputStream(object), meta);
	}

	@SuppressWarnings("resource")
	@Override
	public void load(String path, IOCallback callback) throws IOException, ResourceDoesNotExistException {
		try (InputStream is = client.getObject(bucket, basepath + path).getObjectContent()) {
			callback.load(is);
		} catch (AmazonServiceException e) {
			if (e.getStatusCode() == 404) {
				throw new ResourceDoesNotExistException();
			} else {
				throw e;
			}
		}
	}

	@SuppressWarnings("resource")
	@Override
	public void loadGzipped(String path, IOCallback callback) throws IOException, ResourceDoesNotExistException {
		try (InputStream is = new GZIPInputStream(client.getObject(bucket, basepath + path).getObjectContent())) {
			callback.load(is);
		} catch (AmazonServiceException e) {
			if (e.getStatusCode() == 404) {
				throw new ResourceDoesNotExistException();
			} else {
				throw e;
			}
		}
	}

	@Override
	public List<RemoteFile> listFiles(String path) {
		List<RemoteFile> result = new ArrayList<>();
		ObjectListing previous = null;
		while (true) {
			ObjectListing listing;
			if (previous != null) {
				listing = client.listNextBatchOfObjects(previous);
			} else {
				listing = client.listObjects(bucket, basepath + path);
			}

			for (S3ObjectSummary cur : listing.getObjectSummaries()) {
				RemoteFile curFile = new RemoteFile();
				curFile.setPath(cur.getKey());
				curFile.setLastModifiedTime(cur.getLastModified().getTime());
				curFile.setDirectory(cur.getKey().endsWith("/"));
				result.add(curFile);
			}

			if (!listing.isTruncated()) {
				break;
			}

			previous = listing;
		}
		return result;
	}

	@Override
	public void delete(String path) throws IOException {
		client.deleteObject(bucket, basepath + path);
	}

}
