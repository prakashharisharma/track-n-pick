$(document).ready(function() {
		
			
			$("#downloadForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
			
		});


function submitForm(){
	
	
	var uiDownload = {
			downloadDate: $('#downloadForm').find('input[name="downloadDate"]').val(),
			downloadType: $('#downloadForm').find(":selected").text()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url:"/api/download/initiate",
		data: JSON.stringify(uiDownload),
		cache:false,
		success: function(response){
			$("#add-download-modal").modal('hide');
			 $('.alert').show().html('Download initiated Success');
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
