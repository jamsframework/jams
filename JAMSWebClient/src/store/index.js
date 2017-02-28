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
		setIsConnected(state, isConnected) {
			state.isConnected = isConnected;
		},
		setJamsCloudServerVersion(state, version) {
			state.jamsCloudServerVersion = version;
		}
	},
	state: {
		isConnected: false,
		jamsCloudServerVersion: ""
	},
	// Set strict=false in production mode to avoid performance penalty, see
	// <http://vuex.vuejs.org/en/strict.html>.
	strict: process.env.NODE_ENV !== "production"
});
