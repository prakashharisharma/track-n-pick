$(document).ready(function() {
	
	  $('#resuls').hide();
	  $('#resultTechnicals').hide();
	  $('#resultNotifications').hide();

});



$(function() {
	 function displayResult(item) {
		 
	    	//Set the Value
	    	$('#stock').val(item.text);
	    	$('#stock_id').val(item.value);
	    	
	        $.ajax({
	            url: "/public/api/stocks/"+item.value
	        }).then(function(data) {
	    	
	        		$('#resuls').show();
	        		
	        		$('#resultTechnicals').show();
	        		
	        		 $('#resultNotifications').show();

	        		$('#addStockForm').get(0).reset();
	        		
	        		$('#nseSymbol').html('');
	        		$('#valuation').html('');
	        		
	        		 $('#sector').html('');
	        		 $('#marketCap').html('');
	        	     $('#dividend').html('');
	        	     $('#indice').html('');
	        		
	        	     $('#currentPrice').html('');
	        	     $('#yearLow').html('');
	        	     $('#yearHigh').html('');
	        	     
	        	     
	        	     $('#currentRatio').html('');
	        	     $('#quickRatio').html('');
	        	     $('#debtEquity').html('');
	        	     
	        	     $('#pe').html('');
	        	     $('#sectorPe').html('');
	        	     $('#sectorPb').html('');
	        	     $('#pb').html('');
	        	     
	        	     
	        	     $('#returnOnEquity').html('');
	        	     $('#returnOnCapital').html('');
	        	     
	        	     $('#rsi').html('');
	        	     $('#sok').html('');
	        	     $('#sod').html('');
	        	     
	        	     $('#obv').html('');
	        	     $('#rocv').html('');
	        	     
	        	     $('#ema20').html('');
	        	     $('#ema50').html('');
	        	     $('#ema100').html('');
	        	     $('#ema200').html('');
	        	     
	        	     $('#crossOver').html('');
	        	     $('#breakOut').html('');
	        		 
	        	     $('#nseSymbol').append(data.nseSymbol);
	        	     $('#valuation').append(data.valuation);
	        	     
	        	     $('#sector').append(data.sector);
	        	     $('#marketCap').append(data.marketCap.toFixed(2));
	        	     $('#dividend').append(data.dividend.toFixed(2));
	        	     $('#indice').append(data.indice);
	        		
	        	     $('#currentPrice').append(data.currentPrice.toFixed(2));
	        	     $('#yearLow').append(data.yearLow.toFixed(2));
	        	     $('#yearHigh').append(data.yearHigh.toFixed(2));
	        	     
	        	     
	        	     $('#currentRatio').append(data.currentRatio.toFixed(2));
	        	     $('#quickRatio').append(data.quickRatio.toFixed(2));
	        	     $('#debtEquity').append(data.debtEquity.toFixed(2));
	        	     
	        	     $('#pe').append(data.pe.toFixed(2));
	        	     $('#sectorPe').append(data.sectorPe.toFixed(2));
	        	     $('#sectorPb').append(data.sectorPe.toFixed(2));
	        	     $('#pb').append(data.pb.toFixed(2));
	        	     
	        	     
	        	     $('#returnOnEquity').append(data.returnOnEquity.toFixed(2));
	        	     $('#returnOnCapital').append(data.returnOnCapital.toFixed(2));
	        	     
	        	     $('#rsi').append(data.rsi.toFixed(2));
	        	     $('#sok').append(data.sok.toFixed(2));
	        	     $('#sod').append(data.sod.toFixed(2));
	        	     
	        	     $('#obv').append(data.obv);
	        	     $('#rocv').append(data.rocv.toFixed(2));
	        	     
	        	     $('#ema20').append(data.ema20.toFixed(2));
	        	     $('#ema50').append(data.ema50.toFixed(2));
	        	     $('#ema100').append(data.ema100.toFixed(2));
	        	     $('#ema200').append(data.ema200.toFixed(2));
	        
	        	     $('#crossOver').append(data.crossOver);
	        	     $('#breakOut').append(data.breakOut);
	        	     
	        	     
	        });
	    }

	 
	 
	 

	    $('#stock').typeahead({
	        ajax: '/public/api/stocks/searchstock',
	        displayField: 'companyNameAndSymbol',
	        valueField: 'id',
	        onSelect: displayResult
	    });

});

