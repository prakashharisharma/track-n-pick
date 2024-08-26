$(document).ready(function() {
    $.ajax({
        url: "/api/dashboard/performance"
    }).then(function(data) {
       $('#ytdInvestmentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.ytdInvestmentValue.toFixed(2));
       $('#currentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.currentValue.toFixed(2));
       $('#ytdRealizedGainPer').append(data.ytdRealizedGainPer.toFixed(2) + ' %');
       $('#ytdUnrealizedGainPer').append(data.ytdUnrealizedGainPer.toFixed(2) +' %');

       $('#fyXirr').append(data.fyXirr.toFixed(2) + ' %');
       $('#fyCagr').append(data.fyCagr.toFixed(2) +' %');

       $('#fyInvestmentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyInvestmentValue.toFixed(2));
       $('#fyCurrentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.currentValue.toFixed(2));
       $('#fyRealizedGainPer').append(data.fyRealizedGainPer.toFixed(2) + ' %');
       $('#fyUnrealizedGainPer').append(data.fyUnrealizedGainPer.toFixed(2) +' %');
       
       $('#fyNetGain').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyNetGain.toFixed(2));
       $('#fyNetDividends').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyNetDividends.toFixed(2));
       $('#fyNetTaxPaid').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyNetTaxPaid.toFixed(2));
       $('#fyNetExpense').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyNetExpense.toFixed(2));
       $('#fyNetTaxLiability').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyNetTaxLiability.toFixed(2));

        if(data.fyXirr <= 0.00){
     	   $('#fyXirrTxt').removeClass('text-success').addClass('text-danger');
        }

        if(data.fyCagr <= 0.00){
     	   $('#fyCagrTxt').removeClass('text-success').addClass('text-danger');
        }

       if(data.ytdRealizedGainPer < 0.00){
    	   $('#ytdRealizedGainPerTxt').removeClass('text-success').addClass('text-danger');
       }
       
       if(data.ytdUnrealizedGainPer < 0.00){
    	   $('#ytdUnrealizedGainPerTxt').removeClass('text-success').addClass('text-danger');
       }
       
       if(data.fyRealizedGainPer < 0.00){
    	   $('#fyRealizedGainPerTxt').removeClass('text-success').addClass('text-danger');
       }
       
       if(data.fyUnrealizedGainPer < 0.00){
    	   $('#fyUnrealizedGainPerTxt').removeClass('text-success').addClass('text-danger');
       }
       
       if(data.fyNetExpense > (data.fyNetGain + data.fyNetDividends)){
    	   $('#fyNetExpenseTxt').removeClass('text-success').addClass('text-danger');
       }
       
       
    });

    loadWeeklyChart();
    loadPieChart();
    loadChart();
    loadIndiceChart();
});

function loadWeeklyChart(){

var dataPoints = [];

var options =  {
	animationEnabled: true,
	theme: "light2",
	title: {
		text: "Monthly Performance"
	},
	axisX: {
		valueFormatString: "DD MMM YYYY",
	},
	axisY: {
		title: "INR",
		titleFontSize: 24
	},
	data: [{
		type: "spline",
		yValueFormatString: "₹#,###.##",
		dataPoints: dataPoints
	}]
};

function addData(data) {
	for (var i = 0; i < data.length; i++) {
		dataPoints.push({
			x: new Date(data[i].date),
			y: data[i].units
		});
	}
	$("#weeklychartContainer").CanvasJSChart(options);

}
//$.getJSON("https://canvasjs.com/data/gallery/jquery/daily-sales-data.json", addData);
$.getJSON("/api/chart/performance/monthly", addData);
}


function loadPieChart() {

	var dataPoints = [];
	
	var options = {
		title: {
			text: "Sectoral Allocation"
		},
		subtitles: [{
			text: "As of today"
		}],
		animationEnabled: true,
		data: [{
			type: "pie",
			startAngle: 40,
			toolTipContent: "<b>{label}</b>: {y}%",
			showInLegend: "true",
			legendText: "{label}",
			indexLabelFontSize: 16,
			indexLabel: "{label} - {y}%",
			dataPoints: dataPoints
		}]
	};
	//$("#sectorChartContainer").CanvasJSChart(options);

	function addData(data) {
		for (var i = 0; i < data.length; i++) {
			dataPoints.push({
				y: data[i].allocation.toFixed(2),
				label: data[i].sector
			});
		}
		$("#sectorChartContainer").CanvasJSChart(options);

	}
	
	
	$.getJSON("/api/chart/sector/allocation", addData);
	
	}

function loadIndiceChart() {
	 
	var chart = new CanvasJS.Chart("indiceChartContainer", {
		theme: "light2", // "light1", "dark1", "dark2"
		animationEnabled: true,
		title: {
			text: "Indice Allocation"
		},
		axisX: {
			labelFontSize: 18
		},
		axisY: {
			title: "Allocation Per",
			suffix: "%"
		},
		data: [{
			type: "column",
			indexLabel: "{y}",
			indexLabelFontSize: 18,
			yValueFormatString: "#,##0.0#\"%\""
		}]
	});
	 
	function addData(data) {
		chart.options.data[0].dataPoints = data;
		chart.render();
	}
	 
	$.getJSON("/api/chart/indice/allocation", addData);
	 
	}


function loadChart() {

	var dataPointsCurrentValue = [];
	
	var dataPointsInvestmentValue = [];

	var options = {
		animationEnabled: true,
		theme: "light2",
		title:{
			text: "12 Months Performance"
		},
		axisX:{
			valueFormatString: "MMM YY"
		},
		axisY: {
			title: "INR.",
			suffix: "K",
			minimum: 30
		},
		toolTip:{
			shared:true
		},  
		legend:{
			cursor:"pointer",
			verticalAlign: "bottom",
			horizontalAlign: "left",
			dockInsidePlotArea: true,
			itemclick: toogleDataSeries
		},
		data: [{
			type: "line",
			showInLegend: true,
			name: "Value",
			markerType: "square",
			xValueFormatString: "DD MMM, YYYY",
			color: "#F08080",
			yValueFormatString: "#,##0K",
			dataPoints: dataPointsCurrentValue
		},
		{
			type: "line",
			showInLegend: true,
			name: "Investment",
			lineDashType: "dash",
			yValueFormatString: "#,##0K",
			dataPoints:dataPointsInvestmentValue
		}]
	};
	//$("#chartContainer").CanvasJSChart(options);

	function toogleDataSeries(e){
		if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
			e.dataSeries.visible = false;
		} else{
			e.dataSeries.visible = true;
		}
		e.chart.render();
	}

	
	function addCurrentValueData(data) {
		for (var i = 0; i < data.length; i++) {
			
			dataPointsCurrentValue.push({
				
				x: new Date(data[i].date),
				y: data[i].value
			});
		}
		$("#chartContainer").CanvasJSChart(options);

	}
	
	function addinvestmentvalueData(data) {
		for (var i = 0; i < data.length; i++) {
			
			dataPointsInvestmentValue.push({
				
				x: new Date(data[i].date),
				y: data[i].value
			});
		}
		$("#chartContainer").CanvasJSChart(options);

	}
	
	$.getJSON("/api/chart/performance/currentvalue", addCurrentValueData);
	
	$.getJSON("/api/chart/performance/investmentvalue", addinvestmentvalueData);
	
	
	}
