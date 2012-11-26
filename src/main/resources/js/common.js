window.onload = initGlobal;
function displayLoader() {
	document.getElementById('loader').style.display = 'block';
}
function disableDragAndDrop(elements) {
	for (var i = 0; i < elements.length; i++) {
		elements[i].ondragstart = function() { return false; };
	}
}
function initGlobal() {
	disableDragAndDrop(document.getElementsByTagName("a"));
	disableDragAndDrop(document.getElementsByTagName("img"));
	document.getElementById("errors").addEventListener("webkitAnimationEnd", function(event, element) {
    	document.getElementById("errors").style.display = "none";
	}, false );
	
	// call spage specific init
	init();
}