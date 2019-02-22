$(document).ready(function() {
		
			
			$("#updateledgerForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
			
		});


function submitForm(){
	
	
	var uiUpdateledger = {
			updateledgerType: $('#updateledgerForm').find(":selected").text()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url:"/api/updateledger/initiate",
		data: JSON.stringify(uiUpdateledger),
		cache:false,
		success: function(response){
			$("#add-updateledger-modal").modal('hide');
			 $('.alert').show().html('Update initiated Success');
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
