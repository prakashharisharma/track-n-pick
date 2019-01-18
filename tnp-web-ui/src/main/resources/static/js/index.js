$(document).ready(function() {
    $.ajax({
        url: "/api/dashboard/performance"
    }).then(function(data) {
       $('#ytdInvestmentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.ytdInvestmentValue.toFixed(2));
       $('#currentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.currentValue.toFixed(2));
       $('#ytdRealizedGainPer').append(data.ytdRealizedGainPer.toFixed(2) + ' %');
       $('#ytdUnrealizedGainPer').append(data.ytdUnrealizedGainPer.toFixed(2) +' %');
       
       $('#fyInvestmentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.fyInvestmentValue.toFixed(2));
       $('#fyCurrentValue').append('<i class="fa fa-rupee">&nbsp;</i>' + data.currentValue.toFixed(2));
       $('#fyRealizedGainPer').append(data.fyRealizedGainPer.toFixed(2) + ' %');
       $('#fyUnrealizedGainPer').append(data.fyUnrealizedGainPer.toFixed(2) +' %');
       
       
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
       
       
       
    });
    
    loadPieChart();
    loadChart();
});


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
				y: data[i].allocation,
				label: data[i].sector
			});
		}
		$("#sectorChartContainer").CanvasJSChart(options);

	}
	
	
	$.getJSON("http://localhost:8081/api/chart/sector/allocation", addData);
	
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
	
	$.getJSON("http://localhost:8081/api/chart/performance/currentvalue", addCurrentValueData);
	
	$.getJSON("http://localhost:8081/api/chart/performance/investmentvalue", addinvestmentvalueData);
	
	
	}
