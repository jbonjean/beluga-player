function init()
{
	var s = document.getElementById("query");
	s.addEventListener("search", function(e) {
    	search(s.value);
	}, false);
}

function submitSearch()
{
	search(document.getElementById("query").value);
	
}

function search(query)
{
	if(query != null && query != "")
	{
		displayLoader();
		sendNSCommand('search', query);
	}
}
