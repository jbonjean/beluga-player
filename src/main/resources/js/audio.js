function init()
{
}

// disable plugin search feature, this make JWebBrowser crash
$.jPlayer.prototype._checkForFlash = function(version){ return false; };

var mutedVolume = 0;
$(document).ready(function(){
    $("#jquery_jplayer_1").jPlayer({
	    ready: function () {
	    		#if ($song)
				    $(this).jPlayer("setMedia", {
					    m4a: '$song.getAudioUrlMap().get("lowQuality").getAudioUrl()'
				    }).jPlayer("play");
				#end
				//$(this).jPlayer('volume', mutedVolume);
				//alert($("#jp_audio_1").muted);
			},
		ended: function() {
			sendNSCommand('next');
		},
		volumechange: function (event) {
			var options = event.jPlayer.options;
			if(options.muted && options.volume != 0)
			{
				document.getElementById('background_audio').muted = true;
				//mutedVolume = options.volume;
				//$(this).jPlayer('volume', 0);
			}
			else if(!options.muted && options.volume == 0)
			{
				$(this).jPlayer('volume', mutedVolume);
			}
			//$("#jp_audio_1").prop('muted', true);
		},
		volume: 0,
    	supplied: "m4a",
    	preload:"auto",
    	solution: "html"
 	});
});