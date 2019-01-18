$(document).ready(function() {
			loadTable();
			
			$("#addFundForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
			
		});


function loadTable(){
	$('#example').DataTable({
		"ajax" : {
			"url" :"/api/funds/recenthistory",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "txnDate"
		}, {
			"data" : "amount"
		},{
			"data" : "txnType"
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
	
	
	var fund = {
			txnType: $('#addFundForm').find(":selected").text(),
			txnDate: $('#addFundForm').find('input[name="txnDate"]').val(),
			amount: $('#addFundForm').find('input[name="amount"]').val()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url:"/api/funds/managefunds",
		data: JSON.stringify(fund),
		cache:false,
		success: function(response){
			$("#add-fund-modal").modal('hide');
			$('#example').DataTable().ajax.reload();
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
