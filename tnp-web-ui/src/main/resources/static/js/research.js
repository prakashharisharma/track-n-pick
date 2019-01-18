$(document).ready(function() {
			loadTable();
		});

$(document).ready(function(){	
	$("#contactForm").submit(function(event){
		submitForm();
		return false;
	});
});


function loadTable(){
	$('#example').DataTable({
		"ajax" : {
			"url" : "/api/research/current",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "researchDate"
		},{
			"data" : "researchPrice"
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
	
	var person = {
            name: $('#contactForm').find('input[name="name"]').val(),
            email: $('#contactForm').find('input[name="email"]').val(),
            message: $('#contactForm').find('textarea:input[name="message"]').val()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url: "/portfolio/saveContact",
		data: JSON.stringify(person),
		cache:false,
		success: function(response){
			$("#contact-modal").modal('hide');
			$('#example').DataTable().ajax.reload();
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}

