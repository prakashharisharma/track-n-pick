$(document).ready(function() {
	loadTableFundamental();
	loadTableTechnical();
});

$(document).ready(function() {
	$("#contactForm").submit(function(event) {
		submitForm();
		return false;
	});
});

function loadTableFundamental() {
	$('#fundamental').DataTable({
		"ajax" : {
			"url" : "/api/research/fundamental/advance",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "indice"
		}, {
			"data" : "researchDate"
		}, {
			"data" : "researchPrice",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "currentPrice",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "pe",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "pb",
			
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "roe",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "roc",
			render: $.fn.dataTable.render.number(',', '.', 2)
		},{
			"data" : "peDifference",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, 
		{
			"data" : "profitPer",
			render: $.fn.dataTable.render.number(',', '.', 2)
		} ],
		rowCallback : function(row, data, index) {
			if (data.profitPer > 0.0) {
				$(row).find('td:eq(10)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(10)').css('color', 'red');
			}

		}
	});

}
function loadTableTechnical() {
	$('#technical').DataTable({
		"ajax" : {
			"url" : "/api/research/technical",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "indice"
		}, {
			"data" : "researchDate"
		}, {
			"data" : "researchPrice"
		}, {
			"data" : "currentPrice"
		}, {
			"data" : "sma50"
		}, {
			"data" : "sma200"
		}, {
			"data" : "rsi"
		}, {
			"data" : "profitPer"
		} ],
		rowCallback : function(row, data, index) {
			if (data.profitPer > 0.0) {
				$(row).find('td:eq(8)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(8)').css('color', 'red');
			}

		}
	});
}

function submitForm() {

	var person = {
		name : $('#contactForm').find('input[name="name"]').val(),
		email : $('#contactForm').find('input[name="email"]').val(),
		message : $('#contactForm').find('textarea:input[name="message"]')
				.val()
	};

	$.ajax({
		type : "POST",
		contentType : 'application/json',
		url : "/portfolio/saveContact",
		data : JSON.stringify(person),
		cache : false,
		success : function(response) {
			$("#contact-modal").modal('hide');
			$('#example').DataTable().ajax.reload();
		},
		error : function(e) {
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
