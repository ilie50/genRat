package org.genrat.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.genrat.core.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
    
	@Autowired
	private GeneratorService generatorService; 
	
	@RequestMapping(value = "/upload", method=RequestMethod.POST)
	public void downloadPDF(@RequestParam("name") String name, 
            @RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
 
		byte[] bytes = file.getBytes();
	    response.setContentType("application/pdf");
	    response.setHeader("Content-disposition", "inline; filename="+ name.trim() + ".pdf");
 
	    try {
	    	ByteArrayOutputStream outputStream = generatorService.createPdf(new ByteArrayInputStream(bytes));
	        OutputStream os = response.getOutputStream();
	        os.write(outputStream.toByteArray());
	        os.flush();
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    }
 
	}
	
    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }
    
    @RequestMapping(value="/upload2", method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("name") String name, 
            @RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File(name)));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

}
