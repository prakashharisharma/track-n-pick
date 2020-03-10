package com.example.service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.util.FileLocationConstants;

@Service
public class FileNameService {

	@Autowired
	private CalendarService calendarService;
	
	// https://www.nseindia.com/content/historical/EQUITIES/2018/OCT/cm01OCT2018bhav.csv.zip
	private static String base_nse_bhav_url = "https://www1.nseindia.com/content/historical/EQUITIES/";// 2018/OCT/cm01OCT2018bhav.csv.zip";
	
	private static String base_nse_ReferrerURI = "https://www1.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=";

	// ./src/data/inbox/zip/cm19SEP2018bhav.zip
	private static String base_nse_bhav_downloaded_filename = FileLocationConstants.BHAV_ZIP_LOCATION+ File.separator;
	
	private static String base_nse_master500_downloaded_filename = FileLocationConstants.NIFTY_500_DOWNLOAD_LOCATION+File.separator+"nifty500Stockslist.csv";
	
	private static String base_nse_nifty50_downloaded_filename = FileLocationConstants.NIFTY_50_DOWNLOAD_LOCATION+File.separator+"nifty50Stockslist.csv";
	
	private static String base_nse_nifty100_downloaded_filename = FileLocationConstants.NIFTY_100_DOWNLOAD_LOCATION+File.separator+"nifty100Stockslist.csv";
	
	private static String base_nse_nifty250_downloaded_filename = FileLocationConstants.NIFTY_250_DOWNLOAD_LOCATION+File.separator+"nifty250Stockslist.csv";

	
	public String getNSEBhavFileName() {
		
		String file_name = null;

		LocalDate localDate = calendarService.previousWorkingDay();

		int date = localDate.getDayOfMonth();

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		String dateStr = null;
		
		if (date < 10) {

			dateStr = "0"+date + month + year;
		} else {
			dateStr = date + month + year;
		}

		file_name = base_nse_bhav_downloaded_filename + "cm" + dateStr + "bhav.zip";

		return file_name;
	}

	public String getNSEBhavFileName(LocalDate downloadDate) {
		
		String file_name = null;

		LocalDate localDate = downloadDate;

		int date = localDate.getDayOfMonth();

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		String dateStr = null;
		
		if (date < 10) {

			dateStr = "0"+date + month + year;
		} else {
			dateStr = date + month + year;
		}

		file_name = base_nse_bhav_downloaded_filename + "cm" + dateStr + "bhav.zip";

		return file_name;
	}
	
	public String getNSEBhavReferrerURI(LocalDate downloadDate) {
		String urlpre= base_nse_ReferrerURI;
		String urlpost = "&section=EQ";
		
		LocalDate date = downloadDate;
	    
    	DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    	
    	String dateText = date.format(formatters);
    
		return urlpre + dateText + urlpost;
	}
	
	
	public String getNSEBhavReferrerURI() {
		String urlpre= base_nse_ReferrerURI;
		String urlpost = "&section=EQ";
		
		LocalDate date = calendarService.previousWorkingDay();
	    
    	DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    	
    	String dateText = date.format(formatters);
    
		return urlpre + dateText + urlpost;
	}
	
	public String getNSEBhavDownloadURI(LocalDate downloadDate) {

		String complete_uri = null;

		LocalDate localDate = downloadDate;

		int date = localDate.getDayOfMonth();

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		String dateStr = null;
		
		if (date < 10) {

			dateStr = "0"+date + month + year;
		} else {
			dateStr = date + month + year;
		}

		complete_uri = base_nse_bhav_url + year + "/" + month + "/" + "cm" + dateStr + "bhav.csv.zip";

		return complete_uri;
	}
	
	public String getNSEBhavDownloadURI() {

		String complete_uri = null;

		LocalDate localDate = calendarService.previousWorkingDay();

		int date = localDate.getDayOfMonth();

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		String dateStr = null;
		
		if (date < 10) {

			dateStr = "0"+date + month + year;
		} else {
			dateStr = date + month + year;
		}

		complete_uri = base_nse_bhav_url + year + "/" + month + "/" + "cm" + dateStr + "bhav.csv.zip";

		return complete_uri;
	}

	public String getNSEIndex500StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_nifty500list.csv";
	}
	
	public String getNSEIndex500StocksFileName() {
		return base_nse_master500_downloaded_filename;
	}
	
	public String getNSENifty50StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_nifty50list.csv";
	}
	
	public String getNSENifty50StocksFileName() {
		return base_nse_nifty50_downloaded_filename;
	}
	
	public String getNSENifty100StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_nifty100list.csv";
	}
	
	public String getNSENifty100StocksFileName() {
		return base_nse_nifty100_downloaded_filename;
	}
	
	public String getNSENifty250StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_niftymidcap150list.csv";
	}
	
	public String getNSENifty250StocksFileName() {
		return base_nse_nifty250_downloaded_filename;
	}
}
