<template>
	<div>
		<h1>Account</h1>

		<form class="box center max-width-small">
			<div class="vertical-list-item">
				<label>Username:</label>
				<input disabled :value="$store.state.user.username" type="text">
			</div>

			<div class="vertical-list-item">
				<label>Name:</label>
				<input disabled :value="$store.state.user.name" type="text">
			</div>

			<div class="vertical-list-item">
				<label>E-mail address:</label>
				<input disabled :value="$store.state.user.eMailAddress" type="text">
			</div>

			<div class="vertical-list-item" v-if="$store.state.user.isAdmin">
				<label>Role:</label>
				<input disabled value="Administrator" type="text">
			</div>

			<!--<div class="vertical-list-item">-->
				<!--<button class="primary" disabled type="submit">Save</button>-->
			<!--</div>-->
		</form>
	</div>
</template>
