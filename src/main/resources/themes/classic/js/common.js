window.onload = initGlobal;

function displayLoader()
{
	document.getElementById('loader').style.display = 'block';
}

function hideLoader()
{
	document.getElementById('loader').style.display = 'none';
}

function disableDragAndDrop(elements)
{
	for (var i = 0; i < elements.length; i++) {
		elements[i].ondragstart = function() { return false; };
	}
}

function initGlobal()
{
	disableDragAndDrop(document.getElementsByTagName("a"));
	disableDragAndDrop(document.getElementsByTagName("img"));

	// call spage specific init
	init();
}