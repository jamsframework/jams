import config from "../../../config";
import * as flashes from "../../../flashes";

export default {
	data() {
		return {
			eMailAddress: this.$store.state.user.eMailAddress,
			name: this.$store.state.user.name,
			username: this.$store.state.user.username
		};
	},
	methods: {
		save() {
			flashes.clear();

			const url = config.apiBaseUrl + "/user/" + this.$store.state.user.id;

			const body = {
				admin: this.$store.state.user.isAdmin ? 1 : 0,
				email: this.eMailAddress,
				id: this.$store.state.user.id,
				login: this.username,
				name: this.name,
				password: ""
			};

			this.$http.put(url, body).then((response) => {
				response.json().then((data) => {
					// Update user data locally
					this.$store.commit("user/setUserInfo", {
						eMailAddress: data.email,
						id: data.id,
						isAdmin: data.admin === 1,
						name: data.name,
						username: data.login
					});

					flashes.info("Updated account information");
				}, (response) => {
					console.error("account: Parsing JSON response failed:", response);
				});
			}, (response) => {
				if (response.status === 409) {
					flashes.error("Username is already in use");
					return;
				}

				flashes.error("Account info couldn’t be saved");
			});
		}
	}
};
