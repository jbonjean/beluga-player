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
    noty({dismissQueue: false, type: 'alert', timeout: 0, animation: infoAnimation, text: message});
}