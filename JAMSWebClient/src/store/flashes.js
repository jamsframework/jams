export default {
	mutations: {
		// add adds a flash message. payload is an object with keys “message”
		// and “type”. If type is integer 1, flash messages are displayed in
		// red.
		add(state, payload) {
			let flash = {
				message: payload.message,
				type: payload.type
			};
			state.flashes.push(flash);
		},

		// Delete all flash messages.
		clear(state) {
			state.flashes = [];
		}
	},

	namespaced: true,

	state: {
		flashes: []
	}
};
