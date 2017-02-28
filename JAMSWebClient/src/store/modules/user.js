export default {
	mutations: {
		queriedIsConnected(state) {
			state.queriedIsConnected = true;
		},
		setUserInfo(state, payload) {
			state.eMailAddress = payload.eMailAddress;
			state.id = payload.id;
			state.isAdmin = payload.isAdmin;
			state.isSignedIn = true;
			state.name = payload.name;
			state.username = payload.username;
		},
		signIn(state) {
			state.isSignedIn = true;
		},
		signOut(state) {
			state.eMailAddress = "";
			state.id = 0;
			state.isAdmin = false;
			state.isSignedIn = false;
			state.name = "";
			state.username = "";
		}
	},

	namespaced: true,

	state: {
		eMailAddress: "",
		id: 0,
		isAdmin: false,
		isSignedIn: false,
		name: "",
		queriedIsConnected: false,
		username: ""
	}
};
