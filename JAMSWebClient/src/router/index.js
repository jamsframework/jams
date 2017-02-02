import Vue from "vue";
import VueRouter from "vue-router";

import account from "../views/account/account.vue";
import jobsList from "../views/jobs/list.vue";
import jobsShow from "../views/jobs/show.vue";
import notFound from "../views/not-found.vue";
import signIn from "../views/account/sign-in.vue";
import signOut from "../views/account/sign-out.vue";
import store from "../store/index";
import workspacesList from "../views/workspaces/list.vue";
import workspacesShow from "../views/workspaces/show.vue";

Vue.use(VueRouter);

const router = new VueRouter({
	mode: "history",
	routes: [
		{
			path: "/jobs",
			redirect: "/"
		},
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
			component: jobsShow,
			meta: {
				requiresAuth: true
			},
			path: "/jobs/show/:id"
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
	// If page or any parent page requires authentication, redirect to sign-in
	if (to.matched.some((record) => record.meta.requiresAuth) &&
			!store.state.user.isSignedIn) {
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

	next();
});

export default router;
