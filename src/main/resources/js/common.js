window.onload = init;
function displayLoader() {
	document.getElementById('loader').style.display = 'block';
}
function disableDragAndDrop(elements) {
	for (var i = 0; i < elements.length; i++) {
		elements[i].ondragstart = function() { return false; };
	}
}
function init() {
	disableDragAndDrop(document.getElementsByTagName("a"));
	disableDragAndDrop(document.getElementsByTagName("img"));
}