$(document).ready(function() {
	
	  $('#resuls').hide();

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

	        		$('#addStockForm').get(0).reset();
	        		
	        		$('#nseSymbol').html('');
	        		$('#valuation').html('');
	        		
	        		 $('#sector').html('');
	        		 $('#marketCap').html('');
	        	     $('#dividend').html('');
	        		
	        		
	        	     $('#currentPrice').html('');
	        	     $('#yearLow').html('');
	        	     $('#yearHigh').html('');
	        	     
	        	     
	        	     $('#currentRatio').html('');
	        	     $('#quickRatio').html('');
	        	     $('#debtEquity').html('');
	        	     
	        	     $('#pe').html('');
	        	     $('#sectorPe').html('');
	        	     $('#pb').html('');
	        	     
	        	     
	        	     $('#returnOnEquity').html('');
	        	     $('#returnOnCapital').html('');
	        	     $('#rsi').html('');
	        		 
	        		 
	        	     $('#nseSymbol').append(data.nseSymbol);
	        	     $('#valuation').append(data.valuation);
	        	     
	        	     $('#sector').append(data.sector);
	        	     $('#marketCap').append(data.marketCap.toFixed(2));
	        	     $('#dividend').append(data.dividend.toFixed(2));
	        		
	        		
	        	     $('#currentPrice').append(data.currentPrice.toFixed(2));
	        	     $('#yearLow').append(data.yearLow.toFixed(2));
	        	     $('#yearHigh').append(data.yearHigh.toFixed(2));
	        	     
	        	     
	        	     $('#currentRatio').append(data.currentRatio.toFixed(2));
	        	     $('#quickRatio').append(data.quickRatio.toFixed(2));
	        	     $('#debtEquity').append(data.debtEquity.toFixed(2));
	        	     
	        	     $('#pe').append(data.pe.toFixed(2));
	        	     $('#sectorPe').append(data.sectorPe.toFixed(2));
	        	     $('#pb').append(data.pb.toFixed(2));
	        	     
	        	     
	        	     $('#returnOnEquity').append(data.returnOnEquity.toFixed(2));
	        	     $('#returnOnCapital').append(data.returnOnCapital.toFixed(2));
	        	     $('#rsi').append(data.rsi.toFixed(2));
	        	     
	        
	        });
	    }


	    $('#stock').typeahead({
	        ajax: '/public/api/stocks/searchstock',
	        displayField: 'companyNameAndSymbol',
	        valueField: 'id',
	        onSelect: displayResult
	    });

});

