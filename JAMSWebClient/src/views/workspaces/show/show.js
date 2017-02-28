import config from "../../../config";
import date from "../../../date";

export default {
	computed: {
		formattedCreationDate: function() {
			return date.formatDate(this.workspace.creationDate);
		}
	},
	data() {
		return {
			workspace: null
		};
	},
	methods: {

	},
	mounted() {
		const workspaceId = this.$router.currentRoute.params.id;
		const url = config.baseUrl + "/workspace/" + workspaceId;

		this.$http.get(url).then((response) => {
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
