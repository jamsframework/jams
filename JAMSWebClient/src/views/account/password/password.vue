<template>
	<div>
		<h1>Password</h1>

		<form class="box center max-width-small" @submit.prevent="save">
			<div class="vertical-list-item">
				<label for="newPassword">New password:</label>
				<input id="newPassword" placeholder="Password" type="password" v-model="newPassword">
			</div>

			<div class="vertical-list-item">
				<label for="newPasswordRepeated">New password (repeated):</label>
				<input id="newPasswordRepeated" placeholder="Password" type="password" v-model="newPasswordRepeated">
			</div>

			<div class="vertical-list-item">
				<button class="primary" type="submit">Save</button>
			</div>
		</form>
	</div>
</template>

<script>
import config from "../../../config";
import * as flashes from "../../../flashes";

export default {
	data() {
		return {
			newPassword: "",
			newPasswordRepeated: ""
		};
	},
	methods: {
		save() {
			flashes.clear();

			if (this.newPassword === "" && this.newPasswordRepeated === "") {
				flashes.error("Enter a password");
				return;
			}

			if (this.newPassword !== this.newPasswordRepeated) {
				flashes.error("Password and repeated password do not match");
				return;
			}

			const url = config.apiBaseUrl + "/user/" + this.$store.state.user.id;

			const body = {
				admin: this.$store.state.user.isAdmin ? 1 : 0,
				email: this.$store.state.user.eMailAddress,
				id: this.$store.state.user.id,
				login: this.$store.state.user.username,
				name: this.$store.state.user.name,
				password: this.newPassword
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

					if (document.activeElement) {
						document.activeElement.blur();
					}

					flashes.info("Saved password");
				}, (response) => {
					console.error("account: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Password couldn’t be saved");
			});
		}
	}
};
</script>
