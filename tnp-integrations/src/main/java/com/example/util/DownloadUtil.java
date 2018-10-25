package com.example.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDate;

public class DownloadUtil {

	private static String base_url = "https://www.nseindia.com/content/historical/EQUITIES/2018/SEP/cm19SEP2018bhav.csv.zip";
	
	//https://www.nseindia.com/content/historical/EQUITIES/2018/SEP/cm19SEP2018bhav.csv.zip
	
    public static void main(String[] args) {
    	
        try {
        	
            downloadUsingNIO(base_url, "./src/main/resources/data/cm19SEP2018bhav.zip");
            
          //  downloadUsingStream(url, "./src/main/resources/data/cm12SEP2018bhav.zip");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDownloadUri() {
    	
    	LocalDate ld = LocalDate.now();
    	
    	
    	return base_url + "";
    }
    
    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static void downloadUsingStream(String urlStr, String file) throws IOException{
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1)
        {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }
    
}
