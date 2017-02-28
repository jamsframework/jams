import * as storage from "../../storage";

// keys used for identifying items in storage
const keyEmailAddress = "user.eMailAddress";
const keyId = "user.id";
const keyIsAdmin = "user.isAdmin";
const keyIsSignedIn = "user.isSignedIn";
const keyName = "user.name";
const keyUsername = "user.username";

export default {
	mutations: {
		queriedIsConnected(state) {
			state.queriedIsConnected = true;
		},
		setUserInfo(state, payload) {
			state.eMailAddress = payload.eMailAddress || "";
			state.id = payload.id || 0;
			state.isAdmin = payload.isAdmin || false;
			state.name = payload.name || "";
			state.username = payload.username || "";

			storage.set(keyEmailAddress, state.eMailAddress);
			storage.set(keyId, state.id);
			storage.set(keyIsAdmin, state.isAdmin);
			storage.set(keyName, state.name);
			storage.set(keyUsername, state.username);
		},
		signIn(state) {
			state.isSignedIn = true;
			storage.set(keyIsSignedIn, state.isSignedIn);
		},
		signOut(state) {
			state.eMailAddress = "";
			state.id = 0;
			state.isAdmin = false;
			state.isSignedIn = false;
			state.name = "";
			state.username = "";

			storage.remove(keyEmailAddress);
			storage.remove(keyId);
			storage.remove(keyIsAdmin);
			storage.remove(keyIsSignedIn);
			storage.remove(keyName);
			storage.remove(keyUsername);
		}
	},

	namespaced: true,

	// Restore state from persistent storage if possible
	state: {
		eMailAddress: storage.get(keyEmailAddress, ""),
		id: parseInt(storage.get(keyId, 0), 10),
		isAdmin: storage.get(keyIsAdmin) === "true",
		isSignedIn: storage.get(keyIsSignedIn) === "true",
		name: storage.get(keyName, ""),
		queriedIsConnected: false,
		username: storage.get(keyUsername, "")
	}
};
