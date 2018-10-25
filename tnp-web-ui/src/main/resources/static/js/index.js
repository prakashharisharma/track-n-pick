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
			"url" : "/portfolio/",
			"dataSrc" : ""
		},
		"columns" : [ {
			"data" : "symbol"
		}, {
			"data" : "company"
		}, {
			"data" : "ltp"
		} ],
		rowCallback : function(row, data, index) {
			if (data.ltp > 20.0) {
				$(row).find('td:eq(2)').css('color', 'green');
			}
			if (data.ltp <= 20.0) {
				$(row).find('td:eq(2)').css('color', 'red');
			}
			if (data.symbol.toUpperCase() == 'ABC') {
				$(row).find('td:eq(0)').css('color', 'blue');
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

