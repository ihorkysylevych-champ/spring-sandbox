package com.ihorlev.springsandbox.controller;

import com.ihorlev.springsandbox.service.FileService;
import lombok.AllArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AllArgsConstructor
@RestController
@RequestMapping("/file")
public class FileController {

	private final FileService fileService;

	@GetMapping("/pdf/{ext}")
	public ResponseEntity<InputStreamResource> getPdf(@PathVariable String ext) throws IOException {
		var dto = fileService.getPdf(ext);

		InputStreamResource inputStreamResource = new InputStreamResource(dto.getIn());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(dto.getSize());
		headers.setContentType(MediaType.APPLICATION_PDF);

		return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
	}

	@PostMapping("/convert/pdf")
	public ResponseEntity<InputStreamResource> uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
		var pdf = fileService.covertToPdf(file);

		InputStreamResource inputStreamResource = new InputStreamResource(pdf.getInputStream());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(pdf.getSize());
		headers.setContentType(MediaType.APPLICATION_PDF);

		return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
	}

	@GetMapping("/convert/{from}/{to}")
	public ResponseEntity<InputStreamResource> convertFile(@PathVariable String from, @PathVariable String to) throws IOException, ImageWriteException, ImageReadException {
		var dto = fileService.convert(from, to);

		InputStreamResource inputStreamResource = new InputStreamResource(dto.getIn());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(dto.getSize());
		headers.setContentType(MediaType.IMAGE_PNG);

		return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
	}
}
