<template>
	<div>
		<header id="site-header">
			<nav class="flex-list">
				<router-link class="flex-list-item" exact to="/">Jobs</router-link>
				<router-link class="flex-list-item flex" to="/workspaces">Workspaces</router-link>
				<router-link class="flex-list-item" to="/account" v-if="$store.state.user.isSignedIn">{{$store.state.user.name}}</router-link>
				<router-link class="flex-list-item" to="/sign-in" v-if="!$store.state.user.isSignedIn">Sign in</router-link>
				<router-link class="flex-list-item" to="/sign-out" v-if="$store.state.user.isSignedIn">Sign out</router-link>
			</nav>
		</header>

		<div class="flash" :class="{'flash-error': flash.type === 1}" v-for="flash in $store.state.flashes.flashes">
			<div class="flash-cell">{{flash.message}}</div>
		</div>

		<div id="content">
			<router-view></router-view>
		</div>

		<footer id="site-footer"></footer>
	</div>
</template>

<style src="./reset.css"></style>
<style src="./main.css"></style>
<style src="./forms.css"></style>
