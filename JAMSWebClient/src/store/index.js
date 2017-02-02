import Vue from "vue";
import Vuex from "vuex";

import flashes from "./flashes.js";
import user from "./user.js";

Vue.use(Vuex);

export default new Vuex.Store({
	modules: {
		flashes,
		user
	},
	// Set strict=false in production mode because of performance penalty, see
	// <http://vuex.vuejs.org/en/strict.html>.
	strict: process.env.NODE_ENV !== "production"
});
