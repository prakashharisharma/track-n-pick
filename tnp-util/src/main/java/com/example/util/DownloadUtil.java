package com.example.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class DownloadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtil.class);
	
	public void downloadFile(String urlStr, String fileName) throws IOException {
		
		LOGGER.info("URL :" + urlStr);
		
		LOGGER.info("FILENAME :" + fileName);
		
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
	
}
