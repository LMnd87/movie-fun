package org.superbiz.moviefun.blobstore;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.tika.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.Optional;

import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {

        InputStream in = blob.inputStream;

        OutputStream out = new FileOutputStream("covers/"+blob.name);
        IOUtils.copy(in, out);

        out.flush();

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(name);

        //convert File to inputstream
        File initialFile = new File(name);
        InputStream targetStream = new FileInputStream(initialFile);

        //get contenttype from file
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String contentType = mimeTypesMap.getContentType(initialFile);

        Blob blob = new Blob(name,targetStream,contentType);


        //make blob optional
        Optional<Blob> blobOptional = Optional.ofNullable(blob);

        //if (!(blobOptional.isPresent())) System.out.println("empty");

        return blobOptional;
    }

    @Override
    public void deleteAll() {

        File directory = new File("covers");
        try {
            FileUtils.cleanDirectory(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }



}