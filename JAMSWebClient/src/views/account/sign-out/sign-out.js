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
				message: "You signed out successfully.",
				type: 0
			});
		});
	}
};
