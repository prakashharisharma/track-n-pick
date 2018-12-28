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

	var options = {
		title: {
			text: "Desktop OS Market Share in 2017"
		},
		subtitles: [{
			text: "As of November, 2017"
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
			dataPoints: [
				{ y: 48.36, label: "Windows 7" },
				{ y: 26.85, label: "Windows 10" },
				{ y: 1.49, label: "Windows 8" },
				{ y: 6.98, label: "Windows XP" },
				{ y: 6.53, label: "Windows 8.1" },
				{ y: 2.45, label: "Linux" },
				{ y: 3.32, label: "Mac OS X 10.12" },
				{ y: 4.03, label: "Others" }
			]
		}]
	};
	$("#sectorChartContainer").CanvasJSChart(options);

	}


function loadChart() {

	var options = {
		animationEnabled: true,
		theme: "light2",
		title:{
			text: "Actual vs Projected Sales"
		},
		axisX:{
			valueFormatString: "DD MMM"
		},
		axisY: {
			title: "Number of Sales",
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
			name: "Projected Sales",
			markerType: "square",
			xValueFormatString: "DD MMM, YYYY",
			color: "#F08080",
			yValueFormatString: "#,##0K",
			dataPoints: [
				{ x: new Date(2017, 10, 1), y: 63 },
				{ x: new Date(2017, 10, 2), y: 69 },
				{ x: new Date(2017, 10, 3), y: 65 },
				{ x: new Date(2017, 10, 4), y: 70 },
				{ x: new Date(2017, 10, 5), y: 71 },
				{ x: new Date(2017, 10, 6), y: 65 },
				{ x: new Date(2017, 10, 7), y: 73 },
				{ x: new Date(2017, 10, 8), y: 96 },
				{ x: new Date(2017, 10, 9), y: 84 },
				{ x: new Date(2017, 10, 10), y: 85 },
				{ x: new Date(2017, 10, 11), y: 86 },
				{ x: new Date(2017, 10, 12), y: 94 },
				{ x: new Date(2017, 10, 13), y: 97 },
				{ x: new Date(2017, 10, 14), y: 86 },
				{ x: new Date(2017, 10, 15), y: 89 }
			]
		},
		{
			type: "line",
			showInLegend: true,
			name: "Actual Sales",
			lineDashType: "dash",
			yValueFormatString: "#,##0K",
			dataPoints: [
				{ x: new Date(2017, 10, 1), y: 60 },
				{ x: new Date(2017, 10, 2), y: 57 },
				{ x: new Date(2017, 10, 3), y: 51 },
				{ x: new Date(2017, 10, 4), y: 56 },
				{ x: new Date(2017, 10, 5), y: 54 },
				{ x: new Date(2017, 10, 6), y: 55 },
				{ x: new Date(2017, 10, 7), y: 54 },
				{ x: new Date(2017, 10, 8), y: 69 },
				{ x: new Date(2017, 10, 9), y: 65 },
				{ x: new Date(2017, 10, 10), y: 66 },
				{ x: new Date(2017, 10, 11), y: 63 },
				{ x: new Date(2017, 10, 12), y: 67 },
				{ x: new Date(2017, 10, 13), y: 66 },
				{ x: new Date(2017, 10, 14), y: 56 },
				{ x: new Date(2017, 10, 15), y: 64 }
			]
		}]
	};
	$("#chartContainer").CanvasJSChart(options);

	function toogleDataSeries(e){
		if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
			e.dataSeries.visible = false;
		} else{
			e.dataSeries.visible = true;
		}
		e.chart.render();
	}

	}
