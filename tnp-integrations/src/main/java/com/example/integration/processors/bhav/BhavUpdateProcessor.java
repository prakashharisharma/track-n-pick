package com.example.integration.processors.bhav;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.model.StockPriceIN;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.StockPriceIO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BhavUpdateProcessor implements Processor {

	//private static final Logger LOGGER = LoggerFactory.getLogger(BhavUpdateProcessor.class);

	private static Set<String> ignoreSet = new HashSet<>();

	static {
		ignoreSet.add("BFSI");
		ignoreSet.add("MOQUALITY");
		ignoreSet.add("QGOLDHALF");
		ignoreSet.add("TOP100CASE");
		ignoreSet.add("TATSILV");
		ignoreSet.add("LICNETFSEN");
		ignoreSet.add("HDFCLOWVOL");
		ignoreSet.add("NEXT50");
		ignoreSet.add("MOVALUE");
		ignoreSet.add("GOLDSHARE");
		ignoreSet.add("SETFNN50");
		ignoreSet.add("HDFCNIFBAN");
		ignoreSet.add("SETFNIFBK");
		ignoreSet.add("ALPHAETF");
		ignoreSet.add("11QPD");
		ignoreSet.add("BANKIETF");
		ignoreSet.add("SENSEXETF");
		ignoreSet.add("BANKETF");
		ignoreSet.add("NIFTY50ADD");
		ignoreSet.add("NV20IETF");
		ignoreSet.add("SMALLCAP");
		ignoreSet.add("SNXT50BEES");
		ignoreSet.add("AAYUSHBULL");
		ignoreSet.add("UTINIFTETF");
		ignoreSet.add("NIFTY1");
		ignoreSet.add("NIEHSPH");
		ignoreSet.add("NIEHSPJ");
		ignoreSet.add("NIESSPK");
		ignoreSet.add("NIESSPL");
		ignoreSet.add("MIDCAPETF");
		ignoreSet.add("NIESSPM");
		ignoreSet.add("EQUAL50ADD");
		ignoreSet.add("HDFCBSE500");
		ignoreSet.add("NIFTYETF");
		ignoreSet.add("PSUBNKIETF");
		ignoreSet.add("SENSEXADD");
		ignoreSet.add("SILVRETF");
		ignoreSet.add("LIQUIDADD");
		ignoreSet.add("COMMOIETF");
		ignoreSet.add("PVTBANKADD");
		ignoreSet.add("ABSLLIQUID");
		ignoreSet.add("LIQUIDBETF");
		ignoreSet.add("SHARIABEES");
		ignoreSet.add("LOWVOL1");
		ignoreSet.add("BSLGOLDETF");
		ignoreSet.add("NIF5GETF");
		ignoreSet.add("HDFCQUAL");
		ignoreSet.add("NIFTYBEES");
		ignoreSet.add("CONSUMIETF");
		ignoreSet.add("LICNETFN50");
		ignoreSet.add("LOWVOLIETF");
		ignoreSet.add("INFRAIETF");
		ignoreSet.add("SENSEX1");
		ignoreSet.add("11AGG");
		ignoreSet.add("MOM100");
		ignoreSet.add("HDFCNIFTY");


	}

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		log.info("Starting Bhav Processor");
		
		@SuppressWarnings("unchecked")
		List<StockPriceIN> dailyStockPriceList = (List<StockPriceIN>) exchange.getIn().getBody();

		//dailyStockPriceList = dailyStockPriceList.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ")).collect(Collectors.toList());

		Set<StockPriceIN> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);

		dailyStockPriceSet.forEach( sp -> {
			log.info("Processing Bhav  {} {} {} {} {}", sp.getSource(), sp.getIsin(), sp.getNseSymbol(), sp.getSeries(), sp.getTimestamp());

			if(this.isIgnored(sp.getNseSymbol()) || sp.getNseSymbol().contains("EPF") || sp.getNseSymbol().contains("AMC")) {
				log.info("Ignored Symbol {}", sp.getNseSymbol());
			}else if(sp.getSource().equalsIgnoreCase("NSE")){
				this.processNseBhav(sp);
			} else if (sp.getSource().equalsIgnoreCase("BSE")) {
				this.processBseBhav(sp);
			}else{
				log.info("Invalid Source");
			}

			//StockPriceIO stockPriceIO = new StockPriceIO(sp.getNseSymbol(), sp.getSeries(), sp.getOpen(), sp.getHigh(), sp.getLow(), sp.getClose(), sp.getLast(), sp.getPrevClose(), sp.getTottrdqty(), sp.getTottrdval(), sp.getTimestamp().toString(), sp.getTotaltrades(), sp.getIsin());

			/* Store Bhav for analytics purpose, Not needed for now
			queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE);
			*/

			//queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE);
			//queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);

		});
		
		log.info("Completed Bhav Processor");
		
	}

	private void sendToUpdateQueue(StockPriceIO stockPriceIO){
		log.info("Queuing to update price Stock: {}", stockPriceIO.getNseSymbol());
		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);
	}

	private void processNseBhav(StockPriceIN stockPriceIN){

		if(stockPriceIN.getSeries().equalsIgnoreCase("EQ")) {
			StockPriceIO stockPriceIO = new StockPriceIO(stockPriceIN.getSource().toUpperCase(), stockPriceIN.getCompanyName(),stockPriceIN.getNseSymbol(), stockPriceIN.getSeries(), stockPriceIN.getOpen(), stockPriceIN.getHigh(), stockPriceIN.getLow(), stockPriceIN.getClose(), stockPriceIN.getLast(), stockPriceIN.getPrevClose(), stockPriceIN.getTottrdqty(), stockPriceIN.getTottrdval(), stockPriceIN.getTimestamp().toString(), stockPriceIN.getTotaltrades(), stockPriceIN.getIsin());

			this.sendToUpdateQueue(stockPriceIO);
		}else{
			log.info("Ignored : {} on {}", stockPriceIN.getNseSymbol(), stockPriceIN.getSource());
		}
	}

	private void processBseBhav(StockPriceIN stockPriceIN){
		log.info("Processing BSE Bhav");
		if(stockPriceIN.getSeries().equalsIgnoreCase("A") || stockPriceIN.getSeries().equalsIgnoreCase("B") || stockPriceIN.getSeries().equalsIgnoreCase("M")) {
			StockPriceIO stockPriceIO = new StockPriceIO(stockPriceIN.getSource().toUpperCase(), stockPriceIN.getCompanyName(), stockPriceIN.getNseSymbol(), stockPriceIN.getSeries(), stockPriceIN.getOpen(), stockPriceIN.getHigh(), stockPriceIN.getLow(), stockPriceIN.getClose(), stockPriceIN.getLast(), stockPriceIN.getPrevClose(), stockPriceIN.getTottrdqty(), stockPriceIN.getTottrdval(), stockPriceIN.getTimestamp().toString(), stockPriceIN.getTotaltrades(), stockPriceIN.getIsin());
			stockPriceIO.setBseCode(stockPriceIN.getExchangeCode());
			this.sendToUpdateQueue(stockPriceIO);
		}else{
			log.info("Ignored : {} on {}", stockPriceIN.getNseSymbol(), stockPriceIN.getSource());
		}
	}

	private boolean isIgnored(String nseSymbol){
		return ignoreSet.contains(nseSymbol)? Boolean.TRUE : Boolean.FALSE;
	}
}
