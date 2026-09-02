package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.config.SupabaseConfig;
import com.cloudBasedStorageService.StorageContainer.model.File;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;

    public SupabaseStorageService(
            SupabaseConfig supabaseConfig
    ) {
        this.supabaseConfig = supabaseConfig;
        this.restTemplate = new RestTemplate();
    }

    public String uploadFile(
            MultipartFile file,
            Integer userId
    ) throws Exception {



        String originalFileName =
                file.getOriginalFilename();


        String extension = "";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension =
                    originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );
        }


        String storedFileName =
                UUID.randomUUID() + extension;



        String filePath =
                userId + "/" + storedFileName;


        String url =
                supabaseConfig.getSupabaseUrl()
                        + "/storage/v1/object/"
                        + supabaseConfig.getBucket()
                        + "/"
                        + filePath;



        HttpHeaders headers = new HttpHeaders();

        headers.set("apikey",supabaseConfig.getServiceKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());

        String contentType =
                file.getContentType();

        if (contentType != null) {

            headers.setContentType(
                    MediaType.parseMediaType(
                            contentType
                    )
            );

        } else {

            headers.setContentType(
                    MediaType.APPLICATION_OCTET_STREAM
            );
        }


        HttpEntity<byte[]> request =
                new HttpEntity<>(
                        file.getBytes(),
                        headers
                );



        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        String.class
                );


        if (response.getStatusCode()
                .is2xxSuccessful()) {


            return filePath;
        }

        throw new RuntimeException(
                "Supabase upload failed: "
                        + response.getBody()
        );
    }


    public byte[] downloadFile(String filePath) {
        String url =
                supabaseConfig.getSupabaseUrl()
                        + "/storage/v1/object/"
                        + supabaseConfig.getBucket()
                        + "/"
                        + filePath;

        HttpHeaders headers = new HttpHeaders();

        headers.set("apikey",supabaseConfig.getServiceKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());

        HttpEntity<Void> request =
                new HttpEntity<>(
                        headers
                );

        ResponseEntity<byte[]> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        byte[].class
                );

        if (response.getStatusCode()
                .is2xxSuccessful()) {


            return response.getBody();
        }

        throw new RuntimeException(
                "Supabase download failed: "
                        + response.getBody()
        );



    }

    public String deleteFile(File file) {
        String url =
                supabaseConfig.getSupabaseUrl()
                        + "/storage/v1/object/"
                        + supabaseConfig.getBucket()
                        + "/"
                        + file.getFilePath();

        HttpHeaders headers = new HttpHeaders();

        headers.set("apikey",supabaseConfig.getServiceKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());

        HttpEntity<Void> request =
                new HttpEntity<>(
                        headers
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.DELETE,
                        request,
                        String.class
                );

        if (response.getStatusCode()
                .is2xxSuccessful()) {


            return response.getBody();
        }

        throw new RuntimeException(
                "Supabase delete failed: "
                        + response.getBody()
        );



    }
}

