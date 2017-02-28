module.exports = {
	root: true,
	parser: "babel-eslint",
	parserOptions: {
		sourceType: "module"
	},

	// https://github.com/feross/standard/blob/master/RULES.md#javascript-standard-style
	extends: "standard",
	// Required to lint *.vue files

	plugins: [
		"html"
	],
	"rules": {
		"arrow-parens": ["error", "always"],
		"generator-star-spacing": 0,
		"indent": ["error", "tab", {"SwitchCase": 1}],
		"no-debugger": process.env.NODE_ENV === "production" ? "error" : 0,
		"no-tabs": 0,
		"quotes": ["error", "double"],
		"semi": ["error", "always"],
		"space-before-function-paren": ["error", "never"]
	}
};
