package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.cloudsearchdomain.model.ContentType;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.tika.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Optional;

public class S3Store implements BlobStore{

    private AmazonS3Client amazonS3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.amazonS3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {

        if (!(amazonS3Client.doesBucketExist(photoStorageBucket))) amazonS3Client.createBucket(photoStorageBucket);

        String key = blob.name;

        InputStream in = blob.inputStream;
        ObjectMetadata objectMetadata = new ObjectMetadata();

        objectMetadata.setContentType(blob.contentType);
        objectMetadata.setContentLength(in.available());

        amazonS3Client.putObject(photoStorageBucket, key, in, objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        name = name.substring(7,name.length());
        System.out.println(name);
        S3Object s3Object = amazonS3Client.getObject(photoStorageBucket, name);
        String contentType = s3Object.getObjectMetadata().getContentType();
        InputStream inputStream = s3Object.getObjectContent();
        Blob blob = new Blob(name, inputStream, contentType);
        Optional<Blob> optionalBlob;
        optionalBlob = Optional.ofNullable(blob);
        return optionalBlob;
    }

    @Override
    public void deleteAll() throws IOException {
        

    }
}
