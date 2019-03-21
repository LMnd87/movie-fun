package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;
import org.superbiz.moviefun.blobstore.S3Store;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore s3Store) {
        this.albumsBean = albumsBean;
        this.blobStore = s3Store;
    }

    //private final BlobStore blobStore = new FileStore();



    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, getCoverFile(albumId), albumId);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        /*
        Path coverFilePath = getExistingCoverPath(albumId);
        byte[] imageBytes = readAllBytes(coverFilePath);
        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);
        */

        Optional<Blob> blobOptional = blobStore.get(format("covers/%d", albumId));
        Blob blob = blobOptional.get();
        byte[] imagebytes = IOUtils.toByteArray(blob.inputStream);


        return new HttpEntity<>(imagebytes, createImageHttpHeaders(getExistingCoverPath(albumId), imagebytes));
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile, long id) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        //targetFile.createNewFile();


        //blob
        String name = Long.toString(id);

        InputStream inputStream = uploadedFile.getInputStream();
        String contentType = uploadedFile.getContentType();
        Blob blob = new Blob(name, inputStream, contentType);

        blobStore.put(blob);
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);

        /*
        Optional<Blob> blob;
        try {
           blob = blobStore.get(coverFileName);
           InputStream in = blob.get().inputStream;

        } catch (Exception e) {

        }
        */
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            // THIS APPROACH WILL NOT WORK
            // coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());

            // THIS APPROACH SHOULD WORK
            URI defaultCoverURI = AlbumsController.class.getClassLoader().getResource("default-cover.jpg").toURI();
            coverFilePath = Paths.get(defaultCoverURI);
        }

        return coverFilePath;
    }
}