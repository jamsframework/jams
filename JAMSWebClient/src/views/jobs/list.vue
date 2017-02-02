<template>
	<div class="center max-width-medium">
		<h1>Jobs</h1>

		<div class="box" v-for="job in jobs">
			<div>Job</div>
			<div class="small">
				<span class="tag">ID: {{job.id}}</span>
				<span class="tag">Started: {{formatDate(job.startTime)}}</span>
				<span class="tag">Model: {{job.modelFile.path}}</span>
				<router-link class="tag" :to="'/workspaces/show/' + job.workspace.id">Workspace {{job.workspace.id}}</router-link>
				<router-link class="tag" :to="'/jobs/show/' + job.id">Details</router-link>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	data() {
		return {
			jobs: []
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
		const url = "http://localhost:8080/jamscloud/webresources/job/find";

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.jobs = data.jobs;
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
</script>
