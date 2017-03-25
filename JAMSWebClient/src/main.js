import Vue from "vue";
import VueResource from "vue-resource";

import app from "./views/app/app.vue";
import config from "./config";
import router from "./router/index.js";
import store from "./store/index.js";

Vue.use(VueResource);

// Allow cookies for cross-origin requests (i.e. for requests to API)
Vue.http.options.credentials = true;

// Intercept responses
Vue.http.interceptors.push(function(request, next) {
	// Save when request was sent
	store.commit("setDateLastRequest", Date.now());

	next(function(response) {
		const serverIsReachable = response.status >= 100 && response.status <= 499;
		store.commit("setIsConnected", serverIsReachable);

		if (response.status === 0 || response.status === 403) {
			checkSignInStatus();
			return;
		}

		if (response.status >= 200 && response.status <= 299) {
			return;
		}

		if (response.status === 409) {
			return;
		}

		console.error("unexpected response:", request, response);
	});
});

let isCheckingSignInStatus = false;

// checkSignInStatus checks whether the user is signed in locally, and if yes,
// checks whether the user is still signed in on the server. If not, the user is
// redirected to the sign-in page.
function checkSignInStatus() {
	if (isCheckingSignInStatus || router.currentRoute.name === "signIn") {
		return;
	}

	isCheckingSignInStatus = true;

	if (!store.state.user.isSignedIn) {
		router.push({
			name: "signIn",
			query: {
				from: router.currentRoute.fullPath
			}
		});
		isCheckingSignInStatus = false;
		return;
	}

	const url = config.apiBaseUrl + "/user/isConnected";

	const options = {
		credentials: true
	};

	Vue.http.get(url, options).then((response) => {
		response.json().then((data) => {
			// If user is signed in on server, the 403 response must mean the
			// action is forbidden even for signed in users
			if (data === true) {
				isCheckingSignInStatus = false;
				return;
			}

			// Sign out locally
			store.commit("user/signOut");

			// Redirect to sign-in page
			router.push({
				name: "signIn",
				query: {
					from: router.currentRoute.fullPath
				}
			});
			isCheckingSignInStatus = false;
		}, (response) => {
			console.error("main: Parsing JSON response failed:", response);
			isCheckingSignInStatus = false;
		});
	}, () => {
		isCheckingSignInStatus = false;
	});
}

/* eslint-disable no-new */
new Vue({
	el: "#app",
	render: (h) => h(app), // Replace <div id="app"></div> in index.html with app
	router,
	store
});
