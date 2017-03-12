export default {
	mutations: {
		// add adds a flash message. payload is an object with keys “id”,
		// “message” and “type”.
		add(state, payload) {
			let flash = {
				id: payload.id,
				message: payload.message,
				type: payload.type
			};
			state.flashes.push(flash);
		},

		// clear deletes all flash messages that have the provided id. If id is
		// empty, all flash messages are deleted, regardless of id.
		clear(state, id = "") {
			if (!id) {
				state.flashes = [];
				return;
			}

			for (let i = 0; i < state.flashes.length; i++) {
				if (state.flashes[i].id === id) {
					state.flashes.splice(i, 1);
				}
			}
		}
	},

	namespaced: true,

	state: {
		flashes: []
	}
};
