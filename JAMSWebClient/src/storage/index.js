// storage provides access to persistent storage. Data is stored in
// localStorage, and if localStorage is not available, in cookies.

import * as cookieStorage from "./cookieStorage";

// Whether the type of storage is available
export let cookieStorageIsAvailable = cookieStorage.isAvailable();
export let localStorageIsAvailable = isLocalStorageAvailable();

// prefix is prefixed to the key when calling get, remove and set.
export let prefix = "";

// get returns an item from the storage. If the key does not exist, defaultValue
// is returned.
export function get(key, defaultValue = null) {
	if (localStorageIsAvailable) {
		const item = window.localStorage.getItem(prefix + key);

		if (item === null) {
			return defaultValue;
		}
		return item;
	}

	if (cookieStorageIsAvailable) {
		const cookie = cookieStorage.getCookie(prefix + key);

		if (cookie === null) {
			return defaultValue;
		}
		return cookie;
	}

	return defaultValue;
}

// remove removes an item from the storage.
export function remove(key) {
	if (localStorageIsAvailable) {
		window.localStorage.removeItem(prefix + key);
		return;
	}

	if (cookieStorageIsAvailable) {
		cookieStorage.removeCookie(prefix + key);
	}
}

// set adds an item to the storage if key does not exist, otherwise it updates
// an existing item.
export function set(key, value) {
	if (localStorageIsAvailable) {
		try {
			window.localStorage.setItem(prefix + key, value);
		} catch (exception) {
			console.error("storage: Setting item failed:", exception);
		}
		return;
	}

	if (cookieStorageIsAvailable) {
		cookieStorage.setCookie(prefix + key, value);
	}
}

// isLocalStorageAvailable returns whether localStorage is available.
export function isLocalStorageAvailable() {
	try {
		let key = "__storage_test__";
		window.localStorage.setItem(key, key);
		window.localStorage.removeItem(key);
	} catch (exception) {
		return false;
	}
	return true;
}
