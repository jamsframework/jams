import Vue from "vue";
import VueCookie from "vue-cookie";
import VueResource from "vue-resource";
import {sync} from "vuex-router-sync";

import app from "./views/app/app.vue";
import router from "./router/index.js";
import store from "./store/index.js";

Vue.use(VueCookie);
Vue.use(VueResource);
sync(store, router);

// Allow cookies for cross-origin requests (i.e. for requests to API)
Vue.http.options.credentials = true;

// Intercept responses
Vue.http.interceptors.push(function(request, next) {
	// Save when request was sent
	store.commit("setDateLastRequest", Date.now());

	next(function(response) {
		// Update connection status
		store.commit("setIsConnected", response.status !== 0);

		switch (response.status) {
			case 0:
			case 200:
				break;
			case 403:
				console.debug("Intercepted 403. Should redirect!");
				// Vue.router.push("/sign-in");
				// console.debug("Intercepted 403. Should have redirected!");
				break;
			case 409:
				break;
			default:
				this.$store.commit("flashes/add", {
					message: "The server sent an unexpected response.",
					type: 1
				});
				console.error("unexpected response:", request, response);
		}
	});
});

/* eslint-disable no-new */
new Vue({
	el: "#app",
	render: (h) => h(app), // Replace <div id="app"></div> in index.html with app
	router,
	store
});
