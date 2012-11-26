function init()
{
}

function updateTime(text)
{
	document.getElementById("time").innerHTML = text;
}

function deleteStation(stationName)
{
	if(confirm('Are you sure you want to delete the station "' + stationName + '"?')) 
		sendNSCommand('delete-station');
}
