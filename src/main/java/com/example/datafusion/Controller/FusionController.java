package com.example.datafusion.Controller;

import com.example.datafusion.Service.FusionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
public class FusionController {

    @Autowired
    private FusionService fusionService;

    /*
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void uploadData(@RequestBody Map<String, Object> payload) {

        JSONObject jsonObject = new JSONObject(payload);
        String uuid = UUID.randomUUID().toString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            FileWriter fileOutputStream = new FileWriter("C:\\Users\\43676\\Documents\\GitHub\\dataFusion\\out\\airport-"
                    + uuid + ".txt");
            System.out.println("printx");
            fileOutputStream.write(jsonObject.toString(4));
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     */

    @RequestMapping(value = "/workflow2", method = RequestMethod.POST)
    public ResponseEntity<?> workflow2(@RequestParam(value = "file1") MultipartFile file1, @RequestParam(value = "file2") MultipartFile file2) {

        File file = null;
        File fileSecond = null;
        System.out.println(file1.getName());
        System.out.println(file2.getName());
        if(!file1.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file1.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file1.getName() + ".ttl");
            file = new File(path.toString());
            if (!file.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!file2.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file2.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file2.getName() + ".ttl");
            fileSecond = new File(path.toString());
            if (!fileSecond.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ResponseEntity.ok(fusionService.workflow2(file, fileSecond));
    }

    @RequestMapping(value = "/workflow1", method = RequestMethod.POST)
    public ResponseEntity<?> workflow1(@RequestParam(value = "file1") MultipartFile file1, @RequestParam(value = "file2") MultipartFile file2) {
        System.out.println("Workflow1");
        File file = null;
        File fileSecond = null;
        if(!file1.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file1.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file1.getName() + ".ttl");
            file = new File(path.toString());
            if (!file.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!file2.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file2.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file2.getName() + ".ttl");
            fileSecond = new File(path.toString());
            if (!fileSecond.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ResponseEntity.ok(fusionService.workflow1(file, fileSecond));
    }

    @RequestMapping(value = "/workflow3", method = RequestMethod.POST)
    public ResponseEntity<?> workflow3(@RequestParam(value = "file1") MultipartFile file1, @RequestParam(value = "file2") MultipartFile file2) {
        System.out.println("Workflow3");
        File file = null;
        File fileSecond = null;
        if(!file1.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file1.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file1.getName() + ".ttl");
            file = new File(path.toString());
            if (!file.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!file2.isEmpty()) {
            byte[] bytes = new byte[0];
            try {
                bytes = file2.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get("src/main/resources/templates/" + file2.getName() + ".ttl");
            fileSecond = new File(path.toString());
            if (!fileSecond.exists()) {
                try {
                    Files.write(path, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ResponseEntity.ok(fusionService.workflow3(file, fileSecond));
    }
}
