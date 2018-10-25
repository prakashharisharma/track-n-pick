package com.example;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class JavaDownloadFileFromURL {

    public static void main(String[] args) {
    	
        String url = "https://www.nseindia.com/content/historical/EQUITIES/2018/OCT/cm01OCT2018bhav.csv.zip";
        
        String nifty500URL = "https://www.nseindia.com/content/indices/ind_nifty500list.csv";
        
        try {
        	
            downloadUsingNIO(url, "./src/main/resources/data/cm19SEP2018bhav.zip");
            
            downloadUsingNIO(nifty500URL, "./src/main/resources/data/ind_nifty500list.csv");
            
          //  downloadUsingStream(url, "./src/main/resources/data/cm12SEP2018bhav.zip");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
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
