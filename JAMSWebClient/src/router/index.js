import Vue from "vue";
import VueRouter from "vue-router";

import account from "../views/account/account/account.vue";
import accountPassword from "../views/account/password/password.vue";
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
