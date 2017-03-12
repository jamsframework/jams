import Vue from "vue";
import VueCookie from "vue-cookie";
import VueResource from "vue-resource";
import {sync} from "vuex-router-sync";

import app from "./views/app/app.vue";
import config from "./config";
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
				handle403(response);
				break;
			case 200:
				break;
			case 403:
				handle403(response);
				break;
			case 409:
				break;
			default:
				console.error("unexpected response:", request, response);
		}
	});
});

/* eslint-disable no-unused-vars */
let isHandling403 = false;

// handle403 handles HTTP status code 403 Forbidden responses. If the user is
// signed in locally, it is checked whether he is still signed in on the server.
// If he is not, he is redirected to the sign-in page.
function handle403(response) {
	if (isHandling403 || response.url.indexOf(config.baseUrl + "/user/login") === 0) {
		return;
	}

	isHandling403 = true;

	if (!store.state.user.isSignedIn) {
		router.push({
			path: "/sign-in",
			query: {
				from: router.currentRoute.fullPath
			}
		});
		isHandling403 = false;
		return;
	}

	const url = config.baseUrl + "/user/isConnected";

	const options = {
		credentials: true
	};

	Vue.http.get(url, options).then((response) => {
		response.json().then((data) => {
			// If user is signed in on server, the 403 response must mean the
			// action is forbidden even for signed in users
			if (data === true) {
				isHandling403 = false;
				return;
			}

			// Sign out locally
			store.commit("user/signOut");

			// Redirect to sign-in page
			router.push({
				path: "/sign-in",
				query: {
					from: router.currentRoute.fullPath
				}
			});
			isHandling403 = false;
		}, (response) => {
			console.error("main: Parsing JSON response failed:", response);
			isHandling403 = false;
		});
	});
}

/* eslint-disable no-new */
new Vue({
	el: "#app",
	render: (h) => h(app), // Replace <div id="app"></div> in index.html with app
	router,
	store
});
