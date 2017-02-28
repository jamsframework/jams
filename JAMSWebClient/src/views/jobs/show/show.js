import date from "../../../date/date.js";

export default {
	computed: {
		formattedStartTime: function() {
			return date.formatDate(this.job.startTime);
		}
	},
	data() {
		return {
			duration: 0,
			isActive: false,
			job: null,
			progress: 1,
			size: 0
		};
	},
	methods: {

	},
	mounted() {
		const jobId = this.$router.currentRoute.params.id;
		const url = "http://localhost:8080/jamscloud/webresources/job/" + jobId + "/state";

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.duration = data.duration;
				this.isActive = data.active;
				this.job = data.job;
				this.progress = data.progress;
				this.size = data.size;
			}, (response) => {
				console.error("jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			// TODO: Add flash message.
			this.$store.commit("flashes/add", {
				message: "",
				type: 1
			});
			console.error("jobs: Unexpected response:", response);
		});
	}
};
