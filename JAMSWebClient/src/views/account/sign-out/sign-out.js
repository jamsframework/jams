import config from "../../../config";

export default {
	beforeCreate() {
		if (!this.$store.state.user.isSignedIn) {
			this.$router.push("/");
			return;
		}

		const url = config.baseUrl + "/user/logout";

		this.$http.get(url).then((response) => {
			this.$store.commit("user/signOut");

			this.$store.commit("flashes/add", {
				message: "Signed out",
				type: 0
			});
		}, (response) => {
			this.$store.commit("flashes/add", {
				message: "Couldn’t sign out",
				type: 1
			});
		});
	}
};
