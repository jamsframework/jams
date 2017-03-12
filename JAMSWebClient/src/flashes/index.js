import store from "../store";

export const TYPE_ERROR = 1;
export const TYPE_INFO = 0;

// add adds a flash message.
export function add(message = "", type = TYPE_INFO, id = "") {
	store.commit("flashes/add", {
		id: id,
		message: message,
		type: type
	});
}

// clear deletes all flash messages.
export function clear(id = "") {
	store.commit("flashes/clear", id);
}

// error adds a flash message of type TYPE_ERROR.
export function error(message = "", id = "") {
	add(message, TYPE_ERROR, id);
}

// error adds a flash message of type TYPE_INFO.
export function info(message = "", id = "") {
	add(message, TYPE_INFO, id);
}
