export default {
	mutations: {
		signIn(state, payload) {
			state.eMailAddress = payload.eMailAddress;
			state.id = payload.id;
			state.isAdmin = payload.isAdmin;
			state.isSignedIn = true;
			state.name = payload.name;
			state.username = payload.username;
		},
		signOut(state) {
			state.isSignedIn = false;
		}
	},

	namespaced: true,

	state: {
		eMailAddress: "",
		id: 0,
		isAdmin: false,
		isSignedIn: false,
		name: "",
		username: ""
	}
};
