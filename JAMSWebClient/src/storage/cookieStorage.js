// getCookie returns the cookie if it exists, otherwise null.
export function getCookie(name) {
	let cookies = " " + document.cookie;

	let startIndex = cookies.indexOf(" " + name + "=");

	if (startIndex === -1) {
		return null;
	}

	startIndex = cookies.indexOf("=", startIndex) + 1;

	let endIndex = cookies.indexOf(";", startIndex);

	if (endIndex === -1) {
		endIndex = cookies.length;
	}

	return decodeURIComponent(cookies.substring(startIndex, endIndex));
}

// isAvailable returns whether cookie storage is available.
export function isAvailable() {
	return navigator.cookieEnabled;
}

// removeCookie removes a cookie.
export function removeCookie(name) {
	document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

// setCookie adds a cookie if it does not exist, otherwise it is updated.
export function setCookie(name, value) {
	document.cookie = name + "=" + encodeURIComponent(value);
}
