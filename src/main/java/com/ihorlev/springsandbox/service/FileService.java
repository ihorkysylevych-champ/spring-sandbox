package com.ihorlev.springsandbox.service;

import com.ihorlev.springsandbox.dto.FileDto;
import com.ihorlev.springsandbox.dto.InMemoryMultipartFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ifok.image.image4j.codec.bmp.BMPDecoder;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.bmp.BmpImageParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@Service
@AllArgsConstructor
public class FileService {

	private final ResourceLoader resourceLoader;

	private static final Map<String, String> IMAGES = new HashMap<>();

	static {
		IMAGES.put("jpg", "sample_640×426.jpg");
		IMAGES.put("jpeg", "sample_640×426.jpeg");
		IMAGES.put("jpe", "sample_640×426.jpe");
		IMAGES.put("png", "sample_640×426.png");
		IMAGES.put("bmp", "sample_640×426.bmp");
		IMAGES.put("gif", "sample_640×426.gif");
		IMAGES.put("tiff", "sample_640×426.tiff");
		IMAGES.put("tif", "airfield512x512.tif");
		IMAGES.put("jfif", "sample1.jfif");

		// Not supported by pdfbox
		IMAGES.put("dib", "sample_640×426.dib");
		IMAGES.put("heic", "sample1.heic");
	}

	public FileDto getPdf(String ext) throws IOException {
		Resource resource = resourceLoader.getResource("classpath:static/images/" + IMAGES.get(ext));
		File file = resource.getFile();

		PDDocument doc = new PDDocument();
		PDImageXObject pdImage = PDImageXObject.createFromFileByContent(file, doc);

		PDPage page = new PDPage(new PDRectangle(pdImage.getWidth(), pdImage.getHeight()));
		doc.addPage(page);

		// draw the image at full size at (x=20, y=20)
		try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
			// draw the image at full size at (x=20, y=20)
			contents.drawImage(pdImage, 0, 0);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.save(out);
		doc.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		return new FileDto(out.size(), in);
	}

	public MultipartFile covertToPdf(MultipartFile file) {
		try (PDDocument doc = new PDDocument()) {
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, file.getBytes(), file.getOriginalFilename());
			PDPage page = new PDPage(new PDRectangle(pdImage.getWidth(), pdImage.getHeight()));
			doc.addPage(page);

			PDPageContentStream contents = new PDPageContentStream(doc, page);
			contents.drawImage(pdImage, 0, 0);
			contents.close();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.save(out);

			return new InMemoryMultipartFile(file.getName(), file.getOriginalFilename() + ".pdf", APPLICATION_PDF_VALUE, out.toByteArray());

		} catch (IOException e) {
			log.error("Failed to convert file to pdf: {}", file.getOriginalFilename(), e);
		}

		return file;
	}

	public FileDto convert(String from, String to) throws IOException, ImageReadException, ImageWriteException {
		Resource resource = resourceLoader.getResource("classpath:static/images/" + IMAGES.get(from));
		File file = resource.getFile();
		// final ImageFormat imageFormat = Imaging.guessFormat(file);

//		var image = Imaging.getBufferedImage(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		Imaging.writeImage(image, out, ImageFormats.PNG);
		var r = ImageIO.read(file);

		InputStream is = new FileInputStream(file);
		// var d = new BMPDecoder(is);

		ImageInputStream input = ImageIO.createImageInputStream(file);

		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("BMP");
		while (readers.hasNext()) {
			ImageReader reader = readers.next();
			System.out.println("reader: " + reader);

			reader.setInput(input);
			ImageReadParam param = reader.getDefaultReadParam();
			BufferedImage image = reader.read(0, param);
			r = image != null ? image : r;
		}

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		return new FileDto(out.size(), in);
	}
}
