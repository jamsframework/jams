import config from "../../../config";

export default {
	data() {
		return {
			isRedirect: !!this.$router.currentRoute.query.from,
			password: "jamscloud",
			username: "admin"
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
					this.$store.commit({
						eMailAddress: data.email,
						id: data.id,
						isAdmin: data.admin === 1,
						name: data.name,
						type: "user/signIn",
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
				switch (response.status) {
				case 0:
					this.$store.commit("flashes/add", {
						message: "The server could not be reached.",
						type: 1
					});
					return;
				case 403:
					this.$store.commit("flashes/add", {
						message: "Your username or password is not correct.",
						type: 1
					});
					return;
				default:
					this.$store.commit("flashes/add", {
						message: "You could not be signed in because the server sent an unexpected response.",
						type: 1
					});
					console.error("SignIn: Unexpected response:", response);
				}
			});
		}
	},
	mounted() {
		// If user is already signed in, redirect to home page
		if (this.$store.state.user.isSignedIn) {
			this.$router.push("/");
			return;
		}

		if (this.isRedirect) {
			this.$store.commit("flashes/add", {
				message: "Please sign in to access this page.",
				type: 0
			});
		}
	},
	watch: {
		"$route"(to, from) {
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
