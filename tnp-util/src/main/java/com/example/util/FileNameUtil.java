package com.example.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
@Deprecated
public class FileNameUtil {

	// https://www.nseindia.com/content/historical/EQUITIES/2018/OCT/cm01OCT2018bhav.csv.zip
	private static String base_nse_bhav_url = "https://www.nseindia.com/content/historical/EQUITIES/";// 2018/OCT/cm01OCT2018bhav.csv.zip";

	// ./src/data/inbox/zip/cm19SEP2018bhav.zip
	private static String base_nse_bhav_downloaded_filename = "./data/bhav/nse/zip/";
	
	private static String base_nse_master500_downloaded_filename = "./data/master/stocks/csv/nifty500Stockslist.csv";
	
	public static String getNSEBhavFileName() {
		String file_name = null;

		LocalDate localDate = LocalDate.now();

		DayOfWeek dayOfWeek = localDate.getDayOfWeek();

		int date;

		if (DayOfWeek.MONDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 3;
		} else if (DayOfWeek.SUNDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 2;
		} else {
			date = localDate.getDayOfMonth() - 1;
		}

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		if (date <= 0) {

			localDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

			if (localDate.getDayOfWeek().getValue() > 5) {
				localDate = localDate.minusDays(localDate.getDayOfWeek().getValue() - 5);
			}

			date = localDate.getDayOfMonth();

			month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

			year = localDate.getYear();
		}

		String dateStr = null;
		
		if (date < 10) {

			dateStr = "0"+date + month + year;
		} else {
			dateStr = date + month + year;
		}

		System.out.println(dateStr);

		file_name = base_nse_bhav_downloaded_filename + "cm" + dateStr + "bhav.zip";

		System.out.println(file_name);

		return file_name;
	}

	public static String getNSEBhavDownloadURI() {

		String complete_uri = null;

		LocalDate localDate = LocalDate.now();

		DayOfWeek dayOfWeek = localDate.getDayOfWeek();

		int date;

		if (DayOfWeek.MONDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 3;
		} else if (DayOfWeek.SUNDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 2;
		} else {
			date = localDate.getDayOfMonth() - 1;
		}

		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		if (date <= 0) {

			localDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

			if (localDate.getDayOfWeek().getValue() > 5) {
				localDate = localDate.minusDays(localDate.getDayOfWeek().getValue() - 5);
			}

			date = localDate.getDayOfMonth();

			month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

			year = localDate.getYear();
		}

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
}
