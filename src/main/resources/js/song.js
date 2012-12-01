function init()
{
}

function updateTime(text)
{
	document.getElementById("time").innerHTML = text;
}

function deleteStation()
{
	if(confirm("$text['are.you.sure.you.want.to.delete.this.station']")) 
		sendNSCommand('delete-station');
}
