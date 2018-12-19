$(document).ready(function() {
			loadTable();
			
			$("#addStockForm").submit(function(event){
				submitForm();
				return false;
			});
			
			$("#sellStockForm").submit(function(event){
				sellStock();
				return false;
			});
			
			
			
			
		});


function loadTable(){
	$('#example').DataTable({
		"ajax" : {
			"url" : "/api/portfolio/",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "qunatity"
		},{
			"data" : "averagePrice"
		},{
			"data" : "currentPrice"
		},{
			"data" : "yearLow"
		},{
			"data" : "yearHigh"
		},{
			"data" : "profitPer"
		} ],
		rowCallback : function(row, data, index) {
			if (data.profitPer > 0.0) {
				$(row).find('td:eq(6)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(6)').css('color', 'red');
			}
			
		}
	});
}


function submitForm(){
	
	var stock = {
			stockid: $('#addStockForm').find('input[name="stockid"]').val(),
			symbol: $('#addStockForm').find('input[name="symbol"]').val(),
			buySellPrice: $('#addStockForm').find('input[name="buySellPrice"]').val(),
			qunatity: $('#addStockForm').find('input[name="qunatity"]').val(),
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url: "/api/portfolio/addstock",
		data: JSON.stringify(stock),
		cache:false,
		success: function(response){
			$("#add-stock-modal").modal('hide');
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
        ajax: '/api/stocks/searchstock',
        displayField: 'companyNameAndSymbol',
        valueField: 'id',
        onSelect: displayResult_b
    });
    
});