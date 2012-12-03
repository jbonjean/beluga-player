function init()
{
}

// disable plugin search feature, this make JWebBrowser crash
$.jPlayer.prototype._checkForFlash = function(version){ return false; };

var mutedVolume = $mutedVolume;

$(document).ready(function(){
    $("#jquery_jplayer_1").jPlayer({
	    ready: function (event) {
	    		#if ($song)
				    $(this).jPlayer("setMedia", {
					    m4a: '$song.getAudioUrlMap().get("lowQuality").getAudioUrl()'
				    }).jPlayer("play");
				#end
			},
		play: function (event) {
			//alert("play");
		},
		ended: function() {
			sendNSCommand('next');
		},
		volumechange: function (event) {
			var options = event.jPlayer.options;
			
			// mute and volume > 0, we need to store the volume value
			// to restore it later
			if(options.muted && options.volume != 0)
			{
				mutedVolume = options.volume;
				$(this).jPlayer('volume', 0);
			}
			// unmute (not muted and volume = 0), we restore the volume value
			else if(!options.muted && options.volume == 0)
			{
				$(this).jPlayer('volume', mutedVolume);
			}
			sendNSCommand('store-volume/' + options.volume + "/" + mutedVolume);
		},
		volume: $volume,
		muted: #if ($volume == 0) true #else false #end,
    	supplied: "m4a",
    	preload:"auto",
    	solution: "html"
 	});
});