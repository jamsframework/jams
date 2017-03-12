import Vue from "vue";
import VueRouter from "vue-router";

import account from "../views/account/account/account.vue";
import accountPassword from "../views/account/password/password.vue";
import buildConfig from "../../config";
import * as flashes from "../flashes";
import jobsList from "../views/jobs/list/list.vue";
import jobsShow from "../views/jobs/show/show.vue";
import notFound from "../views/not-found.vue";
import signIn from "../views/account/sign-in/sign-in.vue";
import signOut from "../views/account/sign-out/sign-out.vue";
import store from "../store/index";
import workspacesList from "../views/workspaces/list/list.vue";
import workspacesShow from "../views/workspaces/show/show.vue";

Vue.use(VueRouter);

let basePath = process.env.NODE_ENV === "production"
	? buildConfig.build.assetsPublicPath
	: buildConfig.dev.assetsPublicPath;

// Remove trailing slashes from basePath
basePath = basePath.replace(/\/+$/, "");

const router = new VueRouter({
	mode: "history",
	routes: [
		{
			component: jobsList,
			meta: {
				requiresAuth: true
			},
			name: "jobs",
			path: basePath + "/"
		},
		{
			component: account,
			meta: {
				requiresAuth: true
			},
			name: "account",
			path: basePath + "/account"
		},
		{
			component: accountPassword,
			meta: {
				requiresAuth: true
			},
			name: "accountPassword",
			path: basePath + "/account/password"
		},
		{
			path: basePath + "/jobs",
			redirect: basePath + "/"
		},
		{
			component: jobsShow,
			meta: {
				requiresAuth: true
			},
			name: "job",
			path: basePath + "/jobs/show/:id/:logType?"
		},
		{
			component: signIn,
			name: "signIn",
			path: basePath + "/sign-in"
		},
		{
			component: signOut,
			name: "signOut",
			path: basePath + "/sign-out"
		},
		{
			component: workspacesList,
			meta: {
				requiresAuth: true
			},
			name: "workspaces",
			path: basePath + "/workspaces"
		},
		{
			component: workspacesShow,
			meta: {
				requiresAuth: true
			},
			name: "workspace",
			path: basePath + "/workspaces/show/:id"
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
			name: "signIn",
			query: {
				from: to.fullPath
			}
		});
		return;
	}

	// Delete flash messages from previous page
	flashes.clear();

	// Navigate to the destination page
	next();
});

export default router;
