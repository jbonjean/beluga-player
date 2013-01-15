$.noty.defaults = {
	layout: 'bottomRight',
	theme: 'default',
	type: 'error',
	text: '',
	dismissQueue: true,
	template: '<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
	animation: {
		open: {height: 'toggle'},
		close: {height: 'toggle'},
		easing: 'swing',
		speed: 500
	},
	timeout: 3000,
	force: false,
	modal: false,
	closeWith: ['click'],
	callback: {
		onShow: function() {},
		afterShow: function() {},
		onClose: function() {},
		afterClose: function() {}
	},
	buttons: false
};

var infoAnimation = {
	open: {height: 'toggle'},
	close: {height: 'toggle'},
	easing: 'swing',
	speed: 0
};

function showError(message)
{
    noty({dismissQueue: true, type: 'error', timeout: 3000, text: message});
}

function showInfo(message)
{
	$.noty.closeAll();
    noty({dismissQueue: false, type: 'alert', timeout: 0, animation: infoAnimation, layout: 'top', text: message});
}

function showSuccess(message)
{
    noty({dismissQueue: true, type: 'success', timeout: 2000, text: message});
}

function showFatal()
{
	noty({
		dismissQueue: false,
		type: 'error',
		timeout: 0,
		text: '$text["fatal.error"]', 
		buttons:
		[
			{
				addClass: 'btn btn-primary', text: '$text["exit"]', onClick: function()
				{
					sendNSCommand('exit');
				}
		    },
		    {
		    	addClass: 'btn btn-danger', text: '$text["retry"]', onClick: function()
		    	{
		    		sendNSCommand('login');
				}
			}
		]
	});
}