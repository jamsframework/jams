import config from "../../../config";

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
			this.$store.commit("flashes/clear");

			const url = config.baseUrl + "/user/" + this.$store.state.user.id;

			const body = {
				admin: this.$store.state.user.isAdmin ? 1 : 0,
				email: this.eMailAddress,
				id: this.$store.state.user.id,
				login: this.username,
				name: this.name,
				password: ""
			};

			console.debug(body);

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

					this.$store.commit("flashes/add", {
						message: "Updated account information successfully."
					});
				}, (response) => {
					console.error("account: Parsing JSON response failed:", response);
				});
			}, (response) => {
				if (response.status === 409) {
					this.$store.commit("flashes/add", {
						message: "This username is already in use.",
						type: 1
					});
				}
			});
		}
	}
};
