package com.example.integration.processors.bhav;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.dto.kafka.Message;
import com.example.mq.producer.EventProducerService;
import com.example.util.DownloadCounterUtil;
import com.example.util.io.model.UpdateTriggerIO;
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
public class UpdateBhavProcessor implements Processor {

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
		ignoreSet.add("PSUBANK");
		ignoreSet.add("TATAGOLD");
		ignoreSet.add("SDL24BEES");
		ignoreSet.add("CONSUMIETF");
		ignoreSet.add("LICNETFN50");
		ignoreSet.add("LOWVOLIETF");
		ignoreSet.add("INFRAIETF");
		ignoreSet.add("SENSEX1");
		ignoreSet.add("11AGG");
		ignoreSet.add("MOM100");
		ignoreSet.add("HDFCNIFTY");
		ignoreSet.add("UTIBANKETF");
		ignoreSet.add("ABSLPSE");
		ignoreSet.add("SOBHAPP");
		ignoreSet.add("11DPR");
		ignoreSet.add("HDFCMID150");
		ignoreSet.add("PSUBNKBEES");
		ignoreSet.add("HDFCMOMENT");
		ignoreSet.add("SDL26BEES");
		ignoreSet.add("MOLOWVOL");
		ignoreSet.add("GOLD1");
		ignoreSet.add("MID150BEES");
		ignoreSet.add("MON100");
		ignoreSet.add("NIF100BEES");
		ignoreSet.add("LICNFNHGP");
		ignoreSet.add("SENSEXBEES");
		ignoreSet.add("BANKNIFTY1");
		ignoreSet.add("SBISENSEX");
		ignoreSet.add("PSUBNKBEES");
		ignoreSet.add("11AMD");
		ignoreSet.add("ABSLPSE");
		ignoreSet.add("MOM50");
		ignoreSet.add("MOLOWVOL");
		ignoreSet.add("MIDSMALL");
		ignoreSet.add("HDFCSML250");
		ignoreSet.add("HDFCNIF100");
		ignoreSet.add("NIFTYQLITY");
		ignoreSet.add("MAFANG");
		ignoreSet.add("AXSENSEX");
		ignoreSet.add("HDFCMID150");
		ignoreSet.add("LICNMID100");
		ignoreSet.add("HDFCNIFIT");
		ignoreSet.add("MAKEINDIA");
		ignoreSet.add("08MPD");
		ignoreSet.add("ESG");
		ignoreSet.add("11DPR");
		ignoreSet.add("11DPD");
		ignoreSet.add("MOHEALTH");
		ignoreSet.add("MASPTOP50");
		ignoreSet.add("LOWVOL");
		ignoreSet.add("HEALTHY");
		ignoreSet.add("MIDQ50ADD");
		ignoreSet.add("ICICIB22");
		ignoreSet.add("MID150CASE");
		ignoreSet.add("HDFCSENSEX");
		ignoreSet.add("08GPG");
		ignoreSet.add("BANKBEES");
		ignoreSet.add("HDFCNEXT50");
		ignoreSet.add("MONQ50");
		ignoreSet.add("11MPR");
		ignoreSet.add("11MPD");
		ignoreSet.add("JUNIORBEES");
		ignoreSet.add("BSLNIFTY");
		ignoreSet.add("HDFCMOMENT");
		ignoreSet.add("ABSLNN50ET");
		ignoreSet.add("EVINDIA");
		ignoreSet.add("MAHKTECH");
		ignoreSet.add("11GPG");
		ignoreSet.add("MOMENTUM");
		ignoreSet.add("TECH");
		ignoreSet.add("INFRABEES");
		ignoreSet.add("QNIFTY");
		ignoreSet.add("HDFCVALUE");
		ignoreSet.add("HDFCPSUBK");
		ignoreSet.add("NIEHSPI");
		ignoreSet.add("NIEHSPD");
		ignoreSet.add("NIEHSPE");
		ignoreSet.add("NIESSPA");
		ignoreSet.add("NIEHSPL");
		ignoreSet.add("NIESSPC");
		ignoreSet.add("MOMOMENTUM");
		ignoreSet.add("PSUBANKADD");
		ignoreSet.add("UTISXN50");
		ignoreSet.add("UTINEXT50");
		ignoreSet.add("HDFCGROWTH");
		ignoreSet.add("IDFSENSEXE");
		ignoreSet.add("HDFCPVTBAN");
		ignoreSet.add("NAVINIFTY");
		ignoreSet.add("HEALTHADD");
		ignoreSet.add("NIF100BEES");
		ignoreSet.add("NIFMID150");
		ignoreSet.add("IVZINGOLD");
		ignoreSet.add("SILVERADD");
		ignoreSet.add("GILT5YBEES");
		ignoreSet.add("NV20BEES");
		ignoreSet.add("DIVOPPBEES");
		ignoreSet.add("SILVERBEES");
		ignoreSet.add("AUTOBEES");
		ignoreSet.add("ITBEES");
		ignoreSet.add("SDL26BEES");
		ignoreSet.add("GSEC10YEAR");
		ignoreSet.add("GOLD1");
		ignoreSet.add("LIQUIDBEES");
		ignoreSet.add("HDFCGOLD");
		ignoreSet.add("MID150BEES");
		ignoreSet.add("CONSUMBEES");
		ignoreSet.add("MON100");
		ignoreSet.add("LICMFGOLD");
		ignoreSet.add("GOLDBEES");
		ignoreSet.add("HNGSNGBEES");
		ignoreSet.add("LTGILTBEES");
		ignoreSet.add("NV20");
		ignoreSet.add("PHARMABEES");
		ignoreSet.add("AXISGOLD");
		ignoreSet.add("NIEHSPG");
		ignoreSet.add("NIESSPE");
		ignoreSet.add("IVZINNIFTY");
		ignoreSet.add("08AGG");
		ignoreSet.add("NIESSPJ");
		ignoreSet.add("HDFCSILVER");
		ignoreSet.add("GOLDCASE");
		ignoreSet.add("AXISNIFTY");
		ignoreSet.add("ABGSEC");
		ignoreSet.add("MOREALTY");
		ignoreSet.add("SILVER");
		ignoreSet.add("CONS");
		ignoreSet.add("LIQUID");
		ignoreSet.add("MOGSEC");
		ignoreSet.add("IDFNIFTYET");
		ignoreSet.add("IT");
		ignoreSet.add("SBISILVER");
		ignoreSet.add("MOSMALL250");
		ignoreSet.add("AXISILVER");
		ignoreSet.add("MNC");
		ignoreSet.add("ALPHA");
		ignoreSet.add("ESILVER");
		ignoreSet.add("LIQUIDSBI");
		ignoreSet.add("SILVER1");
		ignoreSet.add("EGOLD");
		ignoreSet.add("HDFCLIQUID");
		ignoreSet.add("MONIFTY500");
		ignoreSet.add("LIQUIDCASE");
		ignoreSet.add("LIQUID1");
		ignoreSet.add("MIDCAP");
		ignoreSet.add("GROWEV");
		ignoreSet.add("GSEC10ABSL");
		ignoreSet.add("GROWWEV");
		ignoreSet.add("MODEFENCE");
		ignoreSet.add("MULTICAP");
		ignoreSet.add("TOP10ADD");
		ignoreSet.add("08QPR");
		ignoreSet.add("NPBET");
				ignoreSet.add("SDL24BEES");
						ignoreSet.add("BBNPPGOLD");
								ignoreSet.add("TATAGOLD");
										ignoreSet.add("LIQUIDSHRI");
										ignoreSet.add("EBANKNIFTY");
										ignoreSet.add("GROWWLIQID");
										ignoreSet.add("MOMENTUM50");
										ignoreSet.add("GROWWLIQID");
										ignoreSet.add("BANKPSU");
		ignoreSet.add("METAL");
		ignoreSet.add("GROWWDEFNC");
		ignoreSet.add("GROWWGOLD");
		ignoreSet.add("EMULTIMQ");
		ignoreSet.add("LIQUIDPLUS");
	}

	@Autowired
	private QueueService queueService;

	@Autowired private EventProducerService eventProducerService;

	@Override
	public void process(Exchange exchange) throws Exception {

		DownloadCounterUtil.reset();

		log.info("Starting Bhav Processor and resetting apiCounter to {}", DownloadCounterUtil.get());

		@SuppressWarnings("unchecked")
		List<StockPriceIN> dailyStockPriceList = (List<StockPriceIN>) exchange.getIn().getBody();

		Set<StockPriceIN> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);

		List<StockPriceIN> listToProcess =  new ArrayList<>();

		dailyStockPriceSet.forEach( sp -> {

			if(!this.isIgnored(sp.getNseSymbol())
					&& !sp.getNseSymbol().contains("ETF")
					) {

				listToProcess.add(sp);
				//this.processBhav(sp);

			}

		});

		String lastSymbolToProcess = listToProcess.get(listToProcess.size() -1).getNseSymbol();

		log.info("last symbol to process {}", lastSymbolToProcess);

		listToProcess.forEach(sp -> {

			this.processBhav(sp, lastSymbolToProcess);

		});


		log.info("Completed Bhav Processor");
	}

	private void processBhav(StockPriceIN stockPriceIN, String lastSymbolToProcess){

		log.info("{} Processing Bhav  {} {} {} {} {}", stockPriceIN.getNseSymbol(), stockPriceIN.getSource(),
				stockPriceIN.getIsin(),
				stockPriceIN.getNseSymbol(),
				stockPriceIN.getSeries(),
				stockPriceIN.getTimestamp());

			if (stockPriceIN.getSource().equalsIgnoreCase("NSE")) {
				this.processNseBhav(stockPriceIN, lastSymbolToProcess);
			}

			if (stockPriceIN.getSource().equalsIgnoreCase("BSE")) {
				this.processBseBhav(stockPriceIN, lastSymbolToProcess);
			}
	}

	private void sendToUpdateQueue(StockPriceIO stockPriceIO, String lastSymbolToProcess ){
		log.info("{} Queuing to update price.", stockPriceIO.getNseSymbol());

		if(stockPriceIO.getNseSymbol().equalsIgnoreCase(lastSymbolToProcess)){
			stockPriceIO.setLastRecordToProcess(Boolean.TRUE);
		}

		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);
		//this.createEvent(stockPriceIO);

	}

	private void processNseBhav(StockPriceIN stockPriceIN, String lastSymbolToProcess){


			if(stockPriceIN.getSeries().equalsIgnoreCase("EQ")
				|| stockPriceIN.getSeries().equalsIgnoreCase("BE")) {

				//if(stockPriceIN.getSeries().equalsIgnoreCase("EQ")) {

				StockPriceIO stockPriceIO = new StockPriceIO(stockPriceIN.getSource().toUpperCase(), stockPriceIN.getCompanyName(),stockPriceIN.getNseSymbol(), stockPriceIN.getSeries(), stockPriceIN.getOpen(), stockPriceIN.getHigh(), stockPriceIN.getLow(), stockPriceIN.getClose(), stockPriceIN.getLast(), stockPriceIN.getPrevClose(), stockPriceIN.getTottrdqty(), stockPriceIN.getTottrdval(), stockPriceIN.getTimestamp().toString(), stockPriceIN.getTotaltrades(), stockPriceIN.getIsin());

			this.sendToUpdateQueue(stockPriceIO, lastSymbolToProcess);

		}else{
			log.info("{} Ignored to process NSE Bhav as Series is {}", stockPriceIN.getNseSymbol(), stockPriceIN.getSource(), stockPriceIN.getSeries());
		}
	}

	private void processBseBhav(StockPriceIN stockPriceIN, String lastSymbolToProcess){

		if(stockPriceIN.getSeries().equalsIgnoreCase("A")
				|| stockPriceIN.getSeries().equalsIgnoreCase("B")
				) {

			StockPriceIO stockPriceIO = new StockPriceIO(stockPriceIN.getSource().toUpperCase(), stockPriceIN.getCompanyName(), stockPriceIN.getNseSymbol(), stockPriceIN.getSeries(), stockPriceIN.getOpen(), stockPriceIN.getHigh(), stockPriceIN.getLow(), stockPriceIN.getClose(), stockPriceIN.getLast(), stockPriceIN.getPrevClose(), stockPriceIN.getTottrdqty(), stockPriceIN.getTottrdval(), stockPriceIN.getTimestamp().toString(), stockPriceIN.getTotaltrades(), stockPriceIN.getIsin());

			stockPriceIO.setBseCode(stockPriceIN.getExchangeCode());

			this.sendToUpdateQueue(stockPriceIO, lastSymbolToProcess);

		}else{
			log.info("{} Ignored to process BSE Bhav as series is {}", stockPriceIN.getNseSymbol(), stockPriceIN.getSource(), stockPriceIN.getSeries());
		}
	}

	private boolean isIgnored(String nseSymbol){

		if(nseSymbol.endsWith("-RE") || nseSymbol.endsWith("-RE1") || nseSymbol.endsWith("-RE2")){
			return Boolean.TRUE;
		}

		return ignoreSet.contains(nseSymbol)? Boolean.TRUE : Boolean.FALSE;
	}


	private void createEvent(StockPriceIO stockPriceIO) {
		Message<StockPriceIO> message = new Message<>(stockPriceIO);
		eventProducerService.create(QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE, message);
		log.info("send event");
	}
}
