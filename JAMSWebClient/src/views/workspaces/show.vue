<template>
	<div class="center max-width-medium">
		<h1>Workspace</h1>

		<div class="box" v-if="workspace !== null">
			<div>{{workspace.name}}</div>
			<div class="small">
				<span class="tag">ID: {{workspace.id}}</span>
				<span class="tag">Size: {{Math.ceil(workspace.workspaceSize / 1024 / 1024)}} MiB</span>
				<span class="tag">Created: {{formatDate(workspace.creationDate)}}</span>
				<span class="tag">Read-only: {{workspace.readOnly}}</span>
			</div>

			<div class="files" v-if="workspace.WorkspaceFileAssociation.length > 0">
				<div>Files ({{workspace.WorkspaceFileAssociation.length}}):</div>
				<ul class="small">
					<li v-for="file in workspace.WorkspaceFileAssociation">
						{{file.role}}
						{{file.ID}}
						<span class="code">{{file.path}}</span>
					</li>
				</ul>
			</div>

			<div class="files" v-else>
				<div>Files (0)</div>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	data() {
		return {
			workspace: null
		};
	},
	methods: {
		formatDate(value) {
			let date = new Date(value);
			return date.getFullYear() + "-" +
				((date.getMonth() + 1) < 10 ? "0" : "") + (date.getMonth() + 1) + "-" +
				(date.getDate() < 10 ? "0" : "") + date.getDate() + " ";
		}
	},
	mounted() {
		const workspaceId = this.$router.currentRoute.params.id;
		const url = "http://localhost:8080/jamscloud/webresources/workspace/" + workspaceId;

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.workspace = data.workspaces[0];
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
</script>

<style scoped>
.files {
	margin-top: 1rem;
}
</style>
