package org.genrat.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.genrat.converter.XmlParser;
import org.genrat.word.DocxToPdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class FileUploadController {

	@Autowired
	private DocxToPdfGeneratorService generatorService;

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public void downloadPDF(@RequestParam("name") String name, @RequestParam("data") String data,
			@RequestParam("dataType") String dataType,
			@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
		byte[] bytes = file.getBytes();
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "inline; filename=" + name.trim() + ".pdf");
		Serializable dataMap = null;
		if ("xml".equalsIgnoreCase(dataType)) {
			dataMap = convertXMLDataToMap(data);
		} else if ("json".equalsIgnoreCase(dataType)) {
			dataMap = convertJSONDataToMap(data);
		} else {
			throw new RuntimeException("Unknown type!");
		}
		try {
			ByteArrayOutputStream outputStream = generatorService.createPdf(new ByteArrayInputStream(bytes), dataMap);
			OutputStream os = response.getOutputStream();
			os.write(outputStream.toByteArray());
			os.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private Serializable convertJSONDataToMap(String data) {
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> map = new HashMap<String, Object>();

		try {
			map = mapper.readValue(data, new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return (Serializable) map;
	}

	private Serializable convertXMLDataToMap(String data) {
		XmlParser xmlParser = new XmlParser("<root>" + data + "</root>");
		return (Serializable) xmlParser.parseXML().get("root");
	}

	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public @ResponseBody String provideUploadInfo() {
		return "You can upload a file by posting to this same URL.";
	}

	@RequestMapping(value = "/upload2", method = RequestMethod.POST)
	public @ResponseBody String handleFileUpload(@RequestParam("name") String name,
			@RequestParam("file") MultipartFile file) {
		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(name)));
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
