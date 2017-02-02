<template></template>

<script>
export default {
	beforeCreate() {
		if (!this.$store.state.user.isSignedIn) {
			this.$router.push("/");
			return;
		}

		this.$store.commit("user/signOut");

		this.$store.commit("flashes/add", {
			message: "You signed out successfully.",
			type: 0
		});
	}
};
</script>
