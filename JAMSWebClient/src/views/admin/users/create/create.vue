<template>
	<div>
		<h1>Create user</h1>

		<form class="box center max-width-small" @submit.prevent="save">
			<div class="vertical-list-item">
				<label for="username">Username:</label>
				<input id="username" placeholder="Username" type="text" v-model="username">
			</div>

			<div class="vertical-list-item">
				<label for="password">Password:</label>
				<input id="password" placeholder="Password" type="password" v-model="password">
			</div>

			<div class="vertical-list-item">
				<label for="name">Name (optional):</label>
				<input id="name" placeholder="Name" type="text" v-model="name">
			</div>

			<div class="vertical-list-item">
				<label for="eMailAddress">E-mail address (optional):</label>
				<input id="eMailAddress" placeholder="E-mail address" type="text" v-model="eMailAddress">
			</div>

			<div class="vertical-list-item">
				<input id="isAdmin" type="checkbox" v-model="isAdmin">
				<label for="isAdmin">Administrator</label>
			</div>

			<div class="vertical-list-item">
				<button class="primary" type="submit">Create</button>
			</div>
		</form>
	</div>
</template>

<style></style>

<script>
	import config from "../../../../config";
	import * as flashes from "../../../../flashes";

	export default {
		data() {
			return {
				eMailAddress: "",
				isAdmin: false,
				name: "",
				password: "",
				username: ""
			};
		},
		methods: {
			save() {
				flashes.clear();

				const url = config.apiBaseUrl + "/user/create";

				const body = {
					admin: this.isAdmin ? 1 : 0,
					email: this.eMailAddress,
					login: this.username,
					name: this.name,
					password: this.password
				};

				this.$http.put(url, body).then((response) => {
					response.json().then(() => {
						flashes.info("Created user");

						// Reset form
						this.eMailAddress = "";
						this.isAdmin = false;
						this.name = "";
						this.password = "";
						this.username = "";
					}, (response) => {
						console.error("users: Parsing JSON response failed:", response);
					});
				}, (response) => {
					if (response.status === 409) {
						flashes.error("Username is already in use");
						return;
					}

					flashes.error("User couldn’t be created");
				});
			}
		}
	};
</script>
