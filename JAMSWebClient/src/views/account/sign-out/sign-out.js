import config from "../../../config";
import * as flashes from "../../../flashes";

export default {
	beforeCreate() {
		if (!this.$store.state.user.isSignedIn) {
			this.$router.push({name: "jobs"});
			return;
		}

		const url = config.apiBaseUrl + "/user/logout";

		this.$http.get(url).then((response) => {
			this.$store.commit("user/signOut");
			flashes.info("Signed out");
		}, (response) => {
			flashes.error("Couldn’t sign out");
		});
	}
};
