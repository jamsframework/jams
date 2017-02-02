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

/* eslint-disable no-new */
new Vue({
	el: "#app",
	render: (h) => h(app), // Replace <div id="app"></div> in index.html with app
	router,
	store
});
