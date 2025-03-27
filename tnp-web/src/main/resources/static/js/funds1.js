$(document).ready(function() {
			loadTable();
			

			$("#manageFundsForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
		});



function loadTable(){
	$('#example').DataTable({
		"ajax" : {
			"url" : "/api/funds/recenthistory/",
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
			if (data.txnType == 'Add') {
				$(row).find('td:eq(2)').css('color', 'green');
			}
			if (data.txnType == 'Withdraw') {
				$(row).find('td:eq(2)').css('color', 'red');
			}
			
		}
	});
}

function resetForm(){
	$("#manageFundsForm select").val("0").change();
	$("#txnDate").val("dd-mm-yyyy");
	 $("#amount").val("0.00");
}

function submitForm(){
	
	var fund = {
			txnType: $('#manageFundsForm').find(":selected").text(),
			txnDate: $('#manageFundsForm').find('input[name="txnDate"]').val(),
			amount: $('#manageFundsForm').find('input[name="amount"]').val()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url: "/api/funds/managefunds",
		data: JSON.stringify(fund),
		cache:false,
		success: function(response){
			$('#example').DataTable().ajax.reload();
			resetForm();
			//$('.alert').show().html('<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a><strong> Funds ' + 'Added' + ' Successfully  </strong>');
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
