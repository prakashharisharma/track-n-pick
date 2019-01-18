package com.example.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileNameService {

	@Autowired
	private CalendarService calendarService;
	
	// https://www.nseindia.com/content/historical/EQUITIES/2018/OCT/cm01OCT2018bhav.csv.zip
	private static String base_nse_bhav_url = "https://www.nseindia.com/content/historical/EQUITIES/";// 2018/OCT/cm01OCT2018bhav.csv.zip";

	// ./src/data/inbox/zip/cm19SEP2018bhav.zip
	private static String base_nse_bhav_downloaded_filename = "./data/bhav/nse/zip/";
	
	private static String base_nse_master500_downloaded_filename = "./data/master/stocks/csv/nifty500Stockslist.csv";
	
	private static String base_nse_nifty50_downloaded_filename = "./data/nifty50/stocks/csv/nifty50Stockslist.csv";

	
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

	public String getNSEBhavReferrerURI() {
		String urlpre= "https://www.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=";
		String urlpost = "&section=EQ";
		
		LocalDate date = calendarService.previousWorkingDay();
	    
    	DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    	
    	String dateText = date.format(formatters);
    
		return urlpre + dateText + urlpost;
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

	public static String getNSEIndex500StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_nifty500list.csv";
	}
	
	public static String getNSEIndex500StocksFileName() {
		return base_nse_master500_downloaded_filename;
	}
	
	public static String getNSENifty50StocksURI() {
		return "https://www.nseindia.com/content/indices/ind_nifty50list.csv";
	}
	
	public static String getNSENifty50StocksFileName() {
		return base_nse_nifty50_downloaded_filename;
	}
}
