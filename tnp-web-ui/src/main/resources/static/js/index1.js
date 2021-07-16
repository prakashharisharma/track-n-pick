$(document).ready(function(){	
	$("#contactForm").submit(function(event){
		submitForm();
		return false;
	});
});

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
		//dataType: 'json',
		success: function(response){
			$("#contact-modal").modal('hide');
		},
		error: function(e){
			alert("Error" + e);
			console.log("ERROR: ", e);
		}
	});
}