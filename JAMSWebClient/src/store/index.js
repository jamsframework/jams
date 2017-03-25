import Vue from "vue";
import Vuex from "vuex";

import flashes from "./modules/flashes.js";
import user from "./modules/user.js";

Vue.use(Vuex);

export default new Vuex.Store({
	modules: {
		flashes,
		user
	},
	mutations: {
		// setDateLastRequest saves the timestamp of the most recent request
		// to the server.
		setDateLastRequest(state, date) {
			state.dateLastRequest = date;
		},

		// setIsConnected saves whether the server is reachable.
		setIsConnected(state, isConnected) {
			state.isConnected = isConnected;
		},

		// setIsOnline saves whether the user’s browser reports it is online.
		setIsOnline(state, isOnline) {
			state.isConnected = isOnline;
			state.isOnline = isOnline;
		},

		// setJamsCloudServerVersion saves the version of JAMS Cloud Server.
		setJamsCloudServerVersion(state, version) {
			state.jamsCloudServerVersion = version;
		}
	},
	state: {
		// Date of last request (in milliseconds)
		dateLastRequest: 0,

		// Whether JAMS Cloud Server can be reached
		isConnected: false,

		// Whether the user is connected to the Internet
		isOnline: false,

		// Version of JAMS Cloud Server
		jamsCloudServerVersion: ""

	},
	// Set strict=false in production mode to avoid performance penalty, see
	// <http://vuex.vuejs.org/en/strict.html>.
	strict: process.env.NODE_ENV !== "production"
});
