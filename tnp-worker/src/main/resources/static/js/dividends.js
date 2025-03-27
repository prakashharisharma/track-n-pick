$(document).ready(function() {
			loadTable();
			
			$("#addDividendForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
			
		});


function loadTable(){
	$('#example').DataTable({
		"ajax" : {
			"url" : "/api/dividends/recent",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "quantity"
		},{
			"data" : "perShareAmount"
		},{
			"data" : "exDate"
		},{
			"data" : "recordDate"
		},{
			"data" : "transactionDate"
		},{
			"data" : "totalAmount"
		} ],
		rowCallback : function(row, data, index) {
			/*if (data.profitPer > 0.0) {
				$(row).find('td:eq(6)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(6)').css('color', 'red');
			}*/
			
		}
	});
}


function submitForm(){
	
	var dividendStock = {
			stockid: $('#addDividendForm').find('input[name="stockid"]').val(),
			symbol: $('#addDividendForm').find('input[name="symbol"]').val(),
			perShareAmount: $('#addDividendForm').find('input[name="perShareAmount"]').val(),
			exDate: $('#addDividendForm').find('input[name="exDate"]').val(),
			recordDate: $('#addDividendForm').find('input[name="recordDate"]').val(),
			transactionDate: $('#addDividendForm').find('input[name="transactionDate"]').val()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url: "/api/dividends/adddividend",
		data: JSON.stringify(dividendStock),
		cache:false,
		success: function(response){
			$("#add-dividend-modal").modal('hide');
			$('#example').DataTable().ajax.reload();
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}

function sellStock(){
	
	var stock = {
			stockid: $('#sellStockForm').find('input[name="stockid"]').val(),
			symbol: $('#sellStockForm').find('input[name="symbol"]').val(),
			buySellPrice: $('#sellStockForm').find('input[name="buySellPrice"]').val(),
			qunatity: $('#sellStockForm').find('input[name="qunatity"]').val(),
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url: "/api/portfolio/sellstock",
		data: JSON.stringify(stock),
		cache:false,
		success: function(response){
			$("#sell-stock-modal").modal('hide');
			$('#example').DataTable().ajax.reload();
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}

$(function() {
    function displayResult_s(item) {
    	
    	//Set the Value
    	$('#stock_s').val(item.text);
    	$('#stock_s_id').val(item.value);
    	
        $('.alert').show().html('You selected <strong>' + item.value + '</strong>: <strong>' + item.text + '</strong>');
    }
    function displayResult_b(item) {
    	
    	//Set the Value
    	$('#stock_b').val(item.text);
    	$('#stock_b_id').val(item.value);
    	
        $('.alert').show().html('You selected <strong>' + item.value + '</strong>: <strong>' + item.text + '</strong>');
    }
   
    $('#stock_s').typeahead({
        ajax: '/api/portfolio/searchstock',
        displayField: 'companyNameAndSymbol',
        valueField: 'id',
        onSelect: displayResult_s
    });

    $('#stock_b').typeahead({
        ajax: '/api/portfolio/searchstock',
        displayField: 'companyNameAndSymbol',
        valueField: 'id',
        onSelect: displayResult_b
    });
    
});