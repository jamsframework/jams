import config from "../../../config";
import * as flashes from "../../../flashes";
import * as storage from "../../../storage";

export default {
	created() {
		// If user is already signed in, redirect to home page
		if (this.$store.state.user.isSignedIn) {
			this.$router.replace({name: "jobs"});
			return;
		}

		if (!storage.cookieStorageIsAvailable) {
			flashes.error("Please enable cookies and refresh the page");
			return;
		}

		if (this.isRedirect) {
			flashes.info("Sign in to access this page");
		}
	},
	data() {
		return {
			canSignIn: storage.cookieStorageIsAvailable,
			isRedirect: !!this.$route.query.from,
			password: "",
			username: ""
		};
	},
	methods: {
		submit() {
			flashes.clear();

			const url = config.apiBaseUrl + "/user/login";

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
						this.$router.push(this.$route.query.from);
						return;
					}

					// Redirect to home page
					this.$router.push({name: "jobs"});
				}, (response) => {
					flashes.error("Couldn’t sign in");
					console.error("sign-in: Parsing JSON response failed:", response);
				});
			}, (response) => {
				if (response.status === 403) {
					flashes.error("Your username or password is not correct");
					return;
				}

				flashes.error("Couldn’t sign in");
			});
		}
	},
	watch: {
		"$route"(to, from) {
			if (this.$store.state.user.isSignedIn) {
				this.$router.replace({name: "jobs"});
				return;
			}

			if (!storage.cookieStorageIsAvailable) {
				flashes.error("Please enable cookies and refresh the page");
				return;
			}

			this.isRedirect = !!this.$route.query.from;

			if (this.isRedirect) {
				flashes.info("Sign in to access this page");
			}
		}
	}
};
