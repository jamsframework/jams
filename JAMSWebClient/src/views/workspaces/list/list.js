import config from "../../../config";
import {formatDateTime} from "../../../date";

export default {
	created() {
		this.getWorkspaces();
	},
	data() {
		return {
			workspaces: []
		};
	},
	methods: {
		getWorkspaces() {
			const url = config.baseUrl + "/workspace/find";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.workspaces = data.workspaces;
				}, (response) => {
					console.error("workspaces: Parsing JSON response failed:", response);
				});
			}, (response) => {
				this.$store.commit("flashes/add", {
					message: "Workspace list couldn’t be loaded",
					type: 1
				});
			});
		},
		removeWorkspace(workspace) {
			const message = "Remove workspace “" + workspace.name + "”?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.baseUrl + "/workspace/" + workspace.id + "/delete";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					for (let i = 0; i < this.workspaces.length; i++) {
						if (this.workspaces[i].id === data.id) {
							this.workspaces.splice(i, 1);
							break;
						}
					}
				}, (response) => {
					console.error("workspaces: Parsing JSON response failed:", response);
				});
			}, (response) => {
				this.$store.commit("flashes/add", {
					message: "Workspace couldn’t be removed",
					type: 1
				});
			});
		},
		formatDateTime
	}
};
