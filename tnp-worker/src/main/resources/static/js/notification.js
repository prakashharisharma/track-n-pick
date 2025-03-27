$(document).ready(function() {
		
			
			$("#notificationForm").submit(function(event){
				submitForm();
				return false;
			});
			
			
			
		});


function submitForm(){
	
	
	var uiNotification = {
			
			notificationType: $('#notificationForm').find(":selected").text()
        };
	
	 $.ajax({
		type: "POST",
        contentType: 'application/json',
		url:"/api/notification/initiate",
		data: JSON.stringify(uiNotification),
		cache:false,
		success: function(response){
			$("#add-notification-modal").modal('hide');
			 $('.alert').show().html('Notification initiated Success');
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}
