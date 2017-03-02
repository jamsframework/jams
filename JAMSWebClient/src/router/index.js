import Vue from "vue";
import VueRouter from "vue-router";

import account from "../views/account/account/account.vue";
import accountPassword from "../views/account/password/password.vue";
// import config from "../config";
import jobsList from "../views/jobs/list/list.vue";
import jobsShow from "../views/jobs/show/show.vue";
import notFound from "../views/not-found.vue";
import signIn from "../views/account/sign-in/sign-in.vue";
import signOut from "../views/account/sign-out/sign-out.vue";
import store from "../store/index";
import workspacesList from "../views/workspaces/list/list.vue";
import workspacesShow from "../views/workspaces/show/show.vue";

Vue.use(VueRouter);

const router = new VueRouter({
	mode: "history",
	routes: [
		{
			component: jobsList,
			meta: {
				requiresAuth: true
			},
			path: "/"
		},
		{
			component: account,
			meta: {
				requiresAuth: true
			},
			path: "/account"
		},
		{
			component: accountPassword,
			meta: {
				requiresAuth: true
			},
			path: "/account/password"
		},
		{
			path: "/jobs",
			redirect: "/"
		},
		{
			component: jobsShow,
			meta: {
				requiresAuth: true
			},
			path: "/jobs/show/:id/:logType?"
		},
		{
			component: signIn,
			path: "/sign-in"
		},
		{
			component: signOut,
			path: "/sign-out"
		},
		{
			component: workspacesList,
			meta: {
				requiresAuth: true
			},
			path: "/workspaces"
		},
		{
			component: workspacesShow,
			meta: {
				requiresAuth: true
			},
			path: "/workspaces/show/:id"
		},
		{
			component: notFound,
			path: "*"
		}
	]
});

// Check rules before navigating to a page
router.beforeEach((to, from, next) => {
	// // Query the server whether the user is signed in
	// if (!store.state.user.queriedIsConnected) {
	// 	const url = config.baseUrl + "/user/isConnected";
	//
	// 	const options = {
	// 		credentials: true
	// 	};
	//
	// 	Vue.http.get(url, options).then((response) => {
	// 		response.json().then((data) => {
	// 			store.commit("user/queriedIsConnected");
	//
	// 			// If user is signed in on server, update sign in status in app
	// 			if (data === true) {
	// 				store.commit("user/signIn");
	// 				next();
	// 				return;
	// 			}
	//
	// 			// If user is not signed in on server, redirect to sign-in page
	// 			next({
	// 				path: "/sign-in",
	// 				query: {
	// 					from: to.path
	// 				}
	// 			});
	// 		}, (response) => {
	// 			console.error("router: Parsing JSON response failed:", response);
	// 		});
	// 	});
	//
	// 	// Block navigation while request is running
	// 	next(false);
	//
	// 	return;
	// }

	// If page or any parent page requires authentication, redirect to sign-in page
	if (to.matched.some((record) => record.meta.requiresAuth) && !store.state.user.isSignedIn) {
		next({
			path: "/sign-in",
			query: {
				from: to.fullPath
			}
		});
		return;
	}

	// Delete flash messages from previous page
	store.commit("flashes/clear");

	// Navigate to the destination page
	next();
});

export default router;
