function updateTime(text) {
	document.getElementById("time").innerHTML = text;
}
window.onload = init;
function init() {
	var elements = document.getElementsByTagName("a,img");
	for (var i = 0; i < elements.length; i++) {
		elements[i].ondragstart = function() { return false; };
	} 
}
function displayLoader() {
	document.getElementById('loader').style.display = 'block';
}