package com.ihorlev.springsandbox.dto;

import lombok.Data;

import java.io.InputStream;

@Data
public class FileDto {

	private final int size;
	private final InputStream in;

}
