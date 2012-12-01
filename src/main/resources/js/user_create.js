function init()
{
}

function createUser()
{
	var gender = "";
	for (var i=0; i < document.user.gender.length; i++)
	{
		if (document.user.gender[i].checked)
		{
			gender = document.user.gender[i].value;
			break;
		}
	}
	sendNSCommand('create-user', document.user.username.value, document.user.password.value, document.user.birthYear.value, document.user.zipCode.value, gender, document.user.emailOptIn.value);
	return false;
}