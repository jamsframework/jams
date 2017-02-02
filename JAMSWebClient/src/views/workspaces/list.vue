<template>
	<div class="center max-width-medium">
		<h1>Workspaces</h1>

		<div class="box clickable" @click="showWorkspace(workspace.id)" v-for="workspace in workspaces">
			<div>{{workspace.name}}</div>
			<div class="small">
				<span class="tag">ID: {{workspace.id}}</span>
				<span class="tag">Size: {{Math.ceil(workspace.workspaceSize / 1024 / 1024)}} MiB</span>
				<span class="tag">Created: {{formatDate(workspace.creationDate)}}</span>
				<span class="tag">Read-only: {{workspace.readOnly}}</span>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	data() {
		return {
			workspaces: []
		};
	},
	methods: {
		formatDate(value) {
			let date = new Date(value);
			return date.getFullYear() + "-" +
				((date.getMonth() + 1) < 10 ? "0" : "") + (date.getMonth() + 1) + "-" +
				(date.getDate() < 10 ? "0" : "") + date.getDate() + " ";
		},
		showWorkspace(id) {
			this.$router.push("/workspaces/show/" + id);
		}
	},
	mounted() {
		const url = "http://localhost:8080/jamscloud/webresources/workspace/find";

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.workspaces = data.workspaces;
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
</script>
