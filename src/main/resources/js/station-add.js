function init()
{
}

var searchDelay;

function searchSubmit(query)
{
	displayLoader();
	sendNSCommand('search', query);
}

function search(query)
{
	window.clearTimeout(searchDelay);
    searchDelay = setTimeout(function(){searchSubmit(query)},1000);
}