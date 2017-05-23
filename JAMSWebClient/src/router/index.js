import Vue from "vue";
import VueRouter from "vue-router";

import account from "../views/account/account/account.vue";
import accountPassword from "../views/account/password/password.vue";
import adminJobsList from "../views/admin/jobs/list/list.vue";
import adminUsersCreate from "../views/admin/users/create/create.vue";
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

// Access levels are used to restrict access to pages
const ACCESS_LEVEL_ADMIN = 2;
const ACCESS_LEVEL_USER = 1;

const router = new VueRouter({
	mode: "history",
	routes: [
		{
			component: jobsList,
			meta: {
				accessLevel: ACCESS_LEVEL_USER
			},
			name: "jobs",
			path: basePath + "/"
		},
		{
			component: account,
			meta: {
				accessLevel: ACCESS_LEVEL_USER
			},
			name: "account",
			path: basePath + "/account"
		},
		{
			component: adminJobsList,
			meta: {
				accessLevel: ACCESS_LEVEL_ADMIN
			},
			name: "adminJobsList",
			path: basePath + "/admin/jobs/list"
		},
		{
			component: adminUsersCreate,
			meta: {
				accessLevel: ACCESS_LEVEL_ADMIN
			},
			name: "adminUsersCreate",
			path: basePath + "/admin/users/create"
		},
		{
			component: accountPassword,
			meta: {
				accessLevel: ACCESS_LEVEL_USER
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
				accessLevel: ACCESS_LEVEL_USER
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
				accessLevel: ACCESS_LEVEL_USER
			},
			name: "workspaces",
			path: basePath + "/workspaces"
		},
		{
			component: workspacesShow,
			meta: {
				accessLevel: ACCESS_LEVEL_USER
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
	if (to.matched.some((record) => record.meta.accessLevel) && !store.state.user.isSignedIn) {
		next({
			name: "signIn",
			query: {
				from: to.fullPath
			}
		});
		return;
	}

	// If page or any parent page requires admin rights
	if (to.matched.some((record) => record.meta.accessLevel >= ACCESS_LEVEL_ADMIN) && !store.state.user.isAdmin) {
		flashes.add("You are not allowed to access this page.", flashes.TYPE_ERROR);
		return;
	}

	// Delete flash messages from previous page
	flashes.clear();

	// Navigate to the destination page
	next();
});

export default router;
