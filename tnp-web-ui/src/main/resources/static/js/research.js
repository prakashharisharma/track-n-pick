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
			"data" : "valuation"
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
            "data" : "sectorPe",
           	render: $.fn.dataTable.render.number(',', '.', 2)
        },{
			"data" : "pb",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
            "data" : "sectorPb",
            render: $.fn.dataTable.render.number(',', '.', 2)
        },{
			"data" : "roe",
			render: $.fn.dataTable.render.number(',', '.', 2)
		}, {
			"data" : "roc",
			render: $.fn.dataTable.render.number(',', '.', 2)
		},
		{
			"data" : "profitPer",
			render: $.fn.dataTable.render.number(',', '.', 2)
		},{
            "data" : "bullish"
         }],
		rowCallback : function(row, data, index) {


            if(data.valuation == 50.0){
                $(row).find('td:eq(1)').css('color', '#9ACD32');
            }else if(data.valuation >= 55.0 && data.valuation <= 65.0){
                $(row).find('td:eq(1)').css('color', '#008000');
            }else if(data.valuation >= 80.0 && data.valuation <= 100.0){
                $(row).find('td:eq(1)').css('color', '#006400');
            }else{
                $(row).find('td:eq(1)').css('color', 'red');
            }

			if (data.profitPer > 0.0) {
				$(row).find('td:eq(11)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(11)').css('color', 'red');
			}
            if (data.bullish == true) {
                $(row).find('td:eq(0)').css('color', 'green');
                $(row).find('td:eq(0)').css('font-weight', 'bold');
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
			"data" : "valuation"
		}, {
			"data" : "researchDate"
		}, {
			"data" : "researchPrice"
		}, {
			"data" : "currentPrice"
		}, {
			"data" : "ema5"
		},  {
            "data" : "ema20"
        },{
             "data" : "ema50"
        },
        {
             "data" : "ema200"
        },
        {
			"data" : "rsi"
		}, {
			"data" : "profitPer"
		} ],
		rowCallback : function(row, data, index) {

            if(data.valuation == 50.0){
                $(row).find('td:eq(1)').css('color', '#9ACD32');
            }else if(data.valuation >= 55.0 && data.valuation <= 65.0){
                $(row).find('td:eq(1)').css('color', '#008000');
            }else if(data.valuation >= 80.0 && data.valuation <= 100.0){
                $(row).find('td:eq(1)').css('color', '#006400');
            }else{
                $(row).find('td:eq(1)').css('color', 'red');
            }


			if (data.profitPer > 0.0) {
				$(row).find('td:eq(10)').css('color', 'green');
			}
			if (data.profitPer < 0.0) {
				$(row).find('td:eq(10)').css('color', 'red');
			}

            if(data.currentPrice > data.ema5){
                $(row).find('td:eq(5)').css('color', 'green');
            }else{
                $(row).find('td:eq(5)').css('color', 'red');
            }

            if(data.currentPrice > data.ema20){
                $(row).find('td:eq(6)').css('color', 'green');
            }else{
                $(row).find('td:eq(6)').css('color', 'red');
            }
            if(data.currentPrice > data.ema50){
                $(row).find('td:eq(7)').css('color', 'green');
            }else{
                $(row).find('td:eq(7)').css('color', 'red');
            }
            if(data.currentPrice > data.ema200){
                $(row).find('td:eq(8)').css('color', 'green');
            }else{
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
