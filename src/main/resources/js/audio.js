function init()
{
}

// disable plugin search feature, this make JWebBrowser crash
$.jPlayer.prototype._checkForFlash = function(version){ return false; };

$(document).ready(function(){
    $("#jquery_jplayer_1").jPlayer({
	    ready: function () {
	    		#if ($song)
				    $(this).jPlayer("setMedia", {
					    m4a: '$song.getAudioUrlMap().get("lowQuality").getAudioUrl()'
				    }).jPlayer("play");
				#end
			},
		
		ended: function() {
			sendNSCommand('next');
		},
    	supplied: "m4a",
    	preload:"auto",
    	solution: "html"
 	});
});