import config from "../../../config";
import * as storage from "../../../storage";

export default {
	beforeCreate() {
		// If user is already signed in, redirect to home page
		if (this.$store.state.user.isSignedIn) {
			this.$router.replace("/");
			return;
		}

		if (!storage.cookieStorageIsAvailable) {
			this.$store.commit("flashes/add", {
				message: "Please enable cookies, and refresh the page.",
				type: 1
			});
			return;
		}

		if (this.isRedirect) {
			this.$store.commit("flashes/add", {
				message: "Please sign in to access this page.",
				type: 0
			});
		}
	},
	data() {
		return {
			canSignIn: storage.cookieStorageIsAvailable,
			isRedirect: !!this.$router.currentRoute.query.from,
			password: "",
			username: ""
		};
	},
	methods: {
		submit() {
			this.$store.commit("flashes/clear");

			const url = config.baseUrl + "/user/login";

			const options = {
				params: {
					login: this.username,
					password: this.password
				}
			};

			this.$http.get(url, options).then((response) => {
				response.json().then((data) => {
					// Store that user is signed in
					this.$store.commit("user/signIn");

					// Store user data
					this.$store.commit("user/setUserInfo", {
						eMailAddress: data.email,
						id: data.id,
						isAdmin: data.admin === 1,
						name: data.name,
						username: data.login
					});

					// If user was redirected to sign-in page, redirect back to source
					if (this.isRedirect) {
						this.$router.push(this.$router.currentRoute.query.from);
						return;
					}

					// Redirect to home page
					this.$router.push("/");
				}, (response) => {
					this.$store.commit("flashes/add", {
						message: "You could not be signed in because the server sent an unexpected response.",
						type: 1
					});
					console.error("SignIn: Parsing JSON response failed:", response);
				});
			}, (response) => {
				if (response.status === 403) {
					this.$store.commit("flashes/add", {
						message: "Your username or password is not correct.",
						type: 1
					});
					return;
				}

				this.$store.commit("flashes/add", {
					message: "Couldn’t sign in",
					type: 1
				});
			});
		}
	},
	watch: {
		"$route"(to, from) {
			if (this.$store.state.user.isSignedIn) {
				this.$router.replace("/");
				return;
			}

			if (!storage.cookieStorageIsAvailable) {
				this.$store.commit("flashes/add", {
					message: "Please enable cookies, and refresh the page.",
					type: 1
				});
				return;
			}

			this.isRedirect = !!this.$router.currentRoute.query.from;

			if (this.isRedirect) {
				this.$store.commit("flashes/add", {
					message: "Please sign in to access this page.",
					type: 0
				});
			}
		}
	}
};
