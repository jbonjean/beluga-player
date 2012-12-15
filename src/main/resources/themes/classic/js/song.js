function init()
{
	$('.drop-menu .title').click(
		function() {
			$('.plus').addClass('hover');
			$('.sub-menu').show();
		}
	);
	$('.drop-menu').mouseleave(
		function() {
			$('.plus').removeClass('hover');
			$('.sub-menu').hide();
		}
	);
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

function hideMenu()
{
	$('.drop-menu .title,.drop-menu .sub-menu').mouseleave();
}
